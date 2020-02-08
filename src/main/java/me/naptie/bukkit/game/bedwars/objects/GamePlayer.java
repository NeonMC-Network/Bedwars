package me.naptie.bukkit.game.bedwars.objects;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.tasks.Run;
import me.naptie.bukkit.game.bedwars.utils.CU;
import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PacketPlayOutTitle;
import me.naptie.bukkit.game.utils.ServerManager;
import me.naptie.bukkit.player.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.io.IOException;
import java.util.List;

public class GamePlayer {

	private Player player;
	private String name, language;
	private int kills, finalKills;
	private int bedsBroken;
	private YamlConfiguration data;
	private GameBed.BedMaterial bedMaterial;
	private GameTeam team;
	private ItemStack[] leggingsAndBoots;
	private boolean damageable;

	public GamePlayer(Player player) {
		this.player = player;
		this.data = ConfigManager.getData(player);
		this.language = this.data.getString("language");
		this.kills = 0;
		this.bedsBroken = 0;
		this.bedMaterial = new GameBed.BedMaterial(Material.RED_BED, (short) 0);
		this.damageable = true;
	}

	public void sendMessage(String message) {
		this.player.sendMessage(CU.t(message));
	}

	public void sendMessage(List<String> messageList) {
		for (String message : messageList)
			player.sendMessage(CU.t(message));
	}

	public void sendTitle(String titleText, String titleProperties, String subtitleText, String subtitleProperties, int titleFadeIn, int titleStay, int titleFadeOut) {
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + titleText + "\"," + titleProperties.toLowerCase() + "}");
		IChatBaseComponent chatSubtitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + subtitleText + "\"," + subtitleProperties.toLowerCase() + "}");
		PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubtitle);
		PacketPlayOutTitle titleLength = new PacketPlayOutTitle(titleFadeIn, titleStay, titleFadeOut);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titleLength);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);
	}

	public void sendActionBar(String message) {
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + CU.t(message) + "\"}");
		PacketPlayOutChat title = new PacketPlayOutChat(chatTitle, ChatMessageType.GAME_INFO);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
	}

	void playSound(Sound sound, int volume, int pitch) {
		player.playSound(player.getLocation(), sound, volume, pitch);
	}

	void teleport(Location location) {
		if (location == null) {
			return;
		}
		getPlayer().teleport(location);
	}

	void giveInteractiveItems() {
		me.naptie.bukkit.inventory.utils.ConfigManager.getItem(this.player, this.player.getInventory(), "options");
		me.naptie.bukkit.inventory.utils.ConfigManager.getItem(this.player, this.player.getInventory(), "leave");
	}

	public void sendToLobby() {
		String lobby;
		try {
			lobby = ServerManager.getLobby(Main.getInstance().getServer().getPort());
		} catch (IllegalArgumentException e) {
			lobby = ServerManager.getLobby(30001);
		}
		me.naptie.bukkit.core.Main.getInstance().connectWithoutChecking(this.player, lobby, false);
	}

	void setDisplayName(String name) {
		this.name = name;
		player.setDisplayName(name);
	}

	public String getName() {
		if (player.isOnline()) {
			String name = player.getDisplayName();
			if (name.contains("[") && name.contains("]")) {
				name = name.split("] ")[1];
			}
			return name;
		} else {
			return this.name;
		}
	}

	public boolean isSpectating() {
		for (GamePlayer spectator : Main.getInstance().getGame().getSpectators()) {
			if (spectator.getPlayer().getUniqueId() == this.player.getUniqueId()) {
				return true;
			}
		}
		return false;
	}

	public boolean isTeamMate(GamePlayer gamePlayer) {
		if (this.team != null && gamePlayer.getTeam() != null)
			return this.team == gamePlayer.getTeam() || this.team.getMembers().contains(gamePlayer) || gamePlayer.getTeam().getMembers().contains(this);
		else
			return false;
	}

	public void setLeggingsAndBoots(ItemStack leggings, ItemStack boots) {
		this.leggingsAndBoots = new ItemStack[]{leggings, boots};
		this.player.getInventory().setLeggings(leggings);
		this.player.getInventory().setBoots(boots);
	}

	public void setLeggingsAndBoots(ItemStack[] leggingsAndBoots) {
		this.leggingsAndBoots = leggingsAndBoots;
		this.player.getInventory().setLeggings(leggingsAndBoots[0]);
		this.player.getInventory().setBoots(leggingsAndBoots[1]);
	}

	public void setLeggingsAndBoots() {
		if (this.leggingsAndBoots == null || this.leggingsAndBoots.length < 2) {
			ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
			{
				LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();
				meta.setColor(this.team.getColor().getBukkitColor());
				leggings.setItemMeta(meta);
			}
			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
			{
				LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
				meta.setColor(this.team.getColor().getBukkitColor());
				boots.setItemMeta(meta);
			}
			setLeggingsAndBoots(leggings, boots);
		} else {
			this.player.getInventory().setLeggings(this.leggingsAndBoots[0]);
			this.player.getInventory().setBoots(this.leggingsAndBoots[1]);
		}
	}

	public GameTeam getTeam() {
		return this.team;
	}

	public void setTeam(GameTeam team) {
		this.team = team;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getLanguage() {
		return this.language;
	}

	public YamlConfiguration getData() {
		return this.data;
	}

	public int getKills() {
		return this.kills;
	}

	public void addKill() {
		this.kills++;
		if (Run.killCount.containsKey(this.player.getUniqueId())) {
			int kills = Run.killCount.get(this.player.getUniqueId());
			Run.killCount.put(this.player.getUniqueId(), kills + 1);
		} else {
			Run.killCount.put(this.player.getUniqueId(), 1);
		}
		if (Run.totalKillCount.containsKey(this.player.getUniqueId())) {
			Run.totalKillCount.put(this.player.getUniqueId(), Run.totalKillCount.get(this.player.getUniqueId()) + 1);
		} else {
			Run.totalKillCount.put(this.player.getUniqueId(), 1);
		}
		data.set("point", data.getInt("point") + 1);
		sendMessage(ChatColor.GREEN + "+1" + (ConfigManager.getLanguageName(getPlayer()).contains("en") ? Messages.getMessage(this.language, "ADD_POINTS").replace("%s%", "") : Messages.getMessage(this.language, "ADD_POINTS")));
		data.set("bedwars.kills", data.getInt("bedwars.kills") + 1);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getFinalKills() {
		return this.finalKills;
	}

	public void addFinalKill() {
		this.finalKills++;
		if (Run.finalKillCount.containsKey(this.player.getUniqueId())) {
			int kills = Run.finalKillCount.get(this.player.getUniqueId());
			Run.finalKillCount.put(this.player.getUniqueId(), kills + 1);
		} else {
			Run.finalKillCount.put(this.player.getUniqueId(), 1);
		}
		if (Run.totalKillCount.containsKey(this.player.getUniqueId())) {
			Run.totalKillCount.put(this.player.getUniqueId(), Run.totalKillCount.get(this.player.getUniqueId()) + 1);
		} else {
			Run.totalKillCount.put(this.player.getUniqueId(), 1);
		}
		data.set("point", data.getInt("point") + 2);
		sendMessage(ChatColor.GREEN + "+2" + (ConfigManager.getLanguageName(getPlayer()).contains("en") ? Messages.getMessage(this.language, "ADD_POINTS").replace("%s%", "") : Messages.getMessage(this.language, "ADD_POINTS")));
		data.set("bedwars.kills", data.getInt("bedwars.kills") + 2);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void upgrade(int item) {
		GameShop shop = Main.getInstance().getGame().getShop();
		for (GameShop.Group group : shop.getGroups()) {
			if (group.getContents().contains(item)) {
				shop.getPlayerUpgradableTable().put(this, group.getContents().get(0), group.getUpgrade(shop.getTier(item), true));
			}
		}
	}

	public void downgrade(int item) {
		GameShop shop = Main.getInstance().getGame().getShop();
		for (GameShop.Group group : shop.getGroups()) {
			if (group.getContents().contains(item)) {
				shop.getPlayerUpgradableTable().put(this, group.getContents().get(0), group.getDowngrade(shop.getTier(item), true));
			}
		}
	}

	public void downgradeAll() {
		GameShop shop = Main.getInstance().getGame().getShop();
		for (GameShop.Group group : shop.getGroups()) {
			for (int item : group.getContents()) {
				if (shop.getPlayerUpgradableTable().contains(this, item))
					downgrade(group.getContents().get(shop.getPlayerUpgradableTable().get(this, item) - 1));
			}
		}
	}

	public int getBedsBroken() {
		return this.bedsBroken;
	}

	public void addBedBroken() {
		this.bedsBroken++;
		data.set("point", data.getInt("point") + 2);
		sendMessage(ChatColor.GREEN + "+2" + (ConfigManager.getLanguageName(getPlayer()).contains("en") ? Messages.getMessage(this.language, "ADD_POINTS").replace("%s%", "") : Messages.getMessage(this.language, "ADD_POINTS")));
		data.set("bedwars.beds-broken", data.getInt("bedwars.beds-broken") + 2);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void giveGameItems() {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
			meta.setColor(this.team.getColor().getBukkitColor());
			helmet.setItemMeta(meta);
		}
		this.player.getInventory().setHelmet(helmet);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
			meta.setColor(this.team.getColor().getBukkitColor());
			chestplate.setItemMeta(meta);
		}
		this.player.getInventory().setChestplate(chestplate);
		setLeggingsAndBoots();
		this.player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD, 1));
	}

	public GameBed.BedMaterial getBedMaterial() {
		return bedMaterial;
	}

	public void setBedMaterial(Material material, short data) {
		this.bedMaterial.setBedMaterial(material, data);
	}

	public boolean isDamageable() {
		return damageable;
	}

	public void setDamageable(boolean damageable) {
		this.damageable = damageable;
	}
}
