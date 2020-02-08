package me.naptie.bukkit.game.bedwars.listeners;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import me.naptie.bukkit.game.bedwars.objects.GameShop;
import me.naptie.bukkit.game.bedwars.objects.GameTeam;
import me.naptie.bukkit.game.bedwars.utils.BedMaterialUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Game game = Main.getInstance().getGame();
        if (game != null && event.getWhoClicked() instanceof Player) {
            if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING)) {
                event.setCancelled(true);
            }
            GameShop shop = game.getShop();
            GamePlayer player = game.getGamePlayer((Player) event.getWhoClicked());
            GameShop.Category category = shop.getCategory(player, event.getClickedInventory());
            if (event.getClickedInventory() != null) {
                if (event.getClickedInventory().getHolder() == null) {
                    if (category != null) {
                        event.setCancelled(true);
                        if (event.getSlot() < 8) {
                            player.getPlayer().openInventory(shop.getCategory(event.getSlot()).getInventory(player));
                            return;
                        }
                        if (event.getSlot() > 18) {
                            Map<String, Integer> result = shop.purchase(player, category.getItem(event.getSlot()));
                            if (result.containsKey("NULL"))
                                return;
                            if (result.containsKey("iron") || result.containsKey("gold")) {
                                String key = "";
                                for (String k : result.keySet()) {
                                    key = k;
                                }
                                String failure = Messages.getMessage(player, "PURCHASE_FAILURE_IG").replace("%amount%", result.get(key) + "").replace("%resource%", Messages.getMessage(player, key.toUpperCase()));
                                if (failure.contains("%s%"))
                                    failure = failure.replace("%s%", result.get(key) == 1 ? "" : "s");
                                player.sendMessage(failure);
                                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return;
                            }
                            if (result.containsKey("diamond") || result.containsKey("emerald")) {
                                String key = "";
                                for (String k : result.keySet()) {
                                    key = k;
                                }
                                String failure = Messages.getMessage(player, "PURCHASE_FAILURE_DE").replace("%amount%", result.get(key) + "").replace("%resource%", Messages.getMessage(player, key.toUpperCase()));
                                if (failure.contains("%s%"))
                                    failure = failure.replace("%s%", result.get(key) == 1 ? "" : "s");
                                player.sendMessage(failure);
                                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return;
                            }
                            String key = "";
                            for (String k : result.keySet()) {
                                key = k;
                            }
                            player.sendMessage(Messages.getMessage(player, "PURCHASE_SUCCESS").replace("%name%", key));
                            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 8);
                            player.getPlayer().openInventory(category.getInventory(player));
                        }
                    } else {
                        handleOtherInventories(event, game, player);
                    }
                } else {
                    GameShop.Category target = shop.getCategory(player, event.getInventory());
                    if (target != null) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void handleOtherInventories(InventoryClickEvent event, Game game, GamePlayer player) {
        Inventory inventory = event.getClickedInventory();
        if (event.getView().getTitle().equals(Messages.getMessage(player, "OPTION_MENU_TITLE"))) {
            event.setCancelled(true);
            if (event.getSlot() == 11) {
                player.getPlayer().openInventory(game.generateOptionMenu(player, true, false));
                return;
            }
            if (event.getSlot() == 15) {
                player.getPlayer().openInventory(game.generateOptionMenu(player, false, true));
                return;
            }
        }
        if (event.getView().getTitle().equals(Messages.getMessage(player, "TEAM_MENU_TITLE"))) {
            event.setCancelled(true);
            ItemStack item = inventory.getItem(event.getSlot());
            if (item != null && item.getType().name().endsWith("_WOOL")) {
                String color = inventory.getItem(event.getSlot()).getType().name().replace("_WOOL", "");
                for (GameTeam team : game.getTeams()) {
                    if (team.getColor().getDyeColor().name().equalsIgnoreCase(color)) {
                        if (game.getQueuedPlayersAmount(team) < game.getPlayersPerTeam()) {
                            game.getPlayerTeamQueue().put(player, team);
                            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 8);
                            player.sendMessage(Messages.getMessage(player, "JOINED_TEAM").replace("%team%", Messages.getMessage(player, Messages.translate(StringUtils.capitalize(team.getColor().name().toLowerCase()), "en-US", player.getLanguage()))));
                            player.getPlayer().closeInventory();
                            return;
                        } else {
                            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            player.sendMessage(Messages.getMessage(player, "TEAM_FULL"));
                            return;
                        }
                    }
                }
            }
        }
        if (event.getView().getTitle().contains(Messages.getMessage(player, "BED_MENU_TITLE"))) {
            event.setCancelled(true);
            if (event.getSlot() < 45 && inventory.getItem(event.getSlot()) != null) {
                ItemStack item = inventory.getItem(event.getSlot());
                if (item.getItemMeta().getDisplayName().contains(ChatColor.GREEN + "")) {
                    Material material = item.getType();
                    short data = item.getDurability();
                    player.setBedMaterial(material, data);
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 8);
                    player.getPlayer().closeInventory();
                    player.sendMessage(Messages.getMessage(player, "BED_MATERIAL_SET").replace("%material%", material.name()));
                } else {
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    player.sendMessage(Messages.getMessage(player, "HAVENT_REACHED_REQUIREMENT"));
                }
                return;
            }
            if (event.getSlot() == 45) {
                if (event.getView().getTitle().contains("1/3"))
                    return;
                if (event.getView().getTitle().contains("2/3")) {
                    player.getPlayer().openInventory(game.generateOptionMenu(player, false, true));
                    return;
                }
                if (event.getView().getTitle().contains("3/3")) {
                    player.getPlayer().openInventory(generateBedMaterialMenu(game, player, 2));
                    return;
                }
            }
            if (event.getSlot() == 53) {
                if (event.getView().getTitle().contains("1/3")) {
                    player.getPlayer().openInventory(generateBedMaterialMenu(game, player, 2));
                    return;
                }
                if (event.getView().getTitle().contains("2/3")) {
                    player.getPlayer().openInventory(generateBedMaterialMenu(game, player, 3));
                }
            }
        }
    }

    private Inventory generateBedMaterialMenu(Game game, GamePlayer player, int page) {
        if (page == 1)
            return game.generateOptionMenu(player, false, true);
        if (page == 2) {
            Inventory inventory = Bukkit.createInventory(null, 54, Messages.getMessage(player, "BED_MENU_TITLE") + " 2/3");
            int slot = 0;
            for (ItemStack item : BedMaterialUtil.getBedMaterials(player, true, 2)) {
                ItemMeta meta = item.getItemMeta();
                if (BedMaterialUtil.getBedMaterials(player, false).contains(item)) {
                    meta.setDisplayName(ChatColor.GREEN + item.getType().name());
                    List<String> lore = new ArrayList<>();
                    if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
                        lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
                    } else {
                        lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
                    }
                    lore.add(Messages.getMessage(player, "CLICK_TO_CHOOSE"));
                    meta.setLore(lore);
                } else {
                    meta.setDisplayName(ChatColor.RED + item.getType().name());
                    List<String> lore = new ArrayList<>();
                    if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
                        lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
                    } else {
                        lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
                    }
                    lore.add(Messages.getMessage(player, "HAVENT_REACHED_REQUIREMENT"));
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
                slot++;
            }
            inventory.setItem(45, BedMaterialUtil.getPreviousPageItem(player.getLanguage()));
            inventory.setItem(53, BedMaterialUtil.getNextPageItem(player.getLanguage()));
            return inventory;
        }
        if (page == 3) {
            Inventory inventory = Bukkit.createInventory(null, 54, Messages.getMessage(player, "BED_MENU_TITLE") + " 3/3");
            int slot = 0;
            for (ItemStack item : BedMaterialUtil.getBedMaterials(player, true, 3)) {
                ItemMeta meta = item.getItemMeta();
                if (BedMaterialUtil.getBedMaterials(player, false).contains(item)) {
                    if (item.getType() == Material.RED_BED) {
                        meta.setDisplayName(ChatColor.GREEN + "BED");
                    } else {
                        meta.setDisplayName(ChatColor.GREEN + item.getType().name());
                    }
                    List<String> lore = new ArrayList<>();
                    if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
                        lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
                    } else {
                        lore.add(ChatColor.GREEN + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
                    }
                    lore.add(Messages.getMessage(player, "CLICK_TO_CHOOSE"));
                    meta.setLore(lore);
                } else if (item.getType() != Material.AIR) {
                    meta.setDisplayName(ChatColor.RED + item.getType().name());
                    List<String> lore = new ArrayList<>();
                    if (BedMaterialUtil.getRequirement(item, false).startsWith("*")) {
                        lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT_1").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false).substring(1)));
                    } else {
                        lore.add(ChatColor.RED + Messages.getMessage(player, "BED_MATERIAL_REQUIREMENT").replace("%level%", BedMaterialUtil.getRequirement(item, true)).replace("%rank%", BedMaterialUtil.getRequirement(item, false)));
                    }
                    lore.add(Messages.getMessage(player, "HAVENT_REACHED_REQUIREMENT"));
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
                slot++;
            }
            inventory.setItem(45, BedMaterialUtil.getPreviousPageItem(player.getLanguage()));
            return inventory;
        }
        return Bukkit.createInventory(null, 54, Messages.getMessage(player, "BED_MENU_TITLE") + " " + page + "/3");
    }

}
