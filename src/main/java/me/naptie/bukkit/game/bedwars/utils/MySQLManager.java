package me.naptie.bukkit.game.bedwars.utils;

import me.naptie.bukkit.game.bedwars.tools.MySQL;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySQLManager {

	public MySQL.Editor editor;
	private Plugin plugin;

	public MySQLManager(Plugin plugin) {
		this.plugin = plugin;
		this.login();
	}

	private void login() {
		MySQL mySQL = new MySQL(this.plugin.getConfig().getString("mysql.username"), this.plugin.getConfig().getString("mysql.password"));
		mySQL.setAddress(this.plugin.getConfig().getString("mysql.address"));
		mySQL.setDatabase(this.plugin.getConfig().getString("mysql.database"));
		mySQL.setTable("games");
		mySQL.setTimezone(this.plugin.getConfig().getString("mysql.timezone"));
		mySQL.setUseSSL(this.plugin.getConfig().getBoolean("mysql.useSSL"));

		try {
			this.editor = mySQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Executors.newSingleThreadExecutor().execute(this::autoReconnect);
	}

	@SuppressWarnings("InfiniteRecursion")
	private void autoReconnect() {

		try {
			TimeUnit.MINUTES.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			this.editor = this.editor.getMySQL().reconnect();
		} catch (SQLException e) {
			this.autoReconnect();
			return;
		}
		this.autoReconnect();
	}

}
