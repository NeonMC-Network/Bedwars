package me.naptie.bukkit.game.bedwars.objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.utils.CU;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameUpgrade {

	private List<Purchasable> purchasableList = new ArrayList<>();
	private static Map<Integer, Purchasable.EnchantmentInfo> enchantmentMap = new HashMap<>();
	private static Map<Integer, Purchasable.EffectInfo> effectMap = new HashMap<>();
	private Game game;
	private FileConfiguration config;

	GameUpgrade(Game game) {
		this.game = game;
		this.config = Main.getInstance().getDataHandler().getUpgradeConfig();
		register();
	}

	private void register() {
		for (String id : config.getConfigurationSection("purchasable").getKeys(false)) {
			new Purchasable(Integer.parseInt(id));
		}
	}

	public /*Inventory*/ void getInventory(GamePlayer player) {
		Inventory inventory = Bukkit.createInventory(null, 27, game.getPlayersPerTeam() == 1 ? Messages.getMessage(player, "SOLO_UPGRADE") : Messages.getMessage(player, "TEAM_UPGRADE"));
		//for ()
	}

	private ChatColor getResourceColor(String resource) {
		if (resource.equalsIgnoreCase("iron"))
			return ChatColor.WHITE;
		if (resource.equalsIgnoreCase("gold"))
			return ChatColor.GOLD;
		if (resource.equalsIgnoreCase("diamond"))
			return ChatColor.AQUA;
		if (resource.equalsIgnoreCase("emerald"))
			return ChatColor.DARK_GREEN;
		else
			return ChatColor.GRAY;
	}

	private List<String> parseLore(String input) {
		if (input.startsWith("(") && input.contains("%@") && input.endsWith(")")) {
			String string = input.replace("(", "").replace(")", "");
			return new ArrayList<>(Arrays.asList(string.split("%@")));
		} else {
			return Collections.singletonList(input);
		}
	}

	private List<String> parseCost(String input) {
		if (input.contains("%@")) {
			return new ArrayList<>(Arrays.asList(input.split("%@")));
		} else {
			return Collections.singletonList(input);
		}
	}

	public void update(GamePlayer player) {
		for (Purchasable purchasable : purchasableList) {
			if (purchasable.teamTierMap.get(player.getTeam()) > 0) {
				purchasable.effect(player);
				purchasable.enchant(player);
			}
		}
	}

	public class Purchasable {

		private int id;
		private Map<GameTeam, Integer> teamTierMap = new HashMap<>();
		private Table<String, Integer, ItemStack> item = HashBasedTable.create();
		private List<TrapItem> trapItems = new ArrayList<>();
		private boolean browser;

		public Purchasable(int id) {
			this.id = id;
			for (String language : config.getConfigurationSection("purchasable." + id + ".name").getKeys(false)) {
				if (config.contains("purchasable." + id + ".cost")) {
					setBrowser(false);
					for (int tier = 1; tier <= config.getStringList("purchasable." + id + ".cost").size(); tier++) {
						String name = config.getString("purchasable." + id + ".name." + language).replace("%level%", tier + "");
						System.out.println("Processing " + name);
						if (name.contains("%custom")) {
							for (String key : config.getConfigurationSection("purchasable." + id).getKeys(false)) {
								if (name.replaceAll("%", "").equalsIgnoreCase(key)) {
									name = CU.t(config.getStringList("purchasable." + id + "." + key).get(tier - 1));
								}
							}
						}
						List<String> lore = config.getStringList("purchasable." + id + ".lore." + language);
						for (int i = 0; i < lore.size(); i++) {
							String string = lore.get(i).replace("%level%", tier + "");
							lore.set(i, CU.t(string));
						}
						if (lore.size() == 1 && lore.get(0).startsWith("%custom")) {
							for (String key : config.getConfigurationSection("purchasable." + id).getKeys(false)) {
								if (lore.get(0).replaceAll("%", "").equalsIgnoreCase(key)) {
									lore.addAll(CU.t(parseLore(config.getStringList("purchasable." + id + "." + key).get(tier - 1))));
								}
							}
						}
						Material material = Material.valueOf(config.getString("purchasable." + id + ".material"));
						int amount = Integer.parseInt(config.getString("purchasable." + id + ".amount"));
						lore.add("");
						List<String> cost = parseCost(config.getStringList("purchasable." + id + ".cost").get(tier - 1));
						System.out.println("Cost " + config.getStringList("purchasable." + id + ".cost").get(tier - 1) + " parsed for " + id);
						if (cost.get(0).equalsIgnoreCase("iron") || cost.get(0).equalsIgnoreCase("gold")) {
							String costInfo = Messages.getMessage(language, "COST_IG").replace("%amount%", getResourceColor(cost.get(0)) + cost.get(1)).replace("%resource%", Messages.getMessage(language, cost.get(0).toUpperCase()));
							if (costInfo.contains("%s%"))
								costInfo = costInfo.replace("%s%", Integer.parseInt(cost.get(1)) == 1 ? "" : "s");
							lore.add(costInfo);
						} else {
							String costInfo = Messages.getMessage(language, "COST_DE").replace("%amount%", getResourceColor(cost.get(0)) + cost.get(1)).replace("%resource%", Messages.getMessage(language, cost.get(0).toUpperCase()));
							if (costInfo.contains("%s%"))
								costInfo = costInfo.replace("%s%", Integer.parseInt(cost.get(1)) == 1 ? "" : "s");
							lore.add(costInfo);
						}
						lore.addAll(Arrays.asList("", "%status%"));
						ItemStack itemStack = new ItemStack(material, amount);
						ItemMeta itemMeta = itemStack.getItemMeta();
						itemMeta.setDisplayName(name);
						itemMeta.setLore(lore);
						itemStack.setItemMeta(itemMeta);
						item.put(language, tier, itemStack);
					}
				} else {
					setBrowser(true);
					String name = config.getString("purchasable." + id + ".name." + language);
					if (name.contains("%custom")) {
						for (String key : config.getConfigurationSection("purchasable." + id).getKeys(false)) {
							if (name.replaceAll("%", "").equalsIgnoreCase(key)) {
								name = CU.t(config.getStringList("purchasable." + id + "." + key).get(0));
							}
						}
					}
					List<String> lore = CU.t(config.getStringList("purchasable." + id + ".lore." + language));
					if (lore.size() == 1 && lore.get(0).startsWith("%custom")) {
						for (String key : config.getConfigurationSection("purchasable." + id).getKeys(false)) {
							if (lore.get(0).replaceAll("%", "").equalsIgnoreCase(key)) {
								lore.addAll(CU.t(parseLore(config.getStringList("purchasable." + id + "." + key).get(0))));
							}
						}
					}
					Material material = Material.valueOf(config.getString("purchasable." + id + ".material"));
					int amount = Integer.parseInt(config.getString("purchasable." + id + ".amount"));
					lore.addAll(Arrays.asList("", Messages.getMessage(language, "CLICK_TO_BROWSE")));
					ItemStack itemStack = new ItemStack(material, amount);
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.setDisplayName(name);
					itemMeta.setLore(lore);
					itemStack.setItemMeta(itemMeta);
					item.put(language, 0, itemStack);
					if (config.getString("purchasable." + id + ".browse").equalsIgnoreCase("traps")) {
						for (String i : config.getConfigurationSection("traps").getKeys(false)) {
							trapItems.add(new TrapItem(Integer.parseInt(i)));
						}
					}
				}
			}
			purchasableList.add(this);
			for (GameTeam team : game.getTeams()) {
				teamTierMap.put(team, 0);
			}
		}

		private boolean isAvailable(GamePlayer gamePlayer) {
			int have = 0;
			for (ItemStack item : gamePlayer.getPlayer().getInventory().getContents()) {
				if (item == null || item.getType() == null)
					continue;
				if (item.getType().equals(getResource(parseCost(config.getStringList("purchasable." + id + ".cost").get(teamTierMap.get(gamePlayer.getTeam()) - 1)).get(0)))) {
					have = have + item.getAmount();
				}
			}
			return have >= Integer.parseInt(parseCost(config.getStringList("purchasable." + id + ".cost").get(teamTierMap.get(gamePlayer.getTeam()) - 1)).get(1));
		}

		public PurchasableStatus getStatus(GamePlayer player) {
			int tier = teamTierMap.get(player.getTeam());
			if (tier == config.getStringList("purchasable." + id + ".cost").size()) {
				return PurchasableStatus.UNLOCKED;
			}
			if (isAvailable(player)) {
				return PurchasableStatus.AVAILABLE;
			} else {
				return PurchasableStatus.CANT_AFFORD;
			}
		}

		public class TrapItem {
			private int id;
			private Table<String, Integer, ItemStack> item = HashBasedTable.create();

			TrapItem(int id) {
				this.id = id;
				for (String language : config.getConfigurationSection("traps." + id + ".name").getKeys(false)) {
					for (int tier = 1; tier <= config.getStringList("traps." + id + ".cost").size(); tier++) {
						String name = config.getString("traps." + id + ".name." + language).replace("%level%", tier + "");
						if (name.contains("%custom")) {
							for (String key : config.getConfigurationSection("traps." + id).getKeys(false)) {
								if (name.replaceAll("%", "").equalsIgnoreCase(key)) {
									name = CU.t(config.getStringList("traps." + id + "." + key).get(tier - 1));
								}
							}
						}
						List<String> lore = config.getStringList("traps." + id + ".lore." + language);
						for (int i = 0; i < lore.size(); i++) {
							String string = lore.get(i).replace("%level%", tier + "");
							lore.set(i, CU.t(string));
						}
						if (lore.size() == 1 && lore.get(0).startsWith("%custom")) {
							for (String key : config.getConfigurationSection("traps." + id).getKeys(false)) {
								if (lore.get(0).replaceAll("%", "").equalsIgnoreCase(key)) {
									lore.addAll(CU.t(parseLore(config.getStringList("traps." + id + "." + key).get(tier - 1))));
								}
							}
						}
						Material material = Material.valueOf(config.getString("traps." + id + ".material"));
						int amount = Integer.parseInt(config.getString("traps." + id + ".amount"));
						lore.add("");
						List<String> cost = parseCost(config.getStringList("traps." + id + ".cost").get(tier - 1));
						if (cost.get(0).equalsIgnoreCase("iron") || cost.get(0).equalsIgnoreCase("gold")) {
							String costInfo = Messages.getMessage(language, "COST_IG").replace("%amount%", getResourceColor(cost.get(0)) + cost.get(1)).replace("%resource%", Messages.getMessage(language, cost.get(0).toUpperCase()));
							if (costInfo.contains("%s%"))
								costInfo = costInfo.replace("%s%", Integer.parseInt(cost.get(1)) == 1 ? "" : "s");
							lore.add(costInfo);
						} else {
							String costInfo = Messages.getMessage(language, "COST_DE").replace("%amount%", getResourceColor(cost.get(0)) + cost.get(1)).replace("%resource%", Messages.getMessage(language, cost.get(0).toUpperCase()));
							if (costInfo.contains("%s%"))
								costInfo = costInfo.replace("%s%", Integer.parseInt(cost.get(1)) == 1 ? "" : "s");
							lore.add(costInfo);
						}
						lore.addAll(Arrays.asList("", "%status%"));
						ItemStack itemStack = new ItemStack(material, amount);
						ItemMeta itemMeta = itemStack.getItemMeta();
						itemMeta.setDisplayName(name);
						itemMeta.setLore(lore);
						itemStack.setItemMeta(itemMeta);
						item.put(language, tier, itemStack);
					}
				}
			}

			public ItemStack getItem(String language, int tier) {
				return item.get(language, tier);
			}

			public Table<String, Integer, ItemStack> getItemTable() {
				return item;
			}

			public int getId() {
				return id;
			}
		}

		public void work(GamePlayer player, int tier) {
			if (isBrowser()) {
				//TODO create an inventory that contains all traps. Traps can be retrieved by looping through trapItems.
			} else {
				teamTierMap.put(player.getTeam(), teamTierMap.get(player.getTeam()));
				String code = config.getStringList("purchasable." + this.id + ".feature").get(tier - 1);
				if (code.contains("(") && code.endsWith(")")) {
					if (code.startsWith("enchant")) {
						enchant(player.getTeam(), code.replace("enchant", "").replace("(", "").replace(")", "").split(", "));
					}
					if (code.startsWith("effect")) {
						effect(player.getTeam(), code.replace("effect", "").replace("(", "").replace(")", "").split(", "));
					}
					if (code.startsWith("setBasicSpeed")) {
						setBasicSpeed(player.getTeam(), Double.parseDouble(code.replace("setBasicSpeed", "").replace("(", "").replace(")", "")));
					}
					if (code.startsWith("addBasicItem")) {
						String[] parameter = code.replace("addBasicItem", "").replace("(", "").replace(")", "").split(", ");
						addBasicItem(player.getTeam(), new ItemStack(Material.valueOf(parameter[0])), Integer.parseInt(parameter[1]));
					}
					if (code.startsWith("trap")) {
						trap(player.getTeam(), code.replace("trap", "").replace("(", "").replace(")", "").split(", "));
					}
				}
			}
		}

		private void trap(GameTeam team, String[] parameter) {
			if (parameter.length == 1) {
				new GameTrap(team, GameTrap.Type.valueOf(parameter[0]));
			}
			if (parameter.length == 4) {
				List<PotionEffectType> effects = new ArrayList<>();
				if (parameter[1].startsWith("%@")) {
					for (String string : parameter[1].substring(1).split("%@")) {
						effects.add(PotionEffectType.getByName(string));
					}
				} else {
					effects.add(PotionEffectType.getByName(parameter[1]));
				}
				new GameTrap(team, effects, Integer.parseInt(parameter[2]), Integer.parseInt(parameter[3]));
			}
		}

		private void addBasicItem(GameTeam team, ItemStack item, int rarity) {
			Generator generator = team.getGenerator();
			generator.getCustomItems().put(item, rarity);
		}

		private void setBasicSpeed(GameTeam team, double parameter) {
			Generator generator = team.getGenerator();
			generator.setSpeed(parameter);
		}

		private void effect(GameTeam team, String[] parameter) {
			if (!effectMap.containsKey(this.id)) {
				if (parameter.length == 2) {
					effectMap.put(this.id, new EffectInfo(PotionEffectType.getByName(parameter[0]), Integer.parseInt(parameter[1])));
				}
				if (parameter.length == 3) {
					effectMap.put(this.id, new EffectInfo(PotionEffectType.getByName(parameter[0]), Integer.parseInt(parameter[1]), Integer.parseInt(parameter[2])));
				}
			}
			EffectInfo effectInfo = effectMap.get(this.id);
			PotionEffectType type = effectInfo.getEffectType();
			int amplifier = effectInfo.getAmplifier();
			if (effectInfo.hasRadius()) {
				int radius = effectInfo.getRadius();
				Location spawn = team.getSpawnpoint();
				for (GamePlayer player : team.getMembers()) {
					if (player.getPlayer().getLocation().distanceSquared(spawn) <= radius * radius && player.isDamageable()) {
						player.getPlayer().addPotionEffect(new PotionEffect(type, 3, amplifier));
					}
				}
			} else {
				for (GamePlayer player : team.getMembers()) {
					if (player.isDamageable()) {
						player.getPlayer().addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier));
					}
				}
			}
		}

		public void effect(GamePlayer player) {
			try {
				EffectInfo info = effectMap.get(this.id);
				PotionEffectType type = info.getEffectType();
				int amplifier = info.getAmplifier();
				if (info.hasRadius()) {
					int radius = info.getRadius();
					Location spawn = player.getTeam().getSpawnpoint();
					if (player.getPlayer().getLocation().distanceSquared(spawn) <= radius * radius && player.isDamageable()) {
						player.getPlayer().addPotionEffect(new PotionEffect(type, 3, amplifier));
					}
				} else {
					if (player.isDamageable()) {
						player.getPlayer().addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier));
					}
				}
			} catch (NullPointerException ignored) {
			}
		}

		private void enchant(GameTeam team, String[] parameter) {
			if (!enchantmentMap.containsKey(this.id)) {
				if (parameter.length == 3 && parameter[2].startsWith("%@")) {
					String[] strings = parameter[2].substring(1).split("%@");
					Set<Material> materialSet = new HashSet<>();
					for (Material material : Material.values()) {
						for (String string : strings) {
							if (material.name().contains(string.toUpperCase())) {
								materialSet.add(material);
							}
						}
					}
					enchantmentMap.put(this.id, new EnchantmentInfo(Enchantment.getByName(parameter[0]), Integer.parseInt(parameter[1]), materialSet));
				}
			}
			EnchantmentInfo enchantmentInfo = enchantmentMap.get(this.id);
			Enchantment enchantment = enchantmentInfo.getEnchantment();
			int level = enchantmentInfo.getLevel();
			for (GamePlayer player : team.getMembers()) {
				for (ItemStack stack : player.getPlayer().getInventory().getContents()) {
					if (enchantmentInfo.getAffectedMaterials().contains(stack.getType())) {
						stack.addUnsafeEnchantment(enchantment, level);
					}
				}
			}
		}

		public void enchant(GamePlayer player) {
			try {
				EnchantmentInfo info = enchantmentMap.get(this.id);
				for (ItemStack stack : player.getPlayer().getInventory().getContents()) {
					if (info.getAffectedMaterials().contains(stack.getType())) {
						stack.addUnsafeEnchantment(info.getEnchantment(), info.getLevel());
					}
				}
			} catch (NullPointerException ignored) {
			}
		}

		public ItemStack getItem(String language, int tier, PurchasableStatus status) {
			ItemStack itemStack = item.get(language, tier);
			List<String> lore = new ArrayList<>();
			for (String string : itemStack.getItemMeta().getLore()) {
				if (string.contains("%status%")) {
					if (status == PurchasableStatus.AVAILABLE) {
						string = string.replace("%status%", Messages.getMessage(language, "CLICK_TO_PURCHASE"));
					}
					if (status == PurchasableStatus.UNLOCKED) {
						string = string.replace("%status%", Messages.getMessage(language, "UNLOCKED"));
					}
					if (status == PurchasableStatus.CANT_AFFORD) {
						string = string.replace("%status%", Messages.getMessage(language, "NOT_ENOUGH_RESOURCES"));
					}
				}
				lore.add(string);
			}
			ItemMeta meta = itemStack.getItemMeta();
			meta.setLore(lore);
			itemStack.setItemMeta(meta);
			return itemStack;
		}

		public Table<String, Integer, ItemStack> getItemTable() {
			return item;
		}

		public boolean isBrowser() {
			return browser;
		}

		private void setBrowser(boolean browser) {
			this.browser = browser;
		}

		public class EnchantmentInfo {
			private Enchantment enchantment;
			private int level;
			private Set<Material> affectedMaterials;

			public EnchantmentInfo(Enchantment enchantment, int level, Set<Material> affectedMaterials) {
				this.enchantment = enchantment;
				this.level = level;
				this.affectedMaterials = affectedMaterials;
			}

			public Enchantment getEnchantment() {
				return enchantment;
			}

			public int getLevel() {
				return level;
			}

			public Set<Material> getAffectedMaterials() {
				return affectedMaterials;
			}
		}

		public class EffectInfo {
			private PotionEffectType type;
			private int amplifier;
			private int radius;

			public EffectInfo(PotionEffectType type, int amplifier) {
				this.type = type;
				this.amplifier = amplifier;
				this.radius = -1;
			}

			public EffectInfo(PotionEffectType type, int amplifier, int radius) {
				this.type = type;
				this.amplifier = amplifier;
				this.radius = radius;
			}

			public PotionEffectType getEffectType() {
				return type;
			}

			public int getAmplifier() {
				return amplifier;
			}

			public boolean hasRadius() {
				return this.radius != -1;
			}

			public int getRadius() {
				return radius;
			}
		}
	}

	private Material getResource(String name) {
		if (name.equalsIgnoreCase("iron"))
			return Material.IRON_INGOT;
		if (name.equalsIgnoreCase("gold"))
			return Material.GOLD_INGOT;
		if (name.equalsIgnoreCase("diamond"))
			return Material.DIAMOND;
		if (name.equalsIgnoreCase("emerald"))
			return Material.EMERALD;
		else
			return Material.valueOf(name.toUpperCase());
	}

	public enum PurchasableStatus {
		AVAILABLE, UNLOCKED, CANT_AFFORD
	}

}
