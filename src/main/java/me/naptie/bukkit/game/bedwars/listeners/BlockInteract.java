package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockInteract implements Listener {

	public static Set<Block> blocksPlacedByPlayer = new HashSet<>();

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		if (game != null) {
			if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded()) {
				event.setCancelled(true);
				return;
			}

			GamePlayer gamePlayer = game.getGamePlayer(player);
			if (gamePlayer != null) {
				if (game.getSpectators().contains(gamePlayer)) {
					event.setCancelled(true);
				}
				if (!game.getPlayers().contains(gamePlayer)) {
					event.setCancelled(true);
				}
				if (gamePlayer.getTeam().getBed().exists()) {
					for (Location location : gamePlayer.getTeam().getBedLocation()) {
						if (location.equals(event.getBlockPlaced().getLocation())) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
		if (!event.isCancelled()) {
			if (event.getBlockPlaced().getType() == Material.TNT) {
				event.getBlockPlaced().setType(Material.AIR);
				Location location = event.getBlockPlaced().getLocation().clone();
				location.add(0.5, 0, 0.5);
				location.getWorld().spawn(location, TNTPrimed.class);
			} else {
				blocksPlacedByPlayer.add(event.getBlockPlaced());
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		if (game != null) {
			if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.hasEnded()) {
				event.setCancelled(true);
				return;
			}

			GamePlayer gamePlayer = game.getGamePlayer(player);
			if (gamePlayer != null) {
				if (game.getSpectators().contains(gamePlayer)) {
					event.setCancelled(true);
					return;
				}
				if (!game.getPlayers().contains(gamePlayer)) {
					event.setCancelled(true);
					return;
				}
				for (Location location : gamePlayer.getTeam().getBedLocation()) {
					if (gamePlayer.getTeam().getBed().exists() && event.getBlock().getLocation().equals(location)) {
						event.setCancelled(true);
						gamePlayer.sendMessage(Messages.getMessage(gamePlayer, "BROKEN_BLOCK_IS_OWN_BED"));
						return;
					}
				}
				boolean cancel = true;
				for (Block block : blocksPlacedByPlayer) {
					if (event.getBlock().getLocation().equals(block.getLocation())) {
						cancel = false;
						for (GameTeam team : game.getTeams()) {
							for (Location loc : team.getBedLocation()) {
								if (team.getBed().exists() && event.getBlock().getLocation().equals(loc)) {
									event.setCancelled(true);
									game.brokeBed(team, gamePlayer);
								}
							}
							team.getBed().validate();
						}
					}
				}
				if (game.isState(Game.GameState.ENDING))
					cancel = false;
				if (game.hasEnded())
					cancel = true;
				event.setCancelled(cancel);
				if (cancel)
					gamePlayer.sendMessage(Messages.getMessage(gamePlayer, "BROKEN_BLOCK_NOT_VALID"));
			}
		}
	}

}
