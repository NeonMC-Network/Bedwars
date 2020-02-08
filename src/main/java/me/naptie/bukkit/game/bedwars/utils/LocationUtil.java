package me.naptie.bukkit.game.bedwars.utils;

import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.listeners.BlockInteract;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.Generator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocationUtil {

	public static Location parseLocation(World world, String string) {
		double x = Double.parseDouble(string.split(", ")[0]);
		double y = Double.parseDouble(string.split(", ")[1]);
		double z = Double.parseDouble(string.split(", ")[2]);
		return new Location(world, x, y, z);
	}

	/**
	 * Gets <code>target</code>'s direction relative to <code>reference</code>
	 *
	 * @return <code>target</code>'s direction relative to <code>reference</code>
	 */
	public static BlockFace getFacing(Location reference, Location target) {
		if (target.getBlockZ() == reference.getBlockZ()) {
			if (target.getBlockX() - reference.getBlockX() > 0)
				return BlockFace.EAST;
			if (target.getBlockX() - reference.getBlockX() < 0)
				return BlockFace.WEST;
			if (target.getBlockX() - reference.getBlockX() == 0)
				return BlockFace.SELF;
		}
		if (target.getBlockX() == reference.getBlockX()) {
			if (target.getBlockZ() - reference.getBlockZ() > 0)
				return BlockFace.SOUTH;
			if (target.getBlockZ() - reference.getBlockZ() < 0)
				return BlockFace.NORTH;
			if (target.getBlockZ() - reference.getBlockZ() == 0)
				return BlockFace.SELF;
		}
		return BlockFace.SELF;
	}

	public static void breakBlocks(Location location, int radius, boolean safe, Material... protectedMaterials) {
		Game game = Main.getInstance().getGame();
		if (game != null)
			for (int x = -(radius); x <= radius; x++) {
				for (int y = -(radius); y <= radius; y++) {
					for (int z = -(radius); z <= radius; z++) {
						Location loc = new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
						Block block = location.getWorld().getBlockAt(loc);
						boolean shouldContinue = false;
						if (safe) {
							if (!BlockInteract.blocksPlacedByPlayer.contains(block))
								shouldContinue = true;
							for (GameTeam team : game.getTeams()) {
								if (team.getBed().exists()) {
									for (Location location1 : team.getBedLocation()) {
										if (location1.equals(loc)) {
											shouldContinue = true;
											break;
										}
									}
								}
							}
						}
						for (Material material : protectedMaterials) {
							if (block.getType().equals(material))
								shouldContinue = true;
						}
						if (shouldContinue)
							continue;
						location.getWorld().getBlockAt(loc).breakNaturally();
						for (Generator gen : Main.getInstance().getGame().getBasicGenerators())
							if (gen.getLocation().equals(loc))
								gen.stop();
						for (Generator gen : Main.getInstance().getGame().getDiamondGenerators())
							if (gen.getLocation().equals(loc))
								gen.stop();
						for (Generator gen : Main.getInstance().getGame().getEmeraldGenerators())
							if (gen.getLocation().equals(loc))
								gen.stop();
					}
				}
			}
	}

}
