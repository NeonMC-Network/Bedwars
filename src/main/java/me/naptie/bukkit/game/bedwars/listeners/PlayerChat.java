package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Game game = Main.getInstance().getGame();
		if (game != null) {
			if (game.getState() == Game.GameState.LOBBY || game.getState() == Game.GameState.STARTING)
				return;
			if (game.getPlayersPerTeam() > 1) {
				event.setCancelled(true);
				for (GamePlayer player : game.getGamePlayer(event.getPlayer()).getTeam().getMembers()) {
					player.sendMessage(event.getPlayer().getDisplayName() + ": " + event.getMessage());
				}
			}
		}
	}

}
