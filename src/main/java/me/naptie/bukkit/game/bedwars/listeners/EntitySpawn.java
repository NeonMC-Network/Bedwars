package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.entity.EntityType.*;

public class EntitySpawn implements Listener {

    private List<EntityType> allowedTypes = Arrays.asList(
    		ARMOR_STAND, VILLAGER, IRON_GOLEM, SILVERFISH,
            DROPPED_ITEM, ENDER_DRAGON, PLAYER, PRIMED_TNT,
            EXPERIENCE_ORB, EGG, ARROW, SNOWBALL, FIREBALL,
            SMALL_FIREBALL, ENDER_PEARL, SPLASH_POTION,
            THROWN_EXP_BOTTLE, FALLING_BLOCK, FIREWORK,
            DRAGON_FIREBALL, BOAT, LIGHTNING);

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (!allowedTypes.contains(event.getEntityType())) {
            event.setCancelled(true);
        }
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) event.getEntity();
            dragon.setAI(true);
            dragon.setCollidable(true);
            dragon.setInvulnerable(false);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> LocationUtil.breakBlocks(event.getLocation(), 3, false, Material.BARRIER, Material.BEDROCK), 100, 5);
        }
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            Item item = (Item) event.getEntity();
            if (item.getItemStack().getType().name().contains("HELMET") || item.getItemStack().getType().name().contains("CHESTPLATE") || item.getItemStack().getType().name().contains("LEGGINGS") || item.getItemStack().getType().name().contains("BOOTS"))
                event.setCancelled(true);
        }
    }
/*
	@SuppressWarnings("SameParameterValue")
	private List<Player> getNearbyPlayers(IronGolem ironGolem, double max) {
		Location source = ironGolem.getLocation();
		List<Player> players = new ArrayList<>();
		for(GamePlayer gamePlayer : Main.getInstance().getGame().getPlayers()) {
			Player p = gamePlayer.getPlayer();
			double distance = Math.sqrt(
					Math.pow((p.getLocation().getX() - source.getX()), 2) + // x coordinate
							Math.pow((p.getLocation().getY() - source.getY()), 2) + // y coordinate
							Math.pow((p.getLocation().getZ() - source.getZ()), 2)  // z coordinate
			);
			if(distance <= max && !uuidGameTeamMap.get(ironGolem.getUniqueId()).getMembers().contains(gamePlayer)) {
				players.add(p);
			}
		}
		return players;
	}
*/
}
