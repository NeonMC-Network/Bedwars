package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoin implements Listener {

	private Map<UUID, Integer> lastPlayerAmount = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = new GamePlayer(player);
		Game game;
		try {
			game = Main.getInstance().getGame();
			game.getAllGamePlayers().add(gamePlayer);
			game.registerEntityNames(gamePlayer);
		} catch (Exception e) {
			gamePlayer.sendMessage(Messages.getMessage(gamePlayer, "ERROR_OCCURRED_WHILE_JOINING"));
			gamePlayer.sendToLobby();
			return;
		}
		event.setJoinMessage(null);
		player.setGameMode(GameMode.ADVENTURE);
		player.getInventory().clear();
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		player.setAllowFlight(false);
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			GamePlayer online = game.getGamePlayer(onlinePlayer);
			if (game.isState(Game.GameState.LOBBY)) {
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				Scoreboard board = manager.getNewScoreboard();
				Objective obj = board.registerNewObjective(online.getName(), "dummy");
				if (!game.getObjectiveMap().containsKey(onlinePlayer)) {
					lastPlayerAmount.put(onlinePlayer.getUniqueId(), 0);
					game.getObjectiveMap().put(onlinePlayer, obj);
				}
				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
				obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Messages.getMessage(online, "BEDWARS").toUpperCase());
				player.setScoreboard(board);
				obj.getScore(CU.t("&a")).setScore(9);
				obj.getScore(CU.t(Messages.getMessage(online, "STATE") + ": &a" + Messages.getMessage(online, game.getState().name()))).setScore(8);
				obj.getScore(CU.t("&b")).setScore(7);
				Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
					if (lastPlayerAmount.get(onlinePlayer.getUniqueId()) != Bukkit.getOnlinePlayers().size()) {
						obj.getScoreboard().resetScores(CU.t(Messages.getMessage(online, "PLAYERS") + ": &a" + lastPlayerAmount.get(onlinePlayer.getUniqueId()) + "&r/" + game.getMaxPlayers()));
					}
					obj.getScore(CU.t(Messages.getMessage(online, "PLAYERS") + ": &a" + Bukkit.getOnlinePlayers().size() + "&r/" + game.getMaxPlayers())).setScore(6);
					lastPlayerAmount.put(onlinePlayer.getUniqueId(), Bukkit.getOnlinePlayers().size());
				}, 0, 20);
				obj.getScore(CU.t("&c")).setScore(5);
				obj.getScore(CU.t(Messages.getMessage(online, "SERVER") + ": &a" + me.naptie.bukkit.core.Main.getInstance().getServerName())).setScore(4);
				obj.getScore(CU.t(Messages.getMessage(online, "MAP") + ": &a" + game.getWorldName())).setScore(3);
				obj.getScore(CU.t(Messages.getMessage(gamePlayer, "MODE") + ": &a" + Messages.getMessage(gamePlayer, game.getModeName()))).setScore(2);
				obj.getScore(CU.t("&4")).setScore(1);
				obj.getScore(Messages.getMessage(online, "SCOREBOARD_FOOTER")).setScore(0);
			}
		}
		if (Bukkit.getOnlinePlayers().size() <= 1) {
			game.resetWorld();
		}
		game.join(gamePlayer);
	}

}
