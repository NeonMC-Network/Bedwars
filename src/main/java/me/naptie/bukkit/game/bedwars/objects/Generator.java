package me.naptie.bukkit.game.bedwars.objects;

import me.naptie.bukkit.game.bedwars.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Generator {

	private Main plugin;
	private static Game game;
	private Type type;
	private Location location;
	private double time, delay = -1;
	private int basicItemSpawnedTimes;
	private List<ArmorStand> armorStands = new ArrayList<>(3);
	private GameTeam team;
	private boolean stop;
	private Map<ItemStack, Integer> customItems;

	Generator(Main plugin, Game game, Type type, Location location, GameTeam team) {
		this.plugin = plugin;
		Generator.game = game;
		this.type = type;
		this.location = location;
		this.time = type.period1Delay;
		this.basicItemSpawnedTimes = 0;
		this.stop = false;
		if (type != Type.BASIC)
			spawn();
		else if (team != null) {
			this.team = team;
			this.team.setGenerator(this);
			this.customItems = new HashMap<>();
		}
	}

	private void spawn() {
		Location titleLoc = location.clone();
		titleLoc.setY(location.getY() + 2.7);
		ArmorStand title = (ArmorStand) game.getWorld().spawnEntity(titleLoc, EntityType.ARMOR_STAND);
		title.setRemoveWhenFarAway(false);
		title.setGravity(false);
		title.setVisible(false);
		title.setBasePlate(false);
		title.setCustomNameVisible(true);
		Location subtitleLoc = location.clone();
		subtitleLoc.setY(location.getY() + 2.4);
		ArmorStand subtitle = (ArmorStand) game.getWorld().spawnEntity(subtitleLoc, EntityType.ARMOR_STAND);
		subtitle.setRemoveWhenFarAway(false);
		subtitle.setGravity(false);
		subtitle.setVisible(false);
		subtitle.setBasePlate(false);
		subtitle.setCustomNameVisible(true);
		subtitle.setCustomName(ChatColor.RED + "" + time + "s");
		Location blockLoc = location.clone();
		blockLoc.setY(location.getY() + 2.5);
		ArmorStand block = (ArmorStand) game.getWorld().spawnEntity(blockLoc, EntityType.ARMOR_STAND);
		block.setRemoveWhenFarAway(false);
		block.setGravity(false);
		block.setVisible(false);
		block.setBasePlate(false);
		block.setCustomNameVisible(false);
		if (this.type == Type.DIAMOND) {
			title.setCustomName("DIAMOND_" + this.type.getPeriod());
			block.setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
		} else if (this.type == Type.EMERALD) {
			title.setCustomName("EMERALD_" + this.type.getPeriod());
			block.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
		}
		rotate(block);
		armorStands.add(title);
		armorStands.add(subtitle);
		armorStands.add(block);
	}

	public void start() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if (!stop && this.location.getBlock().getType() != Material.AIR) {
				if (this.type != Type.BASIC)
					armorStands.get(1).setCustomName(ChatColor.RED + "" + time + "s");
				if (time == 0) {
					if (this.type != Type.BASIC) {
						game.getWorld().dropItemNaturally(armorStands.get(2).getLocation(), new ItemStack(Material.valueOf(this.type.name())));
						if (this.type == Type.DIAMOND) {
							armorStands.get(0).setCustomName("DIAMOND_" + this.type.getPeriod());
						} else if (this.type == Type.EMERALD) {
							armorStands.get(0).setCustomName("EMERALD_" + this.type.getPeriod());
						}
					} else {
						basicItemSpawnedTimes++;
						if (basicItemSpawnedTimes % 4 == 0)
							game.getWorld().dropItemNaturally(this.location, new ItemStack(Material.GOLD_INGOT));
						else
							game.getWorld().dropItemNaturally(this.location, new ItemStack(Material.IRON_INGOT));
						if (this.customItems != null && !this.customItems.isEmpty()) {
							for (Map.Entry<ItemStack, Integer> entry : this.customItems.entrySet()) {
								if (basicItemSpawnedTimes % entry.getValue() == 0) {
									game.getWorld().dropItemNaturally(this.location, entry.getKey());
								}
							}
						}
					}
					time = getDelay() * 20;
				}
				time--;
			}
		}, 0, 1);
	}

	public void stop() {
		this.stop = true;
	}

	public boolean isStopped() {
		return this.stop;
	}

	private void rotate(LivingEntity entity) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if (!stop && this.location.getBlock().getType() != Material.AIR) {
				Location location = entity.getLocation();
				location.setYaw(location.getYaw() + 2.33f);
				entity.teleport(location);
			}
		}, 0, 1);
	}


	public Type getType() {
		return this.type;
	}

	public UUID getTitleId() {
		if (this.type != Type.BASIC)
			return armorStands.get(0).getUniqueId();
		return null;
	}

	public Location getLocation() {
		return this.location;
	}

	public GameTeam getTeam() {
		return team;
	}

	public double getDelay() {
		if (this.delay == -1) {
			this.delay = this.type.getDelay();
			return this.type.getDelay();
		} else {
			return this.delay;
		}
	}

	public void setSpeed(double speed) {
		double oldDelay = this.type.getDelay();
		double newDelay = oldDelay / speed;
		setDelay(newDelay);
	}

	private void setDelay(double delay) {
		if (this.type == Type.BASIC) {
			this.delay = delay;
		}
	}

	public Map<ItemStack, Integer> getCustomItems() {
		return customItems;
	}

	public enum Type {
		BASIC, DIAMOND(45, 30, 20, 15), EMERALD(60, 45, 30, 20);
		public int period0Delay, period1Delay, period2Delay, period3Delay;

		Type() {
		}

		Type(int period0Delay, int period1Delay, int period2Delay, int period3Delay) {
			this.period0Delay = period0Delay;
			this.period1Delay = period1Delay;
			this.period2Delay = period2Delay;
			this.period3Delay = period3Delay;
		}

		private int getBasicDelay() {
				int playersPerTeam = game.getPlayersPerTeam();
				if (playersPerTeam == 1)
					return 2;
				if (playersPerTeam == 2)
					return 3;
				else
					return playersPerTeam;
		}

		public int getDelay() {
			if (this == DIAMOND) {
				if (game.getState() == Game.GameState.LOBBY || game.getState() == Game.GameState.STARTING || game.getState() == Game.GameState.DIAMOND_1)
					return period1Delay;
				else if (game.getState() == Game.GameState.EMERALD_1 || game.getState() == Game.GameState.DIAMOND_2)
					return period2Delay;
				else
					return period3Delay;
			} else if (this == EMERALD) {
				if (game.getState() == Game.GameState.LOBBY || game.getState() == Game.GameState.STARTING || game.getState() == Game.GameState.DIAMOND_1 || game.getState() == Game.GameState.EMERALD_1)
					return period1Delay;
				else if (game.getState() == Game.GameState.DIAMOND_2 || game.getState() == Game.GameState.EMERALD_2)
					return period2Delay;
				else
					return period3Delay;
			} else {
				return getBasicDelay();
			}
		}

		public int getPeriod() {
			if (this == DIAMOND) {
				if (game == null || game.getState() == null)
					return 0;
				if (game.getState().getStage() < 3)
					return 0;
				else if (game.getState().getStage() < 5)
					return 1;
				else if (game.getState().getStage() < 7)
					return 2;
				else
					return 3;
			} else if (this == EMERALD) {
				if (game == null || game.getState() == null)
					return 0;
				if (game.getState().getStage() < 4)
					return 0;
				else if (game.getState().getStage() < 6)
					return 1;
				else if (game.getState().getStage() < 8)
					return 2;
				else
					return 3;
			} else {
				return 0;
			}
		}
	}

}
