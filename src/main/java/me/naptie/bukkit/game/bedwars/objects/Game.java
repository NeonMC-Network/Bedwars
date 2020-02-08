package me.naptie.bukkit.game.bedwars.objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.data.RollbackHandler;
import me.naptie.bukkit.game.bedwars.listeners.PlayerDeath;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.tasks.Countdown;
import me.naptie.bukkit.game.bedwars.tasks.End;
import me.naptie.bukkit.game.bedwars.tasks.Respawn;
import me.naptie.bukkit.game.bedwars.tasks.Run;
import me.naptie.bukkit.game.bedwars.utils.BedMaterialUtil;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.utils.LocationUtil;
import me.naptie.bukkit.game.bedwars.utils.MapUtil;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.IOException;
import java.util.*;

public class Game {

	private Table<UUID, GamePlayer, String> entityNameTable = HashBasedTable.create();
	private Main plugin;
	private String displayName, worldName, modeName;
	private Set<GamePlayer> all = new HashSet<>(), players = new HashSet<>(), spectators = new HashSet<>();
	private int id, minPlayers, maxPlayers, playersPerTeam;
	private List<GameTeam> teams = new ArrayList<>();
	private World world;
	private Location lobby;
	private Set<Generator> basicGenerators = new HashSet<>(), diamondGenerators = new HashSet<>(), emeraldGenerators = new HashSet<>();
	private Set<GameBed> beds = new HashSet<>();
	private Map<LivingEntity, Location> shops = new HashMap<>(), upgrades = new HashMap<>();
	//	private Set<LivingEntity> shops = new HashSet<>(), upgrades = new HashSet<>();
	private GameState gameState = GameState.LOBBY;
	private Map<Player, Objective> objectiveMap = new HashMap<>();
	private GameShop shop;
	private GameUpgrade upgrade;
	private boolean ended = false;
	private Map<GamePlayer, GameTeam> playerTeamQueue = new HashMap<>();

	public Game(Main plugin) {
		this.plugin = plugin;
		configure();
		this.plugin.getMySQLManager().editor.set(this.id, "Bedwars", me.naptie.bukkit.core.Main.getInstance().getServerName(), this.getWorldName(), this.minPlayers, this.maxPlayers, this.players.size(), this.spectators.size(), this.gameState.name(), this.playersPerTeam);
	}

	private void configure() {
		FileConfiguration i = this.plugin.getDataHandler().getGameInfo();
		this.id = i.getInt("id");
		this.displayName = i.getString("display-name");
		this.maxPlayers = i.getInt("max-players");
		int playersPerTeam = i.getInt("players-per-team");
		int minPlayers = i.getInt("min-players");
		if (playersPerTeam == 0 || minPlayers <= this.playersPerTeam) {
			if (playersPerTeam == 0) {
				this.plugin.getLogger().warning(Messages.getMessage("zh-CN", "PLAYERPERTEAM_IS_0"));
				playersPerTeam = 1;
				i.set("players-per-team", 1);
			}
			if (minPlayers <= this.playersPerTeam) {
				minPlayers = this.playersPerTeam + 1;
				this.plugin.getLogger().warning(Messages.getMessage("zh-CN", "MIN_NOT_GREATER_THAN_PLAYERPERTEAM").replace("%number%", minPlayers + ""));
				i.set("min-players", minPlayers);
			}
			this.plugin.getDataHandler().saveGameInfo();
		}
		this.playersPerTeam = playersPerTeam;
		this.minPlayers = minPlayers;
		this.worldName = i.getString("world");
		registerWorld();
		this.lobby = LocationUtil.parseLocation(this.world, i.getString("lobby"));
		registerStates(i);
		registerTeams(i);
		registerGenerators(i);
		registerNPCs(i);
		if (this.maxPlayers / this.playersPerTeam > 4) {
			if (this.playersPerTeam == 1) this.modeName = "SOLO";
			else if (this.playersPerTeam == 2) this.modeName = "DOUBLE";
			else if (this.playersPerTeam == 3) this.modeName = "TRIPLE";
			else if (this.playersPerTeam == 4) this.modeName = "QUADRUPLE";
		} else {
			for (int times = 0; times < this.maxPlayers / this.playersPerTeam; times++) {
				if (times == 0) {
					this.modeName = this.playersPerTeam + "";
				} else {
					this.modeName = String.format("%sv%d", this.modeName, this.playersPerTeam);
				}
			}
		}
		this.shop = new GameShop(this.plugin);
		this.upgrade = new GameUpgrade(this);
	}

	private void registerStates(FileConfiguration i) {
		for (GameState gameState : GameState.values()) {
			if (i.get("events." + gameState.name()) != null)
				gameState.setLength(i.getInt("events." + gameState.name()));
		}
	}

	public void registerBeds() {
		for (GameTeam team : this.teams) {
			this.beds.add(new GameBed(this, team, team.getBedMaterial()));
		}
	}

	private void registerWorld() {
		Bukkit.unloadWorld(this.worldName + "_active", false);
		RollbackHandler.get().rollback(this.worldName);
		this.world = Bukkit.createWorld(new WorldCreator(this.worldName + "_active"));
		resetWorld();
	}

	public void resetWorld() {
		this.world.setDifficulty(Difficulty.NORMAL);
		this.world.setPVP(true);
		this.world.setTime(6000);
		this.world.setStorm(false);
		this.world.setThundering(false);
		this.world.setSpawnFlags(false, false);
		this.world.setAutoSave(false);
	}

	private void registerGenerators(FileConfiguration i) {
		for (String string : i.getStringList("generators.basic")) {
			if (string.startsWith("%@")) {
				String location = string.substring(1).split("%@")[0];
				GameTeam team = getTeamByColor(string.substring(1).split("%@")[1]);
				basicGenerators.add(new Generator(this.plugin, this, Generator.Type.BASIC, LocationUtil.parseLocation(this.world, location), team));
			} else {
				basicGenerators.add(new Generator(this.plugin, this, Generator.Type.BASIC, LocationUtil.parseLocation(this.world, string), null));
			}
		}
		for (String string : i.getStringList("generators.diamond")) {
			diamondGenerators.add(new Generator(this.plugin, this, Generator.Type.DIAMOND, LocationUtil.parseLocation(this.world, string), null));
		}
		for (String string : i.getStringList("generators.emerald")) {
			emeraldGenerators.add(new Generator(this.plugin, this, Generator.Type.EMERALD, LocationUtil.parseLocation(this.world, string), null));
		}
	}

	private void registerNPCs(FileConfiguration i) {
		for (int num = 0; num < i.getStringList("shops").size(); num++) {
			Location location = LocationUtil.parseLocation(this.world, i.getStringList("shops").get(num));
			LivingEntity shop = (LivingEntity) this.world.spawnEntity(location, EntityType.VILLAGER);
			shop.setCustomName("SHOP");
			shop.setCustomNameVisible(true);
			shop.setCanPickupItems(false);
			shop.teleport(location);
			shop.setInvulnerable(true);
			shop.setAI(false);
			shop.setCollidable(false);
			shops.put(shop, location);
		}
		for (int num = 0; num < i.getStringList("upgrades").size(); num++) {
			Location location = LocationUtil.parseLocation(this.world, i.getStringList("upgrades").get(num));
			LivingEntity upgrade = (LivingEntity) this.world.spawnEntity(location, EntityType.VILLAGER);
			upgrade.setCustomName(this.playersPerTeam == 1 ? "SOLO_UPGRADE" : "TEAM_UPGRADE");
			upgrade.setCustomNameVisible(true);
			upgrade.setCanPickupItems(false);
			upgrade.teleport(location);
			upgrade.setInvulnerable(true);
			upgrade.setAI(false);
			upgrade.setCollidable(false);
			upgrades.put(upgrade, location);
		}
	}

	public void registerEntityNames(GamePlayer player) {
		for (Generator gen : diamondGenerators) {
			entityNameTable.put(gen.getTitleId(), player, Messages.getMessage(player, "DIAMOND_" + gen.getType().getPeriod()));
		}
		for (Generator gen : emeraldGenerators) {
			entityNameTable.put(gen.getTitleId(), player, Messages.getMessage(player, "EMERALD_" + gen.getType().getPeriod()));
		}
		for (LivingEntity shop : shops.keySet()) {
			entityNameTable.put(shop.getUniqueId(), player, Messages.getMessage(player, "SHOP"));
		}
		for (LivingEntity upgrade : upgrades.keySet()) {
			entityNameTable.put(upgrade.getUniqueId(), player, Messages.getMessage(player, this.playersPerTeam == 1 ? "SOLO_UPGRADE" : "TEAM_UPGRADE"));
		}
	}

	private void registerTeams(FileConfiguration i) {
		for (String key : i.getConfigurationSection("teams").getKeys(false)) {
			GameTeam team = new GameTeam(GameTeam.TeamColor.valueOf(key.toUpperCase()));
			double x = i.getDouble("teams." + key + ".spawnpoint.x");
			double y = i.getDouble("teams." + key + ".spawnpoint.y");
			double z = i.getDouble("teams." + key + ".spawnpoint.z");
			float yaw = (float) i.getDouble("teams." + key + ".spawnpoint.yaw");
			float pitch = (float) i.getDouble("teams." + key + ".spawnpoint.pitch");
			team.setSpawnpoint(new Location(this.world, x, y, z, yaw, pitch));
			List<Location> bedLocation = new ArrayList<>();
			for (String string : i.getStringList("teams." + key + ".bed")) {
				bedLocation.add(LocationUtil.parseLocation(this.world, string));
			}
			team.setBedLocation(bedLocation);
			this.teams.add(team);
		}
	}

	private GameTeam getTeamByColor(String color) {
		for (GameTeam team : this.teams) {
			if (team.getColor().name().toLowerCase().equals(color.toLowerCase())) {
				return team;
			}
		}
		return null;
	}

	public void join(GamePlayer gamePlayer) {
		if (isState(GameState.LOBBY) || isState(GameState.STARTING)) {
			if (getPlayers().size() == getMaxPlayers()) {
				gamePlayer.sendMessage(Messages.getMessage(gamePlayer, "GAME_FULL"));
				gamePlayer.sendToLobby();
				return;
			}
			getPlayers().add(gamePlayer);
			String size = String.valueOf(getPlayers().size());
			this.plugin.getMySQLManager().editor.set(this.id, "players", this.players.size());
			gamePlayer.teleport(isState(GameState.LOBBY) || isState(GameState.STARTING) ? lobby : null);

			Player player = gamePlayer.getPlayer();
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(gamePlayer.getPlayer().getMaxHealth());
			player.setFoodLevel(20);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.setAllowFlight(false);
			gamePlayer.giveInteractiveItems();
			for (GamePlayer eachGamePlayer : getPlayers()) {
				eachGamePlayer.getPlayer().showPlayer(gamePlayer.getPlayer());
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> sendMessage("JOINED", new String[]{"%player%", "%size%", "%max%"}, new String[]{gamePlayer.getName(), size, String.valueOf(getMaxPlayers())}), 1);

			if (getPlayers().size() == getMinPlayers() && !isState(GameState.STARTING)) {
				setState(GameState.STARTING);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
					playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					start();
				}, 1);
			}

		} else {
			activateSpectatorSettings(gamePlayer, true);
			gamePlayer.getPlayer().setScoreboard(Run.board);
		}
	}

	public void leave(GamePlayer gamePlayer) {
		getPlayers().remove(gamePlayer);
		if (gamePlayer.getTeam() != null)
			gamePlayer.getTeam().removeMember(gamePlayer);
		this.plugin.getMySQLManager().editor.set(this.id, "players", this.players.size());
		if (gamePlayer.isSpectating()) {
			getSpectators().remove(gamePlayer);
			this.plugin.getMySQLManager().editor.set(this.id, "spectators", this.spectators.size());
		}

		if (players.size() != 0)
			sendMessage("QUIT", new String[]{"%player%"}, new String[]{gamePlayer.getName()});

		Player player = gamePlayer.getPlayer();
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}

		Run.playerObjectiveMap.remove(player.getUniqueId());
		Run.killCount.remove(player.getUniqueId());
		Run.finalKillCount.remove(player.getUniqueId());
		Run.totalKillCount.remove(player.getUniqueId());

		if (getGamePlayer(player) != null && !(isState(GameState.LOBBY) || isState(GameState.STARTING) || this.ended)) {
			if (getPlayers().size() == 1 || getAliveTeams().size() == 1) {
				for (GamePlayer winner : getAliveTeams().get(0).getMembers()) {
					reward(winner, true);
				}
				judge(getAliveTeams().get(0), false);

			} else if (getPlayers().size() == 0 || getAliveTeams().size() == 0) {
				judge();
			}
		}
		gamePlayer.sendToLobby();
	}

	public void leaveFromSpectating(GamePlayer gamePlayer) {
		getSpectators().remove(gamePlayer);
		this.plugin.getMySQLManager().editor.set(this.id, "spectators", this.spectators.size());
		Player player = gamePlayer.getPlayer();
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		Run.playerObjectiveMap.remove(player.getUniqueId());
		gamePlayer.sendToLobby();
	}

	public void respawn(GamePlayer gamePlayer) {
		final Player player = gamePlayer.getPlayer();
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(player);
		}
		player.spigot().setCollidesWithEntities(false);
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
		player.getEquipment().clear();
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> player.addPotionEffect(
				new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false)), 1);

		new Respawn(this.plugin, gamePlayer).runTaskTimer(this.plugin, 0, 20);

		YamlConfiguration data = gamePlayer.getData();
		data.set("bedwars.deaths", data.getInt("bedwars.deaths") + 1);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String key, String[] targets, String[] replacements) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String message = Messages.getMessage(gamePlayer, key);
			for (int i = 0; i < targets.length; i++) {
				if (message.contains(targets[i]))
					message = message.replace(targets[i], replacements[i]);
			}
			gamePlayer.sendMessage(message);
		}
	}

	public void playSound(Sound sound, int volume, int pitch) {
		for (GamePlayer gamePlayer : getPlayers())
			gamePlayer.playSound(sound, volume, pitch);
	}

	public void activateSpectatorSettings(GamePlayer gamePlayer, boolean spawn) {

		getSpectators().add(gamePlayer);
		this.plugin.getMySQLManager().editor.set(this.id, "spectators", this.spectators.size());
		final Player player = gamePlayer.getPlayer();
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(player);
		}
		player.spigot().setCollidesWithEntities(false);
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
		player.getEquipment().clear();
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		gamePlayer.giveInteractiveItems();

		if (spawn) {
			Location spectatorSpawn = new Location(lobby.getWorld(), lobby.getX(),
					lobby.getY() - 5, lobby.getZ());
			player.teleport(spectatorSpawn);
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> player.addPotionEffect(
				new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false)), 1);

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if (!player.getAllowFlight())
				player.setAllowFlight(true);
			player.setFlying(true);
		}, 1L, 1L);

	}

	public void sendTitle(String titleText, String titleProperties, String subtitleText, String subtitleProperties, int titleFadeIn, int titleStay, int titleFadeOut) {
		for (GamePlayer gamePlayer : getPlayers()) {
			gamePlayer.sendTitle(titleText, titleProperties, subtitleText, subtitleProperties, titleFadeIn, titleStay, titleFadeOut);
		}
	}

	public void sendActionBar(String key, String[] targets, String[] replacements) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String message = Messages.getMessage(gamePlayer, key);
			for (int i = 0; i < targets.length; i++) {
				if (message.contains(targets[i]))
					message = message.replace(targets[i], replacements[i]);
			}
			gamePlayer.sendActionBar(message);
		}
	}

	private void start() {
		sendMessage("GAME_STARTING", new String[]{}, new String[]{});
		new Countdown(this.plugin, this).runTaskTimer(this.plugin, 0, 20);
	}

	private GameTeam getAvailableTeam() {
		for (GameTeam team : this.teams)
			if (team.getMembers().size() < this.playersPerTeam)
				return team;
		return null;
	}

	public void assignTeams() {
	//	int id = 0;
		for (GamePlayer gamePlayer : this.players) {
			if (getAvailableTeam() == null) {
				gamePlayer.sendMessage(Messages.getMessage(gamePlayer, "GAME_FULL"));
				gamePlayer.sendToLobby();
			}
			try {
				GameTeam gameTeam;
				if (this.playerTeamQueue.containsKey(gamePlayer)) {
					gameTeam = this.playerTeamQueue.get(gamePlayer);
				} else {
					/*int i = id / playersPerTeam;
					id++;
					gameTeam = this.teams.get(i);*/
					gameTeam = getAvailableTeam();
				}
				gameTeam.addMember(gamePlayer);
				gamePlayer.teleport(gameTeam.getSpawnpoint());
				Player player = gamePlayer.getPlayer();
				player.setGameMode(GameMode.SURVIVAL);
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
				for (PotionEffect potionEffect : player.getActivePotionEffects()) {
					player.removePotionEffect(potionEffect.getType());
				}
				player.getInventory().clear();
			} catch (Exception ex) {
				this.plugin.getLogger().severe(Messages.getMessage("zh-CN", "GAME_START_FAILURE").replace("%game%", getDisplayName()).replace("%ex%", ex + ""));
				ex.printStackTrace();
			}
		}
	}

	private void sendGlobalMessage(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			gamePlayer.sendMessage(message);
		}
	}

	private void sendGlobalMessageOfKillers(Map<Integer, Map<String, String>> killerListMap) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = getGamePlayer(player);
			for (int i : killerListMap.keySet()) {
				for (String language : killerListMap.get(i).keySet()) {
					if (gamePlayer.getLanguage().equals(language))
						gamePlayer.sendMessage(killerListMap.get(i).get(language));
				}
			}
		}
	}

	private void sendGlobalMessage(Map<String, String> messageMap) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			gamePlayer.sendMessage(messageMap.get(gamePlayer.getLanguage()));
		}
	}

	public void sendDeathMessage(int num, GamePlayer player, GamePlayer killer, boolean finalKill) {
		for (GamePlayer gamePlayer : this.all) {
			String originalMessage = ChatColor.GRAY + this.plugin.getConfig().getStringList(PlayerDeath.deathMessageMap.get(gamePlayer.getLanguage())).get(num);
			if (finalKill) {
				originalMessage = originalMessage + " " + Messages.getMessage(gamePlayer, "FINAL_KILL");
			}
			String message = originalMessage.replace("player", player.getName() + ChatColor.GRAY).replace("killer", killer.getName() + ChatColor.GRAY);
			if ((originalMessage.startsWith("player") ? player : killer).getData().getString("gender").equals("FEMALE")) {
				message = message.replaceAll(" he ", " she ");
				message = message.replaceAll(" he's ", " she's ");
				message = message.replaceAll(" his ", " her ");
				message = message.replaceAll("他", "她");
			}
			gamePlayer.sendMessage(message);
		}
	}

	public void sendDeathMessage(int num, GamePlayer player, boolean finalKill) {
		for (GamePlayer gamePlayer : this.all) {
			String originalMessage = ChatColor.GRAY + this.plugin.getConfig().getStringList(PlayerDeath.deathMessageMap.get(gamePlayer.getLanguage())).get(num);
			if (finalKill) {
				originalMessage = originalMessage + " " + Messages.getMessage(gamePlayer, "FINAL_KILL");
			}
			String message = originalMessage.replace("player", player.getName() + ChatColor.GRAY);
			if (player.getData().getString("gender").equals("FEMALE")) {
				message = message.replaceAll(" he ", " she ");
				message = message.replaceAll(" he's ", " she's ");
				message = message.replaceAll(" his ", " her ");
				message = message.replaceAll("他", "她");
			}
			gamePlayer.sendMessage(message);
		}
	}

	private void sendEndingMessages(GameTeam team) {
		Map<String, String> textMap1 = Messages.getMessagesInDifLangs("BEDWARS");
		Map<String, String> bedwarsTitleMap = new HashMap<>();
		for (String language : textMap1.keySet()) {
			bedwarsTitleMap.put(language, center(textMap1.get(language), 70));
		}
		Map<String, String> winnerMap = new HashMap<>();
		if (team != null) {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("WINNER");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language) + (language.contains("en") ? (team.getMembers().size() > 1 ? "s" : "") : "") + " &7-&r " + team.getColor().getChatColor() + "" + Messages.getMessage(language, team.getColor().name()) + " &7-&r " + team.getName()), 70));
			}
		} else {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("NOBODY_WON");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language)), 70));
			}
		}

		Map<Integer, Map<String, String>> killerListMap = new HashMap<>();
		int i = 0;
		for (Iterator<Map<UUID, Integer>> it = MapUtil.sortKills(Run.totalKillCount).iterator(); it.hasNext() && i < 3; ++i) {
			Map<UUID, Integer> each = it.next();
			Map<String, String> textMap2 = new HashMap<>();
			if (i == 0) {
				textMap2 = Messages.getMessagesInDifLangs("1ST_KILLER");
			} else if (i == 1) {
				textMap2 = Messages.getMessagesInDifLangs("2ND_KILLER");
			} else if (i == 2) {
				textMap2 = Messages.getMessagesInDifLangs("3RD_KILLER");
			}
			Map<String, String> killerMap = new HashMap<>();
			for (UUID uuid : each.keySet()) {
				GamePlayer killer = getGamePlayer(Bukkit.getPlayer(uuid));
				for (String language : textMap2.keySet()) {
					try {
						killerMap.put(language, center(CU.t(textMap2.get(language) + " &7- " + killer.getName() + " &7- &6" + Run.totalKillCount.get(killer.getPlayer().getUniqueId())), 70));
					} catch (NullPointerException ignored) {
					}
				}
			}
			killerListMap.put(i, killerMap);

		}

		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);
		sendGlobalMessage("");
		sendGlobalMessage(bedwarsTitleMap);
		sendGlobalMessage("");
		sendGlobalMessage(winnerMap);
		sendGlobalMessage("");
		sendGlobalMessageOfKillers(killerListMap);
		sendGlobalMessage("");
		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);

		sendActionBar("GAME_ENDED", new String[]{}, new String[]{});

		if (team != null) {
			for (GamePlayer winner : team.getMembers()) {
				winner.sendTitle(Messages.getMessage(winner, "VICTORY_TITLE"), "\"color\":\"gold\",\"bold\":true", winner.getLanguage().contains("en") ? Messages.getMessage(winner, "VICTORY_SUBTITLE").replace(" man ", " men ") : Messages.getMessage(winner, "VICTORY_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
			}
			for (GamePlayer spectator : spectators) {
				if (team.getName().contains(spectator.getPlayer().getName())) {
					spectator.sendTitle(Messages.getMessage(spectator, "VICTORY_TITLE"), "\"color\":\"gold\",\"bold\":true", spectator.getLanguage().contains("en") ? Messages.getMessage(spectator, "VICTORY_SUBTITLE").replace(" man ", " men ") : Messages.getMessage(spectator, "VICTORY_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				} else {
					spectator.sendTitle(Messages.getMessage(spectator, "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(spectator, "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				}
			}
		}
	}

	public void judge(Object winner, boolean force) {
		if (getAliveTeams().size() == 1) {
			sendEndingMessages((GameTeam) winner);
			setState(Game.GameState.ENDING);
			Run.getInstance().setTime(this.plugin.getDataHandler().getGameInfo().getInt("events.ENDING"));
			ended = true;
			new End(this.plugin, this).runTaskTimer(this.plugin, 0, 20);
		} else if (force) {
			sendEndingMessages((GameTeam) winner);
			getTeams().clear();
			getTeams().add((GameTeam) winner);
			setState(Game.GameState.ENDING);
			Run.getInstance().setTime(this.plugin.getDataHandler().getGameInfo().getInt("events.ENDING"));
			ended = true;
			new End(this.plugin, this).runTaskTimer(this.plugin, 0, 20);
		}
	}

	public void judge() {
		sendEndingMessages(null);
		getPlayers().clear();
		setState(Game.GameState.ENDING);
		Run.getInstance().setTime(this.plugin.getDataHandler().getGameInfo().getInt("events.ENDING"));
		ended = true;
		new End(this.plugin, this).runTaskTimer(this.plugin, 0, 20);
	}

	public void reward(GamePlayer player, boolean won) {
		YamlConfiguration data = player.getData();
		if (won) {
			data.set("bedwars.wins", data.getInt("bedwars.wins") + 1);
			data.set("point", data.getInt("point") + 3);
			player.sendMessage(ChatColor.GREEN + "+3" + (me.naptie.bukkit.player.utils.ConfigManager.getLanguageName(player.getPlayer()).contains("en") ? Messages.getMessage(player, "ADD_POINTS").replace("%s%", "") : Messages.getMessage(player, "ADD_POINTS")));
		} else
			data.set("bedwars.deaths", data.getInt("bedwars.deaths") + 1);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Inventory generateOptionMenu(GamePlayer player, boolean subTeamMenu, boolean subBedMenu) {
		String language = player.getLanguage();
		if (subTeamMenu) {
			Inventory inventory = Bukkit.createInventory(null, 36, Messages.getMessage(language, "TEAM_MENU_TITLE"));
			int slot = 10;
			for (GameTeam team : this.teams) {
				if (slot == 17 || slot == 18) {
					slot = 19;
				}
				ItemStack item = new ItemStack(Material.valueOf(team.getColor().getDyeColor().name() + "_WOOL"));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(team.getColor().getChatColor() + Messages.translate(org.apache.commons.lang.StringUtils.capitalize(team.getColor().name().toLowerCase()), "en-US", language));
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + Messages.getMessage(language, "PLAYERS") + ": " + getQueuedPlayersAmount(team) + "/" + this.playersPerTeam);
				lore.add(Messages.getMessage(language, getQueuedPlayersAmount(team) < this.playersPerTeam ? "CLICK_TO_JOIN" : "TEAM_FULL"));
				meta.setLore(lore);
				item.setItemMeta(meta);
				inventory.setItem(slot, item);
				slot++;
			}
			return inventory;
		} else if (subBedMenu) {
			Inventory inventory = Bukkit.createInventory(null, 54, Messages.getMessage(language, "BED_MENU_TITLE") + " 1/3");
			int slot = 0;
			for (ItemStack item : BedMaterialUtil.getBedMaterials(player, true, 1)) {
				ItemMeta meta = item.getItemMeta();
				if (BedMaterialUtil.getBedMaterials(player, false).contains(item)) {
					meta.setDisplayName(ChatColor.GREEN + item.getType().name());
					List<String> lore = new ArrayList<>();
					if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
						lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
					} else {
						lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
					}
					lore.add(Messages.getMessage(player, "CLICK_TO_CHOOSE"));
					meta.setLore(lore);
				} else {
					meta.setDisplayName(ChatColor.RED + item.getType().name());
					List<String> lore = new ArrayList<>();
					if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
						lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
					} else {
						lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
					}
					lore.add(Messages.getMessage(player, "HAVENT_REACHED_REQUIREMENT"));
					meta.setLore(lore);
				}
				item.setItemMeta(meta);
				inventory.setItem(slot, item);
				slot++;
			}
			inventory.setItem(53, BedMaterialUtil.getNextPageItem(player.getLanguage()));
			return inventory;
		} else {
			Inventory inventory = Bukkit.createInventory(null, 27, Messages.getMessage(language, "OPTION_MENU_TITLE"));
			ItemStack team = new ItemStack(Material.NOTE_BLOCK);
			ItemMeta teamMeta = team.getItemMeta();
			teamMeta.setDisplayName(ChatColor.GREEN + Messages.getMessage(language, "TEAM_MENU_TITLE"));
			team.setItemMeta(teamMeta);
			ItemStack bed = new ItemStack(Material.RED_BED);
			ItemMeta bedMeta = bed.getItemMeta();
			bedMeta.setDisplayName(ChatColor.GREEN + Messages.getMessage(language, "BED_MENU_TITLE"));
			bed.setItemMeta(bedMeta);
			inventory.setItem(11, team);
			inventory.setItem(15, bed);
			return inventory;
		}
	}

	public int getQueuedPlayersAmount(GameTeam team) {
		int i = 0;
		for (Map.Entry<GamePlayer, GameTeam> entry : this.playerTeamQueue.entrySet()) {
			if (entry.getValue() == team) {
				i++;
			}
		}
		return i;
	}
/*
	private Set<GamePlayer> getQueuedPlayers(GameTeam team) {
		Set<GamePlayer> playerSet = new HashSet<>();
		for (Map.Entry<GamePlayer, GameTeam> entry : this.playerTeamQueue.entrySet()) {
			if (entry.getValue() == team) {
				playerSet.add(entry.getKey());
			}
		}
		return playerSet;
	}*/

	public boolean hasEnded() {
		return this.ended;
	}

	public Set<Generator> getBasicGenerators() {
		return this.basicGenerators;
	}

	public Set<Generator> getDiamondGenerators() {
		return this.diamondGenerators;
	}

	public Set<Generator> getEmeraldGenerators() {
		return this.emeraldGenerators;
	}

	public Map<LivingEntity, Location> getShops() {
		return this.shops;
	}

	public Map<LivingEntity, Location> getUpgrades() {
		return this.upgrades;
	}

	public Set<Entity> getShopEntities() {
		return new HashSet<>(this.shops.keySet());
	}

	public Set<Entity> getUpgradeEntities() {
		return new HashSet<>(this.upgrades.keySet());
	}

	public World getWorld() {
		return this.world;
	}

	public String getWorldName() {
		return this.worldName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getPlayersPerTeam() {
		return this.playersPerTeam;
	}

	public boolean isState(GameState gameState) {
		return this.gameState == gameState;
	}

	public GameState getState() {
		return this.gameState;
	}

	public void setState(GameState gameState) {
		this.gameState = gameState;
	}

	public void setState(int stage) {
		for (GameState gameState : GameState.values()) {
			if (gameState.getStage() == stage)
				this.gameState = gameState;
		}
	}

	public List<GameTeam> getTeams() {
		return this.teams;
	}

	public List<GameTeam> getAliveTeams() {
		List<GameTeam> aliveTeams = new ArrayList<>();
		for (GameTeam team : this.teams) {
			if (team.isAlive())
				aliveTeams.add(team);
		}
		return aliveTeams;
	}

	public Set<GamePlayer> getAllGamePlayers() {
		return this.all;
	}

	public Set<GamePlayer> getPlayers() {
		return this.players;
	}

	public Set<GamePlayer> getSpectators() {
		return this.spectators;
	}

	public GamePlayer getGamePlayer(Player player) {
		for (GamePlayer gamePlayer : this.all) {
			if (gamePlayer.getPlayer() == player)
				return gamePlayer;
		}
		return null;
	}

	public String getModeName() {
		return this.modeName;
	}

	public Map<Player, Objective> getObjectiveMap() {
		return this.objectiveMap;
	}

	public Location getLobby() {
		return this.lobby;
	}

	public Table<UUID, GamePlayer, String> getEntityNameTable() {
		return this.entityNameTable;
	}

	private String center(String input, int size) {
		int count = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '\u00a7')
				count++;
		}
		String centralized = StringUtils.center(input, size + count * 2);
		return centralized.substring(0, centralized.length() - centralized.split(input)[1].length());
	}

	public Set<GameBed> getBeds() {
		return this.beds;
	}

	public void destroyBeds() {
		sendMessage("ALL_BEDS_DESTROYED", new String[]{}, new String[]{});
		for (GamePlayer online : this.all) {
			if (online.getTeam() != null && online.getTeam().getBed() != null)
				if (online.getTeam().getBed().exists()) {
					online.sendTitle(Messages.getMessage(online, "BED_DESTROYED_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(online, "BED_DESTROYED_SUBTITLE"), "\"color\":\"yellow\"", 10, 40, 10);
					online.playSound(Sound.ENTITY_WITHER_DEATH, 1, 1);
				}
		}
		for (GameTeam team : this.teams) {
			team.getBed().destroy();
		}
	}

	public void brokeBed(GameTeam team, GamePlayer breaker) {
		team.getBed().destroy();
		breaker.addBedBroken();
		for (GamePlayer online : this.all) {
			String teamName = Messages.translate(org.apache.commons.lang.StringUtils.capitalize(team.getColor().name().toLowerCase()), "en-US", online.getLanguage());
			online.sendMessage("");
			online.sendMessage(Messages.getMessage(online, "BED_DESTROYED").replace("%team%", team.getColor().getChatColor() + "" + teamName).replace("%player%", breaker.getName()));
			online.sendMessage("");
			if (team.getMembers().contains(online)) {
				online.sendTitle(Messages.getMessage(online, "BED_DESTROYED_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(online, "BED_DESTROYED_TITLE"), "\"color\":\"yellow\"", 10, 40, 10);
				online.playSound(Sound.ENTITY_WITHER_DEATH, 1, 1);
			} else {
				online.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
			}
		}
	}

	public void spawnEnderDragons() {
		for (int i = 0; i < getAliveTeams().size() / 4 + 1; i++) {
			this.world.spawnEntity(new Location(this.world, this.lobby.getX(), this.lobby.getY() - 10, this.lobby.getZ()), EntityType.ENDER_DRAGON);
		}
		sendMessage("ENDER_DRAGONS_SPAWNED", new String[]{"%amount%", "%s%"}, new String[]{getAliveTeams().size() / 4 + 1 + "", getAliveTeams().size() / 4 + 1 == 1 ? "" : "s"});
		playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
	}

	public GameShop getShop() {
		return this.shop;
	}

	public Map<GamePlayer, GameTeam> getPlayerTeamQueue() {
		return playerTeamQueue;
	}

	public GameUpgrade getUpgrade() {
		return upgrade;
	}

	public enum GameState {
		LOBBY(0, 0),
		STARTING(1, 0),
		DIAMOND_1(2, 0),
		EMERALD_1(3, 0),
		DIAMOND_2(4, 0),
		EMERALD_2(5, 0),
		DIAMOND_3(6, 0),
		EMERALD_3(7, 0),
		BED_DESTRUCTION(8, 0),
		DEATHMATCH(9, 0),
		ENDING(10, 0);

		private int stage;
		private int length;

		GameState(int stage, int length) {
			this.stage = stage;
			this.length = length;
		}

		public int getStage() {
			return this.stage;
		}

		public int getLength() {
			return this.length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public int getPeriod() {
			if (this.stage <= 3)
				return 1;
			if (this.stage == 4 || this.stage == 5)
				return 2;
			else
				return 3;
		}
	}
}
