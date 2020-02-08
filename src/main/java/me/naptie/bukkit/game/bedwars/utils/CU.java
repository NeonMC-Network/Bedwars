package me.naptie.bukkit.game.bedwars.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class CU {

	public static String t(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static List<String> t(List<String> input) {
		List<String> output = new ArrayList<>();
		for (String string : input) {
			output.add(t(string));
		}
		return output;
	}

}
