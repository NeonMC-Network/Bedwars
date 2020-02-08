package me.naptie.bukkit.game.bedwars.utils;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public class ColorUtil {

	public static DyeColor parseDyeColor(String name) {
		if (name.equalsIgnoreCase("red"))
			return DyeColor.RED;
		if (name.equalsIgnoreCase("yellow"))
			return DyeColor.YELLOW;
		if (name.equalsIgnoreCase("green"))
			return DyeColor.LIME;
		if (name.equalsIgnoreCase("blue"))
			return DyeColor.BLUE;
		if (name.equalsIgnoreCase("aqua"))
			return DyeColor.LIGHT_BLUE;
		if (name.equalsIgnoreCase("white"))
			return DyeColor.WHITE;
		if (name.equalsIgnoreCase("pink"))
			return DyeColor.PINK;
		if (name.equalsIgnoreCase("gray"))
			return DyeColor.GRAY;
		return DyeColor.valueOf(name.toUpperCase());
	}

	public static ChatColor parseChatColor(String name) {
		if (name.equalsIgnoreCase("red"))
			return ChatColor.RED;
		if (name.equalsIgnoreCase("yellow"))
			return ChatColor.YELLOW;
		if (name.equalsIgnoreCase("green"))
			return ChatColor.GREEN;
		if (name.equalsIgnoreCase("blue"))
			return ChatColor.BLUE;
		if (name.equalsIgnoreCase("aqua"))
			return ChatColor.AQUA;
		if (name.equalsIgnoreCase("white"))
			return ChatColor.WHITE;
		if (name.equalsIgnoreCase("pink"))
			return ChatColor.LIGHT_PURPLE;
		if (name.equalsIgnoreCase("gray"))
			return ChatColor.GRAY;
		return ChatColor.valueOf(name.toUpperCase());
	}

}
