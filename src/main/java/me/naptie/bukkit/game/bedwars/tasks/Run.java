package me.naptie.bukkit.game.bedwars.tasks;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.naptie.bukkit.game.bedwars.objects.*;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.utils.MathUtil;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.*;

public class Run extends BukkitRunnable {

	public static Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
	public static Map<UUID, Objective> playerObjectiveMap = new HashMap<>();
	public static Map<UUID, Integer> killCount = new HashMap<>(), finalKillCount = new HashMap<>(), totalKillCount = new HashMap<>();
	private static Map<UUID, Integer> bedBrokenCount = new HashMap<>();
	private static Run instance;
	private Game game;
	private int time = 0;
	private String mode;
	private Table<UUID, Integer, String[]> entryTable = HashBasedTable.create();
	private Table<UUID, Integer, Team> teamTable = HashBasedTable.create();

	public Run(Game game) {
		instance = this;
		this.game = game;
		this.game.setState(Game.GameState.DIAMOND_1);
		this.game.sendMessage("GAME_STARTED_MESSAGE", new String[]{}, new String[]{});
		this.game.assignTeams();
		this.game.registerBeds();
		this.game.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
		this.mode = game.getModeName();

		for (GameBed bed : this.game.getBeds()) {
			if (bed.getTeam().isAlive())
				bed.spawn();
		}
		for (GamePlayer player : game.getPlayers()) {
			player.giveGameItems();
			killCount.put(player.getPlayer().getUniqueId(), 0);
			finalKillCount.put(player.getPlayer().getUniqueId(), 0);
			totalKillCount.put(player.getPlayer().getUniqueId(), 0);
			bedBrokenCount.put(player.getPlayer().getUniqueId(), 0);
		}
		for (GameTeam team : game.getTeams()) {
			team.registerScoreboardTeam();
		}
		for (GamePlayer player : game.getAllGamePlayers()) {
			setupScoreboard(player, "DIAMOND_1");
		}
	}

	public static Run getInstance() {
		return instance;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public void run() {
		if (game.getState().getLength() - time < 0) {
			if (game.getState().getStage() > 1 && game.getState().getStage() < 8) {
				for (GamePlayer player : game.getAllGamePlayers()) {
					player.sendMessage(Messages.getMessage(player, "GENERATORS_UPGRADED").replace("%type%", Messages.getMessage(player, game.getState().name().split("_")[0] + "_0")).replace("%tier%", MathUtil.toRoman(Integer.parseInt(game.getState().name().split("_")[1]))));
				}
			}
			if (game.isState(Game.GameState.BED_DESTRUCTION)) {
				game.destroyBeds();
			}
			if (game.isState(Game.GameState.DEATHMATCH)) {
				game.spawnEnderDragons();
			}
			if (game.isState(Game.GameState.ENDING)) {
				cancel();
				if (game.hasEnded())
					return;
				else {
					game.judge();
					return;
				}
			}
			game.setState(game.getState().getStage() + 1);
			for (GamePlayer player : game.getAllGamePlayers()) {
				game.registerEntityNames(player);
			}
		}
		String state = game.getState().name();

		if (time == 0) {
			for (GamePlayer player : game.getPlayers())
				player.sendTitle(Messages.getMessage(player, "BEDWARS"), "\"color\":\"green\",\"bold\":true", Messages.getMessage(player, "STARTED"), "\"color\":\"white\"", 10, 80, 10);
			game.sendActionBar("GAME_STARTED", new String[]{}, new String[]{});
			game.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
			game.playSound(Sound.BLOCK_PORTAL_TRIGGER, 1, 1);
			for (Generator generator : game.getBasicGenerators())
				generator.start();
			for (Generator generator : game.getDiamondGenerators())
				generator.start();
			for (Generator generator : game.getEmeraldGenerators())
				generator.start();
		}

		for (GamePlayer player : game.getAllGamePlayers()) {
			UUID uuid = player.getPlayer().getUniqueId();

			if (!playerObjectiveMap.containsKey(uuid)) {
				setupScoreboard(player, state);
			}

			this.teamTable.get(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 7 : game.getTeams().size() + 10).setSuffix(CU.t(" " + getCurrentFormattedDate(Main.getInstance().getConfig().getString(player.getLanguage() + ".time-format"))));
			this.teamTable.get(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 5 : game.getTeams().size() + 8).setSuffix(CU.t(Messages.getMessage(player, state) + "&f:"));
			this.teamTable.get(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 4 : game.getTeams().size() + 7).setSuffix(CU.t(getFormattedTime(game.getState().getLength() - time)));

			Map<Integer, String> teams = new HashMap<>();
			if (game.getTeams().size() > 4) {
				for (int i = game.getTeams().size() + 2; i > 2; i--) {
					int num = game.getTeams().size() - (i - 2);
					teams.put(i, CU.t((game.getTeams().get(num).getBed().exists() ? "&a&l\u2713" : (game.getTeams().get(num).getMembers().size() == 0 ? "&c&l\u2717" : "&a" + game.getTeams().get(num).getMembers().size()))) + (game.getTeams().get(num).getMembers().contains(player) ? " " + Messages.getMessage(player, "YOU") : ""));
				}
			} else {
				for (int i = game.getTeams().size() + 5; i > 5; i--) {
					int num = game.getTeams().size() - (i - 5);
					teams.put(i, CU.t((game.getTeams().get(num).getBed().exists() ? "&a&l\u2713" : (game.getTeams().get(num).getMembers().size() == 0 ? "&c&l\u2717" : "&a" + game.getTeams().get(num).getMembers().size()))) + (game.getTeams().get(num).getMembers().contains(player) ? " " + Messages.getMessage(player, "YOU") : ""));
				}
			}
			if (game.getTeams().size() > 4) {
				for (int i = game.getTeams().size() + 2; i > 2; i--) {
					this.teamTable.get(uuid, i).setSuffix(teams.get(i));
				}
			} else {
				for (int i = game.getTeams().size() + 5; i > 5; i--) {
					this.teamTable.get(uuid, i).setSuffix(teams.get(i));
				}

				this.teamTable.get(uuid, 4).setSuffix(CU.t(killCount.get(uuid) + ""));
				this.teamTable.get(uuid, 3).setSuffix(CU.t(finalKillCount.get(uuid) + ""));
				this.teamTable.get(uuid, 2).setSuffix(CU.t(bedBrokenCount.get(uuid) + ""));
			}

		}

		time++;
	}

	private void setupScoreboard(GamePlayer player, String state) {
		UUID uuid = player.getPlayer().getUniqueId();
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = board.registerNewObjective("BedWars", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Messages.getMessage(player, "BEDWARS").toUpperCase());
		player.getPlayer().setScoreboard(board);
		playerObjectiveMap.put(uuid, obj);
		Objective objective = board.registerNewObjective("Health", "health");
		objective.setDisplayName(CU.t("&c\u2764"));
		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		for (Player player1 : Bukkit.getOnlinePlayers())
			objective.getScore(player1.getName()).setScore((int) player1.getHealth());

		entryTable.put(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 7 : game.getTeams().size() + 10, new String[]{CU.t("&7" + (game.getMaxPlayers() / game.getPlayersPerTeam() > 4 ? Messages.getMessage(player, this.mode) : this.mode)), CU.t(" " + getCurrentFormattedDate(Main.getInstance().getConfig().getString(player.getLanguage() + ".time-format")))});
		entryTable.put(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 6 : game.getTeams().size() + 9, new String[]{CU.t("&8"), ""});
		entryTable.put(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 5 : game.getTeams().size() + 8, new String[]{CU.t("&f"), CU.t(Messages.getMessage(player, state) + "&f:")});
		entryTable.put(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 4 : game.getTeams().size() + 7, new String[]{CU.t("&a"), CU.t(getFormattedTime(game.getState().getLength() - time))});
		entryTable.put(uuid, game.getTeams().size() > 4 ? game.getTeams().size() + 3 : game.getTeams().size() + 6, new String[]{CU.t("&b"), ""});

		Map<Integer, String[]> teams = new HashMap<>();
		if (game.getTeams().size() > 4) {
			for (int i = game.getTeams().size() + 2; i > 2; i--) {
				int num = game.getTeams().size() - (i - 2);
				teams.put(i, new String[]{game.getTeams().get(num).getColor().getChatColor() + CU.t("&l") + game.getTeams().get(num).getCharacter() + CU.t(" &r&f" + Messages.translate(StringUtils.capitalize(game.getTeams().get(num).getColor().name().toLowerCase()), "en-US", player.getLanguage()) + ": "), CU.t((game.getTeams().get(num).getBed().exists() ? "&a&l\u2713" : (game.getTeams().get(num).getMembers().size() == 0 ? "&c&l\u2717" : "&a" + game.getTeams().get(num).getMembers().size()))) + (game.getTeams().get(num).getMembers().contains(player) ? " " + Messages.getMessage(player, "YOU") : "")});
			}
		} else {
			for (int i = game.getTeams().size() + 5; i > 5; i--) {
				int num = game.getTeams().size() - (i - 5);
				teams.put(i, new String[]{game.getTeams().get(num).getColor().getChatColor() + CU.t("&l") + game.getTeams().get(num).getCharacter() + CU.t(" &r&f" + Messages.translate(StringUtils.capitalize(game.getTeams().get(num).getColor().name().toLowerCase()), "en-US", player.getLanguage()) + ": "), CU.t((game.getTeams().get(num).getBed().exists() ? "&a&l\u2713" : (game.getTeams().get(num).getMembers().size() == 0 ? "&c&l\u2717" : "&a" + game.getTeams().get(num).getMembers().size()))) + (game.getTeams().get(num).getMembers().contains(player) ? " " + Messages.getMessage(player, "YOU") : "")});
			}
		}
		if (game.getTeams().size() > 4) {
			for (int i = game.getTeams().size() + 2; i > 2; i--) {
				entryTable.put(uuid, i, teams.get(i));
			}
		} else {
			for (int i = game.getTeams().size() + 5; i > 5; i--) {
				entryTable.put(uuid, i, teams.get(i));
			}

			entryTable.put(uuid, 5, new String[]{CU.t("&0"), ""});
			entryTable.put(uuid, 4, new String[]{CU.t(Messages.getMessage(player, "KILLS") + ": &a"), CU.t(killCount.get(uuid) + "")});
			entryTable.put(uuid, 3, new String[]{CU.t(Messages.getMessage(player, "FINAL_KILLS") + ": &a"), CU.t(finalKillCount.get(uuid) + "")});
			entryTable.put(uuid, 2, new String[]{CU.t(Messages.getMessage(player, "BEDS_BROKEN") + ": &a"), CU.t(bedBrokenCount.get(uuid) + "")});
		}
		entryTable.put(uuid, 1, new String[]{CU.t("&1"), ""});
		entryTable.put(uuid, 0, new String[]{Messages.getMessage(player, "SCOREBOARD_FOOTER"), ""});

		for (int i = 0; i < entryTable.columnKeySet().size(); i++) {
			this.teamTable.put(uuid, i, board.registerNewTeam("t" + i));
		}

		for (int i : entryTable.columnKeySet()) {
			this.teamTable.get(uuid, i).addEntry(entryTable.get(uuid, i)[0]);
			this.teamTable.get(uuid, i).setPrefix("");
			this.teamTable.get(uuid, i).setSuffix(entryTable.get(uuid, i)[1]);
			obj.getScore(entryTable.get(uuid, i)[0]).setScore(i);
		}
	}

	private String getCurrentFormattedDate(String format) {
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat(format);
		return ft.format(date);
	}

	private String getFormattedTime(int time) {
		int minutes = (time % 3600) / 60;
		int seconds = time % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}
}
