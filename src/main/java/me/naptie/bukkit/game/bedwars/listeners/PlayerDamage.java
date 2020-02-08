package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.Game.GameState;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDamage implements Listener {

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Game game = Main.getInstance().getGame();

			if (game != null) {

				if (!(game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded()) || !game.getGamePlayer(player).isDamageable()) {
					event.setCancelled(false);
				}

				if (player.getKiller() != null) {
					GamePlayer gameKiller = game.getGamePlayer(player.getKiller());
					if (gameKiller.isTeamMate(game.getGamePlayer(player))) {
						event.setCancelled(true);
					}
				}

				if (game.isState(GameState.LOBBY) || game.isState(GameState.STARTING) || game.hasEnded()) {
					event.setCancelled(true);
				}

				if (game.getGamePlayer(player).isSpectating()) {
					event.setCancelled(true);
				}

				if (!game.getGamePlayer(player).isSpectating() && player.getLocation().getY() < 0 && !(game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded())) {

					for (PotionEffect potionEffect : player.getActivePotionEffects()) {
						player.removePotionEffect(potionEffect.getType());
					}
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
					player.teleport(new Location(game.getWorld(), game.getLobby().getX(),
							game.getLobby().getY() - 5, game.getLobby().getZ()));
					player.setAllowFlight(true);
					player.setFlying(true);
					event.setDamage(player.getMaxHealth() + (player.hasPotionEffect(PotionEffectType.ABSORPTION) ? 4 : 0));

				} else if (!(game.getGamePlayer(player).isSpectating()) && player.getLocation().getY() < 0 && (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded())) {

					player.teleport(game.getLobby());

				} else if (game.getGamePlayer(player).isSpectating() && player.getLocation().getY() < 0) {
					player.teleport(new Location(game.getLobby().getWorld(), game.getLobby().getX(),
							game.getLobby().getY() - 5, game.getLobby().getZ()));
				}
			}
		}
	}

}