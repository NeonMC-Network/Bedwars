package me.naptie.bukkit.game.bedwars.tasks;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class Respawn extends BukkitRunnable {

	private int time = 6;
	private Plugin plugin;
	private GamePlayer player;

	private AtomicInteger task = new AtomicInteger(0);

	public Respawn(Plugin plugin, GamePlayer player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public void run() {
		time -= 1;
		if (time > 0) {
			if (!Bukkit.getServer().getScheduler().isCurrentlyRunning(task.get()))
				task.set(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
					if (player.getPlayer().getGameMode() == GameMode.ADVENTURE) {
						if (!player.getPlayer().getAllowFlight())
							player.getPlayer().setAllowFlight(true);
						player.getPlayer().setFlying(true);
					} else {
						Bukkit.getServer().getScheduler().cancelTask(task.get());
					}
				}, 0, 1L));

			this.player.sendTitle(Messages.getMessage(player, "DEATH_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(player, "RESPAWNING").replace("%time%", time + ""), "\"color\":\"yellow\"", 0, 40, 10);
			this.player.sendActionBar(ChatColor.YELLOW + Messages.getMessage(player, "RESPAWNING").replace("%time%", time + ""));
		}

		if (time == 0) {
			Bukkit.getServer().getScheduler().cancelTask(task.get());
			{
				Player player = this.player.getPlayer();
				for (Player online : Bukkit.getOnlinePlayers()) {
					online.showPlayer(player);
				}
				player.spigot().setCollidesWithEntities(true);
				player.setMaxHealth(20);
				player.setHealth(player.getMaxHealth());
				player.getEquipment().clear();
				player.getInventory().clear();
				player.setGameMode(GameMode.SURVIVAL);
				player.setAllowFlight(false);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> player.removePotionEffect(PotionEffectType.INVISIBILITY), 1);
			}
			this.player.giveGameItems();
			this.player.getPlayer().teleport(this.player.getTeam().getSpawnpoint());
			this.player.sendTitle(Messages.getMessage(this.player, "RESPAWNED"), "\"color\":\"green\",\"bold\":true", "", "\"color\":\"green\"", 0, 40, 10);
			this.player.sendActionBar(ChatColor.GREEN + Messages.getMessage(this.player, "RESPAWNED"));
			Main.getInstance().getGame().getUpgrade().update(this.player);
			this.player.setDamageable(false);
		}
		if (time == -3) {
			cancel();
			this.player.setDamageable(true);
		}
	}

}
