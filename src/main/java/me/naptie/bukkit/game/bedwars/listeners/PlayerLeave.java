package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		event.setQuitMessage(null);

		Game game = Main.getInstance().getGame();
		for (GamePlayer gamePlayer : game.getPlayers()) {
			if (gamePlayer.getPlayer() == player) {
				game.leave(gamePlayer);
				game.getAllGamePlayers().remove(gamePlayer);
				return;
			}
		}
		for (GamePlayer spectator : game.getSpectators()) {
			if (spectator.getPlayer() == player) {
				game.leaveFromSpectating(spectator);
				game.getAllGamePlayers().remove(spectator);
				return;
			}
		}
		game.getPlayerTeamQueue().remove(game.getGamePlayer(player));

	}

}
