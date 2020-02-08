package me.naptie.bukkit.game.bedwars.utils;

import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import me.naptie.bukkit.player.utils.ConfigManager;
import me.naptie.bukkit.player.utils.SkullManager;
import me.naptie.bukkit.rank.objects.Rank;
import me.naptie.bukkit.rank.utils.RankManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BedMaterialUtil {

    public static ItemStack getNextPageItem(String language) {
        ItemStack item = SkullManager.getCustomSkull("http://textures.minecraft.net/texture/17b03b71d3f86220ef122f9831a726eb2b283319c7b62e7dcd2d64d9682");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Messages.getMessage(language, "NEXT_PAGE"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getPreviousPageItem(String language) {
        ItemStack item = SkullManager.getCustomSkull("http://textures.minecraft.net/texture/48348aa77f9fb2b91eef662b5c81b5ca335ddee1b905f3a8b92095d0a1f141");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Messages.getMessage(language, "PREVIOUS_PAGE"));
        item.setItemMeta(meta);
        return item;
    }

    public static List<ItemStack> getBedMaterials(GamePlayer player, boolean all, int page) {
        List<ItemStack> list = getBedMaterials(player, all);
        List<ItemStack> result = new ArrayList<>();
        if (page == 1) {
            for (int i = 0; i < 45; i++) {
                if (list.size() > i) {
                    result.add(list.get(i));
                } else {
                    return result;
                }
            }
        }
        if (page == 2) {
            for (int i = 45; i < 90; i++) {
                if (list.size() > i) {
                    result.add(list.get(i));
                } else {
                    return result;
                }
            }
        }
        if (page == 3) {
            for (int i = 90; i < list.size(); i++) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    public static List<ItemStack> getBedMaterials(GamePlayer player, boolean all) {
        if (all) {
            if (RankManager.hasRank(player.getPlayer(), Rank.ADMIN, true)) {
                return getBedMaterials(6);
            } else {
                return getBedMaterials(5);
            }
        }
        return getBedMaterials(getTier(player));
    }

    public static String getRequirement(ItemStack item, boolean level) {
        if (getBedMaterials(-1).contains(item))
            return level ? "0" : "MEMBER";
        if (getBedMaterials(0).contains(item))
            return level ? "2" : "*MEMBER";
        if (getBedMaterials(1).contains(item))
            return level ? "4" : "VIP";
        if (getBedMaterials(2).contains(item))
            return level ? "6" : "VIP+";
        if (getBedMaterials(3).contains(item))
            return level ? "7" : "MVP";
        if (getBedMaterials(4).contains(item))
            return level ? "8" : "MVP+";
        if (getBedMaterials(5).contains(item))
            return level ? "9" : "MVP++";
        if (getBedMaterials(6).contains(item))
            return level ? "0" : "*ADMIN";
        return level ? "0" : "MEMBER";
    }

    private static int getTier(GamePlayer player) {
        int level = ConfigManager.getLevelInt(player.getPlayer());
        if (RankManager.hasRank(player.getPlayer(), Rank.HACKER, false))
            return -1;
        if (RankManager.hasRank(player.getPlayer(), Rank.ADMIN, true))
            return 6;
        if (level >= 9 || RankManager.hasRank(player.getPlayer(), Rank.MVPPlusPlus, true))
            return 5;
        if (level == 8 || RankManager.hasRank(player.getPlayer(), Rank.MVPPlus, false))
            return 4;
        if (level == 7 || RankManager.hasRank(player.getPlayer(), Rank.MVP, false))
            return 3;
        if (level == 6 || RankManager.hasRank(player.getPlayer(), Rank.VIPPlus, false))
            return 2;
        if (level == 4 || level == 5 || RankManager.hasRank(player.getPlayer(), Rank.VIP, false))
            return 1;
        if (level == 2 || level == 3)
            return 0;
        return -1;
    }

    private static List<ItemStack> getBedMaterials(int tier) {
        List<ItemStack> itemList = new ArrayList<>(Collections.singletonList(new ItemStack(Material.RED_BED)));
        if (tier >= 0) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.GRASS), new ItemStack(Material.DIRT), new ItemStack(Material.COARSE_DIRT), new ItemStack(Material.PODZOL),
                    new ItemStack(Material.ACACIA_WOOD), new ItemStack(Material.BIRCH_WOOD), new ItemStack(Material.DARK_OAK_WOOD),
                    new ItemStack(Material.JUNGLE_WOOD), new ItemStack(Material.OAK_WOOD), new ItemStack(Material.SPRUCE_WOOD),
                    new ItemStack(Material.ACACIA_SLAB), new ItemStack(Material.BIRCH_SLAB), new ItemStack(Material.DARK_OAK_SLAB),
                    new ItemStack(Material.JUNGLE_SLAB), new ItemStack(Material.OAK_SLAB), new ItemStack(Material.PETRIFIED_OAK_SLAB), new ItemStack(Material.SPRUCE_SLAB),
                    new ItemStack(Material.PUMPKIN), new ItemStack(Material.CACTUS), new ItemStack(Material.SAND),
                    new ItemStack(Material.SOUL_SAND), new ItemStack(Material.GLOWSTONE)));
        }
        if (tier >= 1) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.STONE), new ItemStack(Material.COBBLESTONE), new ItemStack(Material.GRANITE),
                    new ItemStack(Material.DIORITE), new ItemStack(Material.ANDESITE), new ItemStack(Material.SANDSTONE),
                    new ItemStack(Material.MOSSY_COBBLESTONE), new ItemStack(Material.BIRCH_FENCE), new ItemStack(Material.JUNGLE_FENCE),
                    new ItemStack(Material.DARK_OAK_FENCE), new ItemStack(Material.ACACIA_FENCE), new ItemStack(Material.STRIPPED_ACACIA_WOOD), new ItemStack(Material.STRIPPED_BIRCH_WOOD), new ItemStack(Material.STRIPPED_DARK_OAK_WOOD),
                    new ItemStack(Material.STRIPPED_JUNGLE_WOOD), new ItemStack(Material.STRIPPED_OAK_WOOD), new ItemStack(Material.STRIPPED_SPRUCE_WOOD)));
        }
        if (tier >= 2) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.SLIME_BLOCK),
                    new ItemStack(Material.COAL_ORE), new ItemStack(Material.LAPIS_ORE),
                    new ItemStack(Material.IRON_ORE), new ItemStack(Material.GOLD_ORE),
                    new ItemStack(Material.REDSTONE_ORE), new ItemStack(Material.DISPENSER), new ItemStack(Material.DROPPER),
                    new ItemStack(Material.DIAMOND_ORE), new ItemStack(Material.EMERALD_ORE), new ItemStack(Material.NETHERRACK),
                    new ItemStack(Material.BOOKSHELF)));
        }
        if (tier >= 3) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.ANDESITE_SLAB), new ItemStack(Material.BRICK_SLAB),
                    new ItemStack(Material.COBBLESTONE_SLAB), new ItemStack(Material.DIORITE_SLAB), new ItemStack(Material.END_STONE_BRICK_SLAB),
                    new ItemStack(Material.GRANITE_SLAB), new ItemStack(Material.MOSSY_COBBLESTONE_SLAB), new ItemStack(Material.MOSSY_STONE_BRICK_SLAB), new ItemStack(Material.NETHER_BRICK_SLAB),
                    new ItemStack(Material.OAK_SLAB), new ItemStack(Material.PRISMARINE_BRICK_SLAB), new ItemStack(Material.PRISMARINE_SLAB), new ItemStack(Material.PURPUR_SLAB),
                    new ItemStack(Material.QUARTZ_SLAB), new ItemStack(Material.RED_NETHER_BRICK_SLAB), new ItemStack(Material.RED_SANDSTONE_SLAB), new ItemStack(Material.SANDSTONE_SLAB), new ItemStack(Material.STONE_BRICK_SLAB), new ItemStack(Material.STONE_SLAB),
                    new ItemStack(Material.RED_SANDSTONE), new ItemStack(Material.COBWEB)));
        }
        if (tier >= 4) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.MYCELIUM), new ItemStack(Material.COAL_BLOCK), new ItemStack(Material.LAPIS_BLOCK),
                    new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.GOLD_BLOCK), new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.DIAMOND_BLOCK),
                    new ItemStack(Material.EMERALD_BLOCK), new ItemStack(Material.NETHER_QUARTZ_ORE), new ItemStack(Material.ACACIA_LOG), new ItemStack(Material.BIRCH_LOG), new ItemStack(Material.DARK_OAK_LOG),
                    new ItemStack(Material.JUNGLE_LOG), new ItemStack(Material.OAK_LOG), new ItemStack(Material.SPRUCE_LOG)));
        }
        if (tier >= 5) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.QUARTZ_BLOCK), new ItemStack(Material.QUARTZ_SLAB), new ItemStack(Material.END_PORTAL_FRAME),
                    new ItemStack(Material.ACACIA_LEAVES), new ItemStack(Material.BIRCH_LEAVES), new ItemStack(Material.DARK_OAK_LEAVES), new ItemStack(Material.JUNGLE_LEAVES), new ItemStack(Material.OAK_LEAVES), new ItemStack(Material.SPRUCE_LEAVES),
                    new ItemStack(Material.BEACON), new ItemStack(Material.SEA_LANTERN), new ItemStack(Material.TORCH), new ItemStack(Material.REDSTONE_TORCH),
                    new ItemStack(Material.HAY_BLOCK), new ItemStack(Material.PACKED_ICE), new ItemStack(Material.HOPPER), new ItemStack(Material.ACACIA_FENCE_GATE),
                    new ItemStack(Material.BIRCH_FENCE_GATE), new ItemStack(Material.DARK_OAK_FENCE_GATE), new ItemStack(Material.JUNGLE_FENCE_GATE), new ItemStack(Material.OAK_FENCE_GATE), new ItemStack(Material.SPRUCE_FENCE_GATE)));
        }
        if (tier >= 6) {
            itemList.addAll(Arrays.asList(new ItemStack(Material.ACACIA_STAIRS), new ItemStack(Material.BIRCH_STAIRS), new ItemStack(Material.DARK_OAK_STAIRS), new ItemStack(Material.JUNGLE_STAIRS), new ItemStack(Material.OAK_STAIRS),
                    new ItemStack(Material.SPRUCE_STAIRS), new ItemStack(Material.COBBLESTONE_STAIRS), new ItemStack(Material.SANDSTONE_STAIRS),
                    new ItemStack(Material.RED_SANDSTONE_STAIRS), new ItemStack(Material.BRICK_STAIRS), new ItemStack(Material.NETHER_BRICK_STAIRS), new ItemStack(Material.SMOOTH_QUARTZ_STAIRS),
                    new ItemStack(Material.QUARTZ_STAIRS), new ItemStack(Material.PISTON), new ItemStack(Material.SPAWNER), new ItemStack(Material.ACACIA_SIGN), new ItemStack(Material.BIRCH_SIGN), new ItemStack(Material.DARK_OAK_SIGN),
                    new ItemStack(Material.JUNGLE_SIGN), new ItemStack(Material.OAK_SIGN), new ItemStack(Material.SPRUCE_SIGN),
                    new ItemStack(Material.BEDROCK), new ItemStack(Material.BARRIER)));
        }
        return itemList;
    }

	/*public static Material convertFromItemToBlock(Material material) {
		if (material == Material.BED)
			return Material.BED_BLOCK;
		if (material == Material.CAULDRON_ITEM)
			return Material.CAULDRON;
		if (material == Material.BREWING_STAND_ITEM)
			return Material.BREWING_STAND;
		if (material == Material.SIGN)
			return Material.SIGN_POST;
		return material;
	}*/

}
