package me.naptie.bukkit.game.bedwars.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataHandler {

	private static DataHandler instance;
	private Plugin plugin;
	private File gameInfoFile;
	private FileConfiguration gameInfo;
	private Map<String, FileConfiguration> languageConfigs = new HashMap<>();
	private FileConfiguration shop;
	private FileConfiguration upgrade;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public DataHandler(Plugin plugin) {
		instance = this;
		this.plugin = plugin;

		this.gameInfoFile = new File(plugin.getDataFolder(), "game-info.yml");

		if (!this.gameInfoFile.exists()) {
			try {
				this.gameInfoFile.getParentFile().mkdirs();
				this.gameInfoFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.gameInfo = YamlConfiguration.loadConfiguration(this.gameInfoFile);
		for (String language : plugin.getConfig().getStringList("languages")) {
			File languageFile = new File(plugin.getDataFolder(), language + ".yml");
			if (languageFile.exists()) {
				if (plugin.getConfig().getBoolean("update-language-files")) {
					plugin.saveResource(language + ".yml", true);
				}
			} else {
				plugin.saveResource(language + ".yml", false);
			}
			FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
			this.languageConfigs.put(language, config);
		}
		File shop = new File(plugin.getDataFolder(), "shop.yml");
		if (!shop.exists()) {
			plugin.saveResource("shop.yml", false);
		}
		this.shop = YamlConfiguration.loadConfiguration(shop);

		File upgrade = new File(plugin.getDataFolder(), "upgrade.yml");
		if (!upgrade.exists()) {
			plugin.saveResource("upgrade.yml", false);
		}
		this.upgrade = YamlConfiguration.loadConfiguration(upgrade);
	}

	public static DataHandler getInstance() {
		return instance;
	}

	public FileConfiguration getShopConfig() {
		return shop;
	}

	public FileConfiguration getUpgradeConfig() {
		return upgrade;
	}

	public FileConfiguration getLanguageConfig(String language) {
		return languageConfigs.get(language);
	}

	public FileConfiguration getGameInfo() {
		return gameInfo;
	}

	public void saveGameInfo() {
		try {
			this.gameInfo.save(this.gameInfoFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
