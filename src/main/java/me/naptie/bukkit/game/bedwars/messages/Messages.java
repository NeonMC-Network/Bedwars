package me.naptie.bukkit.game.bedwars.messages;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.data.DataHandler;
import me.naptie.bukkit.game.bedwars.utils.CU;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messages {

	public static final String COMMAND_HYPHEN = CU.t("&b-----------------------------");
	public static final String ENDING_MESSAGE_HYPHEN = CU.t("&a&l---------------------------------------------");
	private static String pluginName = Main.getInstance().getDescription().getName();
	private static String pluginVersion = Main.getInstance().getDescription().getVersion();
	public static final String ENABLED = "Enabled " + pluginName + " v" + pluginVersion;
	public static final String DISABLED = "Disabled " + pluginName + " v" + pluginVersion;

	public static String getMessage(FileConfiguration language, String message) {
		return CU.t(language.getString(message));
	}

	public static String getMessage(String language, String message) {
		if (DataHandler.getInstance().getLanguageConfig(language).contains(message))
			return CU.t(DataHandler.getInstance().getLanguageConfig(language).getString(message));
		else
			return message;
	}

	public static String getMessage(GamePlayer player, String message) {
		return getMessage(player.getLanguage(), message);
	}

	public static Map<String, String> getMessagesInDifLangs(String message) {
		Map<String, String> output = new HashMap<>();
		for (String language : Main.getInstance().getConfig().getStringList("languages")) {
			output.put(language, getMessage(language, message));
		}
		return output;
	}

	public static String translate(String text, String fromLanguage, String toLanguage) {
		String result = "";
		FileConfiguration sourceYaml = DataHandler.getInstance().getLanguageConfig(fromLanguage);
		FileConfiguration targetYaml = DataHandler.getInstance().getLanguageConfig(toLanguage);
		for (String key : sourceYaml.getKeys(false)) {
			if (text.equalsIgnoreCase(sourceYaml.getString(key))) {
				result = targetYaml.getString(key);
			}
		}
		return result;
	}

	public static List<String> getMessages(GamePlayer player, String message) {
		return CU.t(getLanguage(player.getLanguage()).getStringList(message));
	}

	public static List<String> getMessages(String language, String message) {
		return CU.t(getLanguage(language).getStringList(message));
	}

	private static FileConfiguration getLanguage(String language) {
		return DataHandler.getInstance().getLanguageConfig(language);
	}

}