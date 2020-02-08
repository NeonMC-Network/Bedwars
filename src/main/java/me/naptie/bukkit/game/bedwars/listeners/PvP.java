package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PvP implements Listener {

	static List<Player> deadByGettingShot = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event) {
		Game game = Main.getInstance().getGame();

		if (game != null) {

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				if (event.getEntity() instanceof Player) {
					Player player = (Player) event.getEntity();

					if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded() || !game.getGamePlayer(player).isDamageable()) {
						event.setCancelled(true);
						return;
					}

					if (game.getGamePlayer(damager).isSpectating()) {
						event.setCancelled(true);
						return;
					}

					for (GameTeam gameTeam : game.getTeams()) {
						if (gameTeam.getMembers().contains(game.getGamePlayer(player)) && gameTeam.getMembers().contains(game.getGamePlayer(damager))) {
							event.setCancelled(true);
							return;
						}
					}
				} else {
					if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded()) {
						event.setCancelled(true);
						return;
					}

					if (game.getGamePlayer(damager).isSpectating()) {
						event.setCancelled(true);
						return;
					}
				}
			}
			if (event.getDamager() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getDamager();
				if (arrow.getShooter() instanceof Player) {
					Player shooter = (Player) arrow.getShooter();
					Damageable v = (Damageable) event.getEntity();
					if (v instanceof Player) {
						Player player = (Player) v;
						for (GameTeam gameTeam : game.getTeams()) {
							if (gameTeam.getMembers().contains(game.getGamePlayer(player)) && gameTeam.getMembers().contains(game.getGamePlayer(shooter))) {
								event.setCancelled(true);
								return;
							}
						}
						double playerHealth = player.getHealth();
						double klc = event.getFinalDamage();
						if (!v.isDead()) {
							Double lc = playerHealth - (double) (int) klc;
							NumberFormat flc = NumberFormat.getInstance();
							flc.setMaximumFractionDigits(1);
							if (lc.intValue() > 0) {
								shooter.sendMessage(Messages.getMessage(game.getGamePlayer(shooter), "BOW_SHOT").replace("%player%", game.getGamePlayer(player).getName()).replace("%hp%", flc.format(lc)));
							} else {
								deadByGettingShot.add(player);
							}
						} else {
							deadByGettingShot.add(player);
						}
					}
				}
			}

		}
	}
}