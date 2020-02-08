package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import me.naptie.bukkit.game.bedwars.objects.GameTrap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Game game = Main.getInstance().getGame();
		if (game != null) {
			GamePlayer player = game.getGamePlayer(event.getPlayer());
			for (GameTeam team : game.getAliveTeams()) {
				if (isAttacking(player, team)) {
					for (GameTrap trap : team.getTraps()) {
						trap.activate(player);
					}
				}
			}
		}
	}

	private boolean isAttacking(GamePlayer player, GameTeam team) {
		if (player.getTeam() == team || team.getMembers().contains(player)) {
			return false;
		}
		return player.getPlayer().getLocation().distanceSquared(team.getSpawnpoint()) <= 64;
	}

}
