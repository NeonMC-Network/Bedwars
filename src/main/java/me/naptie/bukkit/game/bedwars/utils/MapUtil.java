package me.naptie.bukkit.game.bedwars.utils;

import java.util.*;

public class MapUtil {

	public static List<Map<UUID, Integer>> sortKills(Map<UUID, Integer> data) {
		final List<Map<UUID, Integer>> kills = new ArrayList<>();
		for (UUID uuid : data.keySet()) {
			Map<UUID, Integer> each = new HashMap<>();
			each.put(uuid, data.get(uuid));
			kills.add(each);
		}
		kills.sort((o1, o2) -> {
			int kills1 = 0, kills2 = 0;
			for (UUID uuid : o1.keySet()) {
				kills1 = o1.get(uuid);
			}
			for (UUID uuid : o2.keySet()) {
				kills2 = o2.get(uuid);
			}
			return kills2 - kills1;
		});
		return kills;
	}

}
