package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItem implements Listener {

	public PlayerPickupItem() {
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		GamePlayer gamePlayer = game.getGamePlayer(player);
		if (gamePlayer.isSpectating()) {
			event.setCancelled(true);
		}
		if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING)) {
			event.setCancelled(true);
		}
	}
}