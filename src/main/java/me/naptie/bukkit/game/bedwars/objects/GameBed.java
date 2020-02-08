package me.naptie.bukkit.game.bedwars.objects;

import me.naptie.bukkit.game.bedwars.listeners.BlockInteract;
import me.naptie.bukkit.game.bedwars.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameBed {

	private Game game;
	private GameTeam team;
	private List<Location> locations;
	private BedMaterial material;
	private boolean valid;

	public GameBed(Game game, GameTeam team, BedMaterial material) {
		this.game = game;
		this.team = team;
		this.locations = team.getBedLocation();
		this.material = material;
		this.valid = false;
		team.setBed(this);
	}

	public void spawn() {
		Set<Block> blocks = new HashSet<>();
		if (Tag.BEDS.isTagged(this.material.getMaterial())) {
			BlockFace facing = LocationUtil.getFacing(locations.get(0), locations.get(1));
			Block bedHeadBlock = locations.get(1).getBlock();
			System.out.println(material.getMaterial().name());
			blocks.addAll(setBed(bedHeadBlock, facing, material.getMaterial()));
		} else {
			for (Location location : locations) {
				location.getBlock().setType(this.material.getMaterial());
				blocks.add(location.getBlock());
			}
		}
		BlockInteract.blocksPlacedByPlayer.addAll(blocks);
		this.valid = true;
	}

	public void validate() {
		boolean needRespawning = false;
		for (Location location : this.locations) {
			if (this.valid && location.getBlock().getType() != this.material.getMaterial()) {
				needRespawning = true;
			}
		}
		if (needRespawning)
			if (Tag.BEDS.isTagged(this.material.getMaterial())) {
				BlockFace facing = LocationUtil.getFacing(locations.get(0), locations.get(1));
				Block bedHeadBlock = locations.get(1).getBlock();
				setBed(bedHeadBlock, facing, material.getMaterial());
			} else {
				for (Location location : locations) {
					location.getBlock().setType(this.material.getMaterial());
				}
			}
	}

	public Set<Block> setBed(Block head, BlockFace facing, Material material) {
		Set<Block> bedBlocks = new HashSet<>();
		for (Bed.Part part : Bed.Part.values()) {
			bedBlocks.add(head);
			head.setBlockData(Bukkit.createBlockData(material, (data) -> {
				((Bed) data).setPart(part);
				((Bed) data).setFacing(facing);
			}));
			head = head.getRelative(facing.getOppositeFace());
		}
		return bedBlocks;
	}

	public boolean exists() {
		return this.valid;
	}

	public GameTeam getTeam() {
		return this.team;
	}

	public Game getGame() {
		return this.game;
	}

	public void setMaterial(BedMaterial material) {
		this.material = material;
	}

	public void destroy() {
		for (Location location : this.locations) {
			location.getBlock().setType(Material.AIR);
		}
		this.valid = false;
	}

	public static class BedMaterial {

		private Material material;
		private short data;

		public BedMaterial(Material material, short data) {
			this.material = material;
			this.data = data;
		}

		public void setBedMaterial(Material material, short data) {
			this.material = material;
			this.data = data;
		}

		public Material getMaterial() {
			return this.material;
		}

		public short getData() {
			return this.data;
		}
	}
}
