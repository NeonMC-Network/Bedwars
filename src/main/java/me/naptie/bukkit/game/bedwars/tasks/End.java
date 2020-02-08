package me.naptie.bukkit.game.bedwars.tasks;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.objects.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class End extends BukkitRunnable {

	private int time = 10;
	private Plugin plugin;
	private Game game;

	public End(Plugin plugin, Game game) {
		this.plugin = plugin;
		this.game = game;
	}

	@Override
	public void run() {
		time -= 1;
		if (time == 0) {

			cancel();

			Bukkit.getScheduler().cancelTasks(this.plugin);

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					p.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
				game.getGamePlayer(p).sendToLobby();
			}

			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Main.getInstance().restart(game);
		}
	}

}
