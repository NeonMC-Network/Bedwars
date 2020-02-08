package me.naptie.bukkit.game.bedwars.objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.utils.MathUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameShop {

	private FileConfiguration config;
	private List<Category> categories = new ArrayList<>();
	private Map<String, Group> groups = new HashMap<>();
	private Table<GamePlayer, Inventory, Category> playerInventoryCategoryTable = HashBasedTable.create();
	private Table<GamePlayer, Integer, Integer> playerUpgradableTable = HashBasedTable.create();

	GameShop(Main main) {
		this.config = main.getDataHandler().getShopConfig();
		registerCategories();
	}

	public Category getMainCategory() {
		for (Category category : categories) {
			if (category.getSlot() == 0)
				return category;
		}
		return categories.get(0);
	}

	private void registerCategories() {
		for (String id : config.getConfigurationSection("categories").getKeys(false)) {
			new Category(Integer.parseInt(id));
		}
	}

	private boolean isAvailable(GamePlayer gamePlayer, int id) {
		int have = 0;
		for (ItemStack item : gamePlayer.getPlayer().getInventory().getContents()) {
			if (item == null || item.getType() == null)
				continue;
			if (item.getType().equals(getResource(config.getStringList("items." + id + ".cost").get(0)))) {
				have = have + item.getAmount();
			}
		}
		return have >= Integer.parseInt(config.getStringList("items." + id + ".cost").get(1));
	}

	private Map<String, Integer> toAvailable(GamePlayer gamePlayer, int id) {
		int have = 0;
		for (ItemStack item : gamePlayer.getPlayer().getInventory().getContents()) {
			if (item == null || item.getType() == null)
				continue;
			if (item.getType().equals(getResource(config.getStringList("items." + id + ".cost").get(0)))) {
				have = have + item.getAmount();
			}
		}
		Map<String, Integer> map = new HashMap<>();
		map.put(config.getStringList("items." + id + ".cost").get(0), Integer.parseInt(config.getStringList("items." + id + ".cost").get(1)) - have);
		return map;
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

	public Map<String, Integer> purchase(GamePlayer player, int item) {
		if (item < 0) {
			Map<String, Integer> map = new HashMap<>();
			map.put("NULL", 0);
			return map;
		}
		if (!isAvailable(player, item))
			return toAvailable(player, item);
		PlayerInventory inventory = player.getPlayer().getInventory();
		Material cost = getResource(config.getStringList("items." + item + ".cost").get(0));
		int amount = Integer.parseInt(config.getStringList("items." + item + ".cost").get(1));
		Map<Integer, ItemStack> have = new HashMap<>();
		for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(cost).entrySet()) {
			have.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Integer, ItemStack> entry : have.entrySet()) {
			if (entry.getValue().getAmount() >= amount) {
				if (entry.getValue().getAmount() > amount)
					entry.getValue().setAmount(entry.getValue().getAmount() - amount);
				else
					inventory.clear(entry.getKey());
				if (config.contains("items." + item + ".material") && config.getString("items." + item + ".material").contains("LEGGINGS")) {
					player.setLeggingsAndBoots(getLeggingsAndBoots(item));
				} else if (config.contains("items." + item + ".material") && config.getString("items." + item + ".material").contains("SWORD")) {
					switch (config.getString("items." + item + ".material")) {
						case "STONE_SWORD":
							if (inventory.contains(Material.WOODEN_SWORD)) {
								inventory.remove(new ItemStack(Material.WOODEN_SWORD, 1));
								break;
							}
							if (inventory.contains(Material.STONE_SWORD)) {
								break;
							}
							if (inventory.contains(Material.IRON_SWORD)) {
								break;
							}
							if (inventory.contains(Material.DIAMOND_SWORD)) {
								break;
							}
						case "IRON_SWORD":
							if (inventory.contains(Material.WOODEN_SWORD)) {
								inventory.remove(new ItemStack(Material.WOODEN_SWORD, 1));
								break;
							}
							if (inventory.contains(Material.STONE_SWORD)) {
								break;
							}
							if (inventory.contains(Material.IRON_SWORD)) {
								break;
							}
							if (inventory.contains(Material.DIAMOND_SWORD)) {
								break;
							}
						case "DIAMOND_SWORD":
							if (inventory.contains(Material.WOODEN_SWORD)) {
								inventory.remove(new ItemStack(Material.WOODEN_SWORD, 1));
								break;
							}
							if (inventory.contains(Material.STONE_SWORD)) {
								break;
							}
							if (inventory.contains(Material.IRON_SWORD)) {
								break;
							}
							if (inventory.contains(Material.DIAMOND_SWORD)) {
								break;
							}
					}
					inventory.addItem(getItem(item, player));
				} else {
					inventory.addItem(getItem(item, player));
				}
				player.upgrade(item);
				Map<String, Integer> map = new HashMap<>();
				map.put(config.getString("items." + item + ".name." + player.getLanguage()), 0);
				return map;
			} else {
				amount = amount - entry.getValue().getAmount();
				inventory.clear(entry.getKey());
			}
		}
		return toAvailable(player, item);
	}

	private ItemStack[] getLeggingsAndBoots(int id) {
		String material = config.getString("items." + id + ".material").replace("LEGGINGS", "");
		return new ItemStack[]{new ItemStack(Material.valueOf(material + "LEGGINGS")), new ItemStack(Material.valueOf(material + "BOOTS"))};
	}

	private ItemStack getItem(int id, GamePlayer player) {
		ItemStack item;
		if (config.contains("items." + id + ".potion")) {
			item = new ItemStack(Material.POTION, config.getInt("items." + id + ".amount"));
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			int amplifier = Integer.parseInt(config.getStringList("items." + id + ".potion").get(1));
			if (amplifier > 0)
				amplifier--;
			meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(config.getStringList("items." + id + ".potion").get(0)), Integer.parseInt(config.getStringList("items." + id + ".potion").get(2)) * 20, amplifier), true);
			item.setItemMeta(meta);
		} else {
			Material material = Material.valueOf(config.getString("items." + id + ".material"));
			if (material.name().endsWith("WOOL"))
				item = new ItemStack(Material.valueOf(player.getTeam().getColor().getDyeColor().name() + "_WOOL"), config.getInt("items." + id + ".amount"));
			else if (material == Material.GLASS)
				item = new ItemStack(Material.valueOf(player.getTeam().getColor().getDyeColor().name() + "_STAINED_GLASS"), config.getInt("items." + id + ".amount"));
			else
				item = new ItemStack(material, config.getInt("items." + id + ".amount"));
		}
		if (config.contains("items." + id + ".enchantment")) {
			for (int i = 0; i < config.getStringList("items." + id + ".enchantment").size() / 2; i++)
				item.addEnchantment(Enchantment.getByName(config.getStringList("items." + id + ".enchantment").get(i * 2)), Integer.parseInt(config.getStringList("items." + id + ".enchantment").get(i * 2 + 1)));
		}
		return item;
	}

	private ItemStack generateItem(int id, String language, boolean available) {
		ItemStack item;
		if (config.contains("items." + id + ".potion")) {
			item = new ItemStack(Material.POTION, config.getInt("items." + id + ".amount"));
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			int amplifier = Integer.parseInt(config.getStringList("items." + id + ".potion").get(1));
			if (amplifier > 0)
				amplifier--;
			meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(config.getStringList("items." + id + ".potion").get(0)), Integer.parseInt(config.getStringList("items." + id + ".potion").get(2)) * 20, amplifier), true);
			item.setItemMeta(meta);
		} else {
			item = new ItemStack(Material.valueOf(config.getString("items." + id + ".material")), config.getInt("items." + id + ".amount"));
		}
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName((available ? ChatColor.GREEN : ChatColor.RED) + config.getString("items." + id + ".name." + language));
		List<String> lore = new ArrayList<>();
		List<String> cost = config.getStringList("items." + id + ".cost");
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
		if (config.contains("items." + id + ".lore." + language)) {
			lore.add("");
			lore.addAll(CU.t(config.getStringList("items." + id + ".lore." + language)));
		}
		if (config.contains("items." + id + ".group")) {
			if (groups.containsKey(config.getString("items." + id + ".group"))) {
				groups.get(config.getString("items." + id + ".group"));
			} else {
				new Group(config.getString("items." + id + ".group"));
			}
			lore.add("");
			lore.addAll(CU.t(config.getStringList("upgradable-lore." + language)));
		}
		if (config.contains("items." + id + ".enchantment")) {
			for (int i = 0; i < config.getStringList("items." + id + ".enchantment").size() / 2; i++)
				item.addUnsafeEnchantment(Enchantment.getByName(config.getStringList("items." + id + ".enchantment").get(i * 2)), Integer.parseInt(config.getStringList("items." + id + ".enchantment").get(i * 2 + 1)));
		}
		if (config.contains("items." + id + ".potion")) {
			lore.add("");
			int amplifier = Integer.parseInt(config.getStringList("items." + id + ".potion").get(1));
			if (amplifier == 0) {
				lore.add(ChatColor.BLUE + StringUtils.capitalize(config.getStringList("items." + id + ".potion").get(0).toLowerCase()) + " (" + Integer.parseInt(config.getStringList("items." + id + ".potion").get(2)) + "s)");
			} else {
				lore.add(ChatColor.BLUE + StringUtils.capitalize(config.getStringList("items." + id + ".potion").get(0).toLowerCase()) + " " + MathUtil.toRoman(amplifier) + " (" + Integer.parseInt(config.getStringList("items." + id + ".potion").get(2)) + "s)");
			}
		}
		lore.add("");
		if (available)
			lore.add(Messages.getMessage(language, "CLICK_TO_PURCHASE"));
		else
			lore.add(Messages.getMessage(language, "NOT_ENOUGH_RESOURCES"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
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

	public Category getCategory(GamePlayer player, Inventory inventory) {
		return playerInventoryCategoryTable.get(player, inventory);
	}

	public Category getCategory(int slot) {
		for (Category category : this.categories) {
			if (category.getSlot() == slot) {
				return category;
			}
		}
		return null;
	}

	int getTier(int item) {
		if (config.contains("items." + item + ".group") && config.contains("items." + item + ".tier")) {
			return config.getInt("items." + item + ".tier");
		} else {
			return -1;
		}
	}

	int getTier(GamePlayer player, int item) {
		if (playerUpgradableTable.contains(player, item)) {
			return playerUpgradableTable.get(player, item);
		} else {
			return 1;
		}
	}

	private int getItem(int tier1Item, int tier) {
		if (getTier(tier1Item) > 0) {
			for (Group group : groups.values()) {
				if (group.getContents().contains(tier1Item)) {
					return group.getContents().get(tier - 1);
				}
			}
			return tier1Item;
		} else {
			return tier1Item;
		}
	}

	List<Category> getCategories() {
		return this.categories;
	}

	Collection<Group> getGroups() {
		return this.groups.values();
	}

	Table<GamePlayer, Integer, Integer> getPlayerUpgradableTable() {
		return this.playerUpgradableTable;
	}

	public class Group {
		private List<Integer> contents = new ArrayList<>();
		private String name;

		public Group(String name) {
			this.name = name;
			groups.put(name, this);
			for (String key : config.getConfigurationSection("items").getKeys(false)) {
				if (!config.contains("items." + key + ".group"))
					continue;
				if (config.getString("items." + key + ".group").equals(this.name)) {
					this.contents.add(Integer.parseInt(key));
				}
			}
		}

		public String getName() {
			return this.name;
		}

		public List<Integer> getContents() {
			return this.contents;
		}

		public int getUpgrade(int tier, boolean returnTier) {
			List<Integer> contents = getContents();
			if (tier <= 0) {
				return returnTier ? 1 : contents.get(0);
			}
			if (tier < contents.size()) {
				return returnTier ? tier + 1 : contents.get(tier);
			} else {
				return returnTier ? contents.size() : contents.get(contents.size() - 1);
			}
		}

		public int getDowngrade(int tier, boolean returnTier) {
			List<Integer> contents = getContents();
			if (tier <= 0) {
				return returnTier ? contents.size() : contents.get(contents.size() - 1);
			}
			if (tier > 1 && tier <= contents.size())
				return returnTier ? tier - 1 : contents.get(tier - 2);
			else
				return returnTier ? 1 : contents.get(0);
		}
	}

	public class Category {
		private List<Integer> contents;
		private Map<String, String> name = new HashMap<>();
		private Map<String, ItemStack> item = new HashMap<>();
		private Map<Integer, Integer> itemSlotMap = new HashMap<>();
		private int slot;

		public Category(int id) {
			this.slot = id;
			for (String language : config.getConfigurationSection("categories." + id + ".name").getKeys(false)) {
				String name = CU.t(config.getString("categories." + id + ".name." + language));
				this.name.put(language, name);
				ItemStack item = new ItemStack(Material.valueOf(config.getString("categories." + id + ".material")), 1);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(name);
				meta.setLore(CU.t(config.getStringList("category-lore." + language)));
				item.setItemMeta(meta);
				this.item.put(language, item);
			}
			this.contents = config.getIntegerList("categories." + id + ".contents");
			categories.add(this);
		}

		public int getItem(int slot) {
			return itemSlotMap.getOrDefault(slot, -1);
		}

		public Inventory getInventory(GamePlayer gamePlayer) {
			Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GREEN + name.get(gamePlayer.getLanguage()));
			setupInventory(inventory, gamePlayer.getLanguage());
			int slot = 19;
			for (int i : this.contents) {
				if (slot == 26 || slot == 27)
					slot = 28;
				if (slot == 35 || slot == 36)
					slot = 37;
				if (slot == 44 || slot == 45)
					slot = 46;
				if (slot > 53)
					break;
				inventory.setItem(slot, generateItem(GameShop.this.getItem(i, getTier(gamePlayer, i)), gamePlayer.getLanguage(), isAvailable(gamePlayer, GameShop.this.getItem(i, getTier(gamePlayer, i)))));
				itemSlotMap.put(slot, GameShop.this.getItem(i, getTier(gamePlayer, i)));
				slot++;
			}
			playerInventoryCategoryTable.put(gamePlayer, inventory, this);
			return inventory;
		}

		private void setupInventory(Inventory inventory, String language) {
			for (Category category : categories) {
				inventory.setItem(category.getSlot(), category.getItem(language));
			}
			for (int i = 9; i < 18; i++) {
				if (i - 9 == this.slot) {
					ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CU.t(config.getString("glass-pane.name." + language)));
					meta.setLore(CU.t(config.getStringList("glass-pane.lore." + language)));
					item.setItemMeta(meta);
					inventory.setItem(i, item);
				} else {
					ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(CU.t(config.getString("glass-pane.name." + language)));
					meta.setLore(CU.t(config.getStringList("glass-pane.lore." + language)));
					item.setItemMeta(meta);
					inventory.setItem(i, item);
				}
			}
		}

		public List<Integer> getContents() {
			return this.contents;
		}

		public int getSlot() {
			return this.slot;
		}

		public ItemStack getItem(String language) {
			return this.item.get(language);
		}

		public String getName(String language) {
			return this.name.get(language);
		}
	}

}
