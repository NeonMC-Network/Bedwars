package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;

public class PlayerInteractEntity implements Listener {

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Game game = Main.getInstance().getGame();
		if (game != null) {
			Entity entity = event.getRightClicked();
			GamePlayer player = game.getGamePlayer(event.getPlayer());
			for (LivingEntity shopEntity : game.getShops().keySet()) {
				if (shopEntity.getUniqueId() == entity.getUniqueId()) {
					event.setCancelled(true);
					player.getPlayer().openInventory(game.getShop().getMainCategory().getInventory(player));
				}
			}
			for (LivingEntity upgradeEntity : game.getUpgrades().keySet()) {
				if (upgradeEntity.getUniqueId() == entity.getUniqueId()) {
					event.setCancelled(true);
					Inventory i = Bukkit.createInventory(null, 54, Messages.getMessage(player, game.getPlayersPerTeam() == 1 ? "SOLO_UPGRADE" : "TEAM_UPGRADE"));
					// TODO
					player.getPlayer().openInventory(i);
				}
			}
		}
	}

}
