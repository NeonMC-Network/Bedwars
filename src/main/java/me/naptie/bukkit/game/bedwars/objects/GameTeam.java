package me.naptie.bukkit.game.bedwars.objects;

import me.naptie.bukkit.game.bedwars.tasks.Run;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.player.utils.ConfigManager;
import org.bukkit.*;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameTeam {

	private Set<GamePlayer> members;
	private TeamColor color;
	private Location spawnpoint;
	private List<Location> bedLocation;
	private GameBed bed;
	private Team team;
	private String character;
	private String prefix;
	private GameBed.BedMaterial material;
	private Generator generator;
	private List<GameTrap> traps;

	public GameTeam(TeamColor color) {
		this.color = color;
		this.members = new HashSet<>();
		this.character = this.color.equals(TeamColor.GRAY) ? "S" : this.color.name().substring(0, 1);
		this.prefix = this.color.chatColor + "[" + character + "] " + this.color.chatColor;
		this.material = new GameBed.BedMaterial(Material.valueOf(color.getDyeColor().name() + "_BED"), (short) 0);
		System.out.println("Registered " + color.name() + " with BedMaterial " + this.material.getMaterial().name());
	}

	public void registerScoreboardTeam() {
		this.team = Run.board.registerNewTeam(character);
		this.team.setPrefix(this.prefix);
		this.team.setSuffix(CU.t("&r"));
		this.team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
		this.team.setDisplayName(getName());
		this.team.setAllowFriendlyFire(false);
		this.team.setCanSeeFriendlyInvisibles(true);
		for (GamePlayer player : this.members) {
			this.team.addEntry(player.getPlayer().getName());
			player.getPlayer().setPlayerListName(this.prefix + player.getPlayer().getName());
			player.setDisplayName(this.prefix + player.getPlayer().getName() + CU.t("&r"));
		}
	}

	public void addMember(GamePlayer player) {
		player.setTeam(this);
		if (this.members.size() == 0) {
			setBedMaterial(player.getBedMaterial());
		} else {
			for (GamePlayer member : this.members) {
				if (ConfigManager.getLevelInt(player.getPlayer()) >= ConfigManager.getLevelInt(member.getPlayer())) {
					setBedMaterial(player.getBedMaterial());
				}
			}
		}
		this.members.add(player);
	}

	public Set<GamePlayer> getMembers() {
		return this.members;
	}

	public void removeMember(GamePlayer player) {
		player.setTeam(null);
		this.members.remove(player);
		this.team.removeEntry(player.getPlayer().getName());
		player.getPlayer().setPlayerListName(player.getPlayer().getDisplayName());
		player.setDisplayName(player.getPlayer().getDisplayName());
	}

	public TeamColor getColor() {
		return this.color;
	}

	public void sendMessage(String message) {
		for (GamePlayer player : this.members) {
			player.sendMessage(message);
		}
	}

	public void sendTitle(String titleText, String titleProperties, String subtitleText, String subtitleProperties, int titleFadeIn, int titleStay, int titleFadeOut) {
		for (GamePlayer player : this.members) {
			player.sendTitle(titleText, titleProperties, subtitleText, subtitleProperties, titleFadeIn, titleStay, titleFadeOut);
		}
	}

	public void sendActionBar(String message) {
		for (GamePlayer player : this.members) {
			player.sendActionBar(message);
		}
	}

	public String getName() {
		String name = "";
		for (GamePlayer gamePlayer : this.members) {
			if (name.equals("")) {
				if (!gamePlayer.isSpectating())
					name = gamePlayer.getName();
				else
					name = CU.t("&m" + gamePlayer.getName());
			} else {
				if (!gamePlayer.isSpectating())
					name = CU.t(name + "&7, " + gamePlayer.getName());
				else
					name = CU.t(name + "&7, &m" + gamePlayer.getName());
			}
		}
		return name;
	}

	public List<Location> getBedLocation() {
		return this.bedLocation;
	}

	public void setBedLocation(List<Location> bedLocation) {
		this.bedLocation = bedLocation;
	}

	public Location getSpawnpoint() {
		return spawnpoint;
	}

	public void setSpawnpoint(Location spawnpoint) {
		this.spawnpoint = spawnpoint;
	}

	public String getCharacter() {
		return this.character;
	}

	public GameBed getBed() {
		return this.bed;
	}

	void setBed(GameBed bed) {
		this.bed = bed;
	}

	public boolean isAlive() {
		return this.members.size() > 0;
	}

	public GameBed.BedMaterial getBedMaterial() {
		return this.material;
	}

	public void setBedMaterial(GameBed.BedMaterial material) {
		if (Tag.BEDS.isTagged(material.getMaterial())) {
			this.material = new GameBed.BedMaterial(Material.valueOf(this.color.getDyeColor().name() + "_BED"), (short) 0);
		} else {
			this.material = material;
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}

	public List<GameTrap> getTraps() {
		return traps;
	}

	public void addTrap(GameTrap trap) {
		this.traps.add(trap);
	}

	public void activateTrap(GamePlayer enemy) {

	}

	public enum TeamColor {
		RED(DyeColor.RED, ChatColor.RED),
		YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
		GREEN(DyeColor.LIME, ChatColor.GREEN),
		BLUE(DyeColor.BLUE, ChatColor.BLUE),
		AQUA(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
		PINK(DyeColor.PINK, ChatColor.LIGHT_PURPLE),
		WHITE(DyeColor.WHITE, ChatColor.WHITE),
		GRAY(DyeColor.GRAY, ChatColor.DARK_GRAY);

		private DyeColor dyeColor;
		private ChatColor chatColor;

		TeamColor(DyeColor dyeColor, ChatColor chatColor) {
			this.dyeColor = dyeColor;
			this.chatColor = chatColor;
		}

		public Color getBukkitColor() {
			return this.dyeColor.getColor();
		}

		public DyeColor getDyeColor() {
			return this.dyeColor;
		}

		public ChatColor getChatColor() {
			return this.chatColor;
		}
	}
}
