package me.naptie.bukkit.game.bedwars.tasks;

import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Countdown extends BukkitRunnable {

	private static int time = 20;
	private static Countdown instance;
	private Plugin plugin;
	private Game game;
	private Map<UUID, Integer> lastCountdownTime = new HashMap<>();
	private Map<UUID, Integer> lastPlayerAmount = new HashMap<>();
	private Map<UUID, Objective> playerObjectiveMap = new HashMap<>();

	public Countdown(Plugin plugin, Game game) {
		this.plugin = plugin;
		this.game = game;
		instance = this;
	}

	public static Countdown getInstance() {
		return instance;
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = game.getGamePlayer(player);
			UUID uuid = player.getUniqueId();
			if (!playerObjectiveMap.containsKey(uuid)) {
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				Scoreboard board = manager.getNewScoreboard();
				Objective obj = board.registerNewObjective("BedWars", "dummy");
				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
				obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Messages.getMessage(gamePlayer, "BEDWARS").toUpperCase());
				lastCountdownTime.put(uuid, 21);
				lastPlayerAmount.put(uuid, 0);
				player.setScoreboard(board);
				playerObjectiveMap.put(uuid, obj);
			}

			Objective obj = playerObjectiveMap.get(uuid);

			if (game.isState(Game.GameState.STARTING)) {
				obj.getScore(CU.t("&a")).setScore(9);
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "STATE") + ": &a" + Messages.getMessage(gamePlayer, game.getState().name()))).setScore(8);

				if (lastCountdownTime.get(uuid) != time) {
					obj.getScoreboard().resetScores(Messages.getMessage(gamePlayer, "STARTING_IN").replace("%time%", lastCountdownTime.get(uuid) + ""));
				}
				obj.getScore(Messages.getMessage(gamePlayer, "STARTING_IN").replace("%time%", time + "")).setScore(7);
				lastCountdownTime.put(uuid, time);

				if (lastPlayerAmount.get(uuid) != game.getPlayers().size()) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(gamePlayer, "PLAYERS") + ": &a" + lastPlayerAmount.get(uuid) + "&r/" + game.getMaxPlayers()));
				}
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "PLAYERS") + ": &a" + game.getPlayers().size() + "&r/" + game.getMaxPlayers())).setScore(6);
				lastPlayerAmount.put(uuid, game.getPlayers().size());
				obj.getScore(CU.t("&c")).setScore(5);
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "SERVER") + ": &a" + me.naptie.bukkit.core.Main.getInstance().getServerName())).setScore(4);
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "MAP") + ": &a" + game.getWorldName())).setScore(3);
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "MODE") + ": &a" + Messages.getMessage(gamePlayer, game.getModeName()))).setScore(2);
				obj.getScore(CU.t("&4")).setScore(1);
				obj.getScore(Messages.getMessage(gamePlayer, "SCOREBOARD_FOOTER")).setScore(0);
			} else if (game.isState(Game.GameState.LOBBY)) {
				player.setScoreboard(game.getObjectiveMap().get(player).getScoreboard());
			} else {
				player.setScoreboard(Run.board);
			}
		}

		if (game.getPlayers().size() < game.getMinPlayers()) {
			int need = game.getMinPlayers() - game.getPlayers().size();
			cancel();
			time = 21;
			game.sendMessage("COUNTDOWN_CANCELLED", new String[]{}, new String[]{});
			if (need == 1)
				game.sendMessage("NEED_1_TO_START", new String[]{}, new String[]{});
			else
				game.sendMessage("NEED_MORE_TO_START", new String[]{"%need%"}, new String[]{need + ""});
			game.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1, -8);
			game.setState(Game.GameState.LOBBY);
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.setScoreboard(game.getObjectiveMap().get(player).getScoreboard());
			}
		}

		if (time <= 0 && game.getPlayers().size() >= game.getMinPlayers()) {
			cancel();
			new Run(game).runTaskTimer(Main.getInstance(), 0, 20);

		} else if (game.getPlayers().size() >= game.getMinPlayers()) {
			if (time == 20 || time == 15 || time == 10) {
				game.sendTitle(String.valueOf(time), "\"color\":\"gold\"", "", "\"color\":\"white\"", 0, 20, 5);
				game.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
			} else if (time == 5 || time == 4 || time == 3 || time == 2 || time == 1) {
				game.sendTitle(String.valueOf(time), "\"color\":\"red\"", "", "\"color\":\"white\"", 0, 20, 5);
				game.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
			}
		}
		if (time <= 20 && time > 0) {
			game.sendActionBar("GAME_STARTS_IN", new String[]{"%time%", "%s%"}, new String[]{(time > 5 ? "&6" : "&c") + time, (time == 1 ? "" : "s")});
		}
		time -= 1;
	}

	public void forcestart() {
		time = 1;
		new Run(game).runTaskTimer(Main.getInstance(), 0, 20);
	}
}
