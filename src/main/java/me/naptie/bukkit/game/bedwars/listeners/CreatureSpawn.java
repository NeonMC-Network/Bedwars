package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawn implements Listener {

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		LivingEntity entity = event.getEntity();
		Game game = Main.getInstance().getGame();
		if (game != null && game.getShopEntities() != null && game.getUpgradeEntities() != null && entity.getCustomName() != null && !entity.getCustomName().equalsIgnoreCase("") && !entity.getCustomName().equalsIgnoreCase("null"))
			if (game.getShopEntities().contains(entity) || game.getUpgradeEntities().contains(entity)) {
				for (GamePlayer gamePlayer : game.getAllGamePlayers())
					game.getEntityNameTable().put(entity.getUniqueId(), gamePlayer, Messages.getMessage(gamePlayer, entity.getCustomName()));
				entity.setCustomNameVisible(true);
			}
		event.setCancelled(false);
	}

}
