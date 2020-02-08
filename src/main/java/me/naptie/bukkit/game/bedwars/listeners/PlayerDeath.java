package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDeath implements Listener {

	public static Map<String, String> deathMessageMap = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent event) {

		event.setDeathMessage(null);
		Player player = event.getEntity();
		Game game = Main.getInstance().getGame();
		if (game != null && game.getGamePlayer(player) != null) {
			GamePlayer gamePlayer = game.getGamePlayer(player);
			if (gamePlayer.getPlayer() == player) {
				handle(event, game);
			}
		}
	}

	private void handle(PlayerDeathEvent event, Game game) {
		Player player = event.getEntity();
		EntityDamageEvent lastDamageCause = player.getLastDamageCause();
		DamageCause dmgCause = lastDamageCause.getCause();

		if (!game.isState(Game.GameState.LOBBY) && !game.isState(Game.GameState.STARTING) && !game.hasEnded() && player.getGameMode() == GameMode.SURVIVAL) {
			GamePlayer gamePlayer = game.getGamePlayer(player);
			Player killer = player.getKiller();

			if (killer != null && !Objects.equals(killer.getUniqueId(), player.getUniqueId())) {
				GamePlayer gameKiller = game.getGamePlayer(killer);
				killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				for (String language : Main.getInstance().getConfig().getStringList("languages")) {
					String deathMessages;
					if (PvP.deadByGettingShot.contains(player)) {
						deathMessages = language + ".death-messages.killer-known.shot";
					} else if (dmgCause == DamageCause.VOID) {
						deathMessages = language + ".death-messages.killer-known.void";
					} else if (dmgCause == DamageCause.FIRE) {
						deathMessages = language + ".death-messages.killer-known.fire";
					} else if (dmgCause == DamageCause.FIRE_TICK) {
						deathMessages = language + ".death-messages.killer-known.fire-tick";
					} else if (dmgCause == DamageCause.LAVA) {
						deathMessages = language + ".death-messages.killer-known.lava";
					} else if (dmgCause == DamageCause.BLOCK_EXPLOSION) {
						deathMessages = language + ".death-messages.killer-known.block-explosion";
					} else if (dmgCause == DamageCause.FALL) {
						deathMessages = language + ".death-messages.killer-known.fall";
					} else if (dmgCause == DamageCause.DROWNING) {
						deathMessages = language + ".death-messages.killer-known.drowning";
					} else {
						deathMessages = language + ".death-messages.killer-known.pvp";
					}
					deathMessageMap.put(language, deathMessages);
				}
				int num = ThreadLocalRandom.current().nextInt(0, Main.getInstance().getConfig().getStringList(deathMessageMap.get("zh-CN")).toArray().length);

				if (gamePlayer.getTeam().getBed().exists()) {
					gameKiller.addKill();
					game.sendDeathMessage(num, game.getGamePlayer(player), game.getGamePlayer(killer), false);
				} else {
					gameKiller.addFinalKill();
					game.sendDeathMessage(num, game.getGamePlayer(player), game.getGamePlayer(killer), true);
				}

			} else {
				for (String language : Main.getInstance().getConfig().getStringList("languages")) {
					String deathMessages;
					if (dmgCause == DamageCause.VOID) {
						deathMessages = language + ".death-messages.killer-unknown.void";
					} else if (dmgCause == DamageCause.FIRE) {
						deathMessages = language + ".death-messages.killer-unknown.fire";
					} else if (dmgCause == DamageCause.FIRE_TICK) {
						deathMessages = language + ".death-messages.killer-unknown.fire-tick";
					} else if (dmgCause == DamageCause.LAVA) {
						deathMessages = language + ".death-messages.killer-unknown.lava";
					} else if (dmgCause == DamageCause.BLOCK_EXPLOSION) {
						deathMessages = language + ".death-messages.killer-unknown.block-explosion";
					} else if (dmgCause == DamageCause.FALL) {
						deathMessages = language + ".death-messages.killer-unknown.fall";
					} else if (dmgCause == DamageCause.DROWNING) {
						deathMessages = language + ".death-messages.killer-unknown.drowning";
					} else {
						deathMessages = language + ".death-messages.killer-unknown.pvp";
					}
					deathMessageMap.put(language, deathMessages);
				}
				int num = ThreadLocalRandom.current().nextInt(0, Main.getInstance().getConfig().getStringList(deathMessageMap.get("zh-CN")).toArray().length);
				if (gamePlayer.getTeam().getBed().exists()) {
					game.sendDeathMessage(num, game.getGamePlayer(player), false);
				} else {
					game.sendDeathMessage(num, game.getGamePlayer(player), true);
				}
			}

			for (int i = 0; i < event.getDrops().size(); i++) {
				ItemStack stack = event.getDrops().get(i);
				if (!(stack.getType() == Material.IRON_INGOT || stack.getType() == Material.GOLD_INGOT || stack.getType() == Material.DIAMOND || stack.getType() == Material.EMERALD || stack.getType().name().endsWith("WOOL"))) {
					event.getDrops().remove(stack);
				}
			}

			if (gamePlayer.getTeam().getBed().exists()) {
				game.respawn(gamePlayer);
				gamePlayer.downgradeAll();
			} else {
				gamePlayer.downgradeAll();
				GameTeam team = gamePlayer.getTeam();
				team.removeMember(gamePlayer);
				game.getPlayers().remove(gamePlayer);
				game.reward(gamePlayer, false);
				game.activateSpectatorSettings(gamePlayer, false);
				if (team.getMembers().size() == 0) {
					for (GamePlayer online : game.getAllGamePlayers()) {
						String teamName = Messages.translate(StringUtils.capitalize(team.getColor().name().toLowerCase()), "en-US", online.getLanguage());
						online.sendMessage("");
						online.sendMessage(Messages.getMessage(online, "TEAM_ELIMINATED").replace("%team%", team.getColor().getChatColor() + "" + teamName));
						online.sendMessage("");
					}
				}
			}

			if (game.getPlayers().size() <= 1 || game.getAliveTeams().size() <= 1) {
				if (!game.getAliveTeams().contains(gamePlayer.getTeam()))
					gamePlayer.sendTitle(Messages.getMessage(gamePlayer, "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true",
							Messages.getMessage(gamePlayer, "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				if (game.getAliveTeams().size() > 0) {
					for (GamePlayer winner : game.getAliveTeams().get(0).getMembers()) {
						game.reward(winner, true);
					}
					game.judge(game.getAliveTeams().get(0), false);
				} else {
					game.judge();
				}
			} else {
				gamePlayer.sendTitle(Messages.getMessage(gamePlayer, "DEATH_TITLE"), "\"color\":\"red\",\"bold\":true",
						Messages.getMessage(gamePlayer, "DEATH_SUBTITLE"), "\"color\":\"gray\"", 0, 90, 20);
			}
		}
	}
}
