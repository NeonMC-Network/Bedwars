package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunch implements Listener {

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (Main.getInstance().getGame() != null && event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof Egg) {
			Egg egg = (Egg) event.getEntity();
			GamePlayer player = Main.getInstance().getGame().getGamePlayer((Player) egg.getShooter());
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
				private Location location;

				@Override
				public void run() {
					if (location == null)
						location = new Location(egg.getLocation().getWorld(), egg.getLocation().getX(), egg.getLocation().getY() - 2, egg.getLocation().getZ());
					for (int x = 0; x <= 1; x++) {
							for (int z = 0; z <= 1; z++) {
								Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);
								if (block.getType() == Material.AIR) {
									block.setType(Material.valueOf(player.getTeam().getColor().getDyeColor().name() + "_WOOL"));
									BlockInteract.blocksPlacedByPlayer.add(block);
								}
							}
					}
					location = new Location(egg.getLocation().getWorld(), egg.getLocation().getX(), egg.getLocation().getY() - 2, egg.getLocation().getZ());
				}
			}, 3, 1);
		}
	}

}
