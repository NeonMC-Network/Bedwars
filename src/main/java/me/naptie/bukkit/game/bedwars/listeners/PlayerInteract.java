package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import me.naptie.bukkit.inventory.utils.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerInteract implements Listener {

	public static Map<Location, GameTeam> queue = new HashMap<>();
	private Map<UUID, GameTeam> uuidGameTeamMap = new HashMap<>();

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		assert game != null;
		GamePlayer gamePlayer = game.getGamePlayer(player);
		if (gamePlayer != null && gamePlayer.getPlayer() != null) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (player.getInventory().getItemInMainHand().getType() == Material.POLAR_BEAR_SPAWN_EGG) {
					queue.put(Objects.requireNonNull(event.getClickedBlock()).getLocation(), gamePlayer.getTeam());
					IronGolem i = (IronGolem) Objects.requireNonNull(player.getLocation().getWorld()).spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
					Location location = i.getLocation().clone();
					location.setY(i.getLocation().getY() - 1);
					for (Location queued : PlayerInteract.queue.keySet()) {
						if (queued.equals(location)) {
							uuidGameTeamMap.put(i.getUniqueId(), PlayerInteract.queue.get(queued));
						}
					}
					//List<Player> nearPlayers = getNearbyPlayers(i, 16);
					//int index = (int) Math.round(Math.rint(nearPlayers.size() - 1));
					// i.damage(0, nearPlayers.get(index));
					// i.setTarget(nearPlayers.get(index));
					i.setPlayerCreated(false);
					for (GamePlayer enemy : Main.getInstance().getGame().getPlayers()) {
						if (!uuidGameTeamMap.get(i.getUniqueId()).getMembers().contains(enemy)) {
							i.damage(0, enemy.getPlayer());
							i.setTarget(enemy.getPlayer());
							break;
						}
					}
				}
			}
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (player.getInventory().getItemInMainHand().getType() == Material.FIRE_CHARGE) {
					Fireball fireball = player.launchProjectile(Fireball.class);
					fireball.setVelocity(fireball.getVelocity().multiply(3));
					for (Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(Material.FIRE_CHARGE).entrySet()) {
						if (entry.getValue().getType() == Material.FIRE_CHARGE) {
							if (entry.getValue().getAmount() > 1) {
								int amount = player.getInventory().getItem(entry.getKey()).getAmount();
								player.getInventory().getItem(entry.getKey()).setAmount(amount - 1);
							} else {
								player.getInventory().clear(entry.getKey());
							}
							break;
						}
					}
					player.updateInventory();
				}
				if (ConfigManager.isMatching(player.getInventory().getItemInMainHand(), ConfigManager.getItem(player, "leave"))) {
					if (gamePlayer.isSpectating()) {
						game.leaveFromSpectating(gamePlayer);
					} else {
						game.leave(gamePlayer);
					}
					player.sendMessage(Messages.getMessage(gamePlayer, "QUIT_GAME"));
					return;
				} else if (gamePlayer.isSpectating()) {
					event.setCancelled(true);
					return;
				}
				if (ConfigManager.isMatching(player.getInventory().getItemInMainHand(), ConfigManager.getItem(player, "options"))) {
					player.openInventory(game.generateOptionMenu(gamePlayer, false, false));
				}
			}
		}
	}
}