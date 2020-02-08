package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.utils.LocationUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplode implements Listener {

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
		LocationUtil.breakBlocks(event.getLocation(), 2, true, Material.BARRIER, Material.BEDROCK, Material.OBSIDIAN, Material.GLASS, Material.END_STONE,
				Material.BLACK_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.BROWN_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
				Material.GREEN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
				Material.ORANGE_STAINED_GLASS, Material.PINK_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.WHITE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS);
		/*
		for (int i = 0; i < event.blockList().size(); i++) {
			Block block = event.blockList().get(i);
			System.out.println(event.getEventName() + ": Processing " + block.getType().name() + "! ----- WATCH ME PROCESS!");
			if (block.getType() == Material.BED_BLOCK) {
				event.blockList().remove(block);
				System.out.println(event.getEventName() + ": Removed BED_BLOCK from blockList()!");
			}
			if (!BlockInteract.blocksPlacedByPlayer.contains(block)) {
				event.blockList().remove(block);
				System.out.println(event.getEventName() + ": Removed " + block.getType().name() + " from blockList()!");
			}
		}
		*/
	}

}
