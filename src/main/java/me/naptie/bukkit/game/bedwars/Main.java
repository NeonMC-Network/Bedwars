package me.naptie.bukkit.game.bedwars;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import me.naptie.bukkit.game.bedwars.data.DataHandler;
import me.naptie.bukkit.game.bedwars.listeners.*;
import me.naptie.bukkit.game.bedwars.utils.MySQLManager;
import me.naptie.bukkit.game.bedwars.commands.Bed;
import me.naptie.bukkit.game.bedwars.commands.Shout;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main instance;
	private MySQLManager mySQLManager;
	private DataHandler dataHandler;
	private String restartCmd;
	private Game game;

	public static Main getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		saveDefaultConfig();
		dataHandler = new DataHandler(this);
		mySQLManager = new MySQLManager(this);
		restartCmd = getConfig().getString("restart-command");

		addPacketListener();
		registerCommands();
		registerEvents();

		game = new Game(this);

		this.getLogger().info(Messages.ENABLED);
	}

	@Override
	public void onDisable() {
		this.getLogger().info(Messages.DISABLED);
		instance = null;
	}

	private void addPacketListener() {
		/*ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(this)
				.serverSide()
				.listenerPriority(ListenerPriority.HIGHEST)
				.gamePhase(GamePhase.PLAYING)
				.options(ListenerOptions.SKIP_PLUGIN_VERIFIER)
				.types(PacketType.Play.Server.ENTITY_METADATA)) {

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				final Entity entity = packet.getEntityModifier(event.getPlayer().getWorld()).read(0);
				event.setPacket(packet = packet.deepClone());
				try {
					System.out.println(entity.getUniqueId());
					System.out.println(game.getGamePlayer(event.getPlayer()).getName());
					System.out.println(game.getEntityNameTable().get(entity.getUniqueId(), game.getGamePlayer(event.getPlayer())));
					final String name = game.getEntityNameTable().get(entity.getUniqueId(), game.getGamePlayer(event.getPlayer()));
					if (name != null) {
						event.setPacket(packet = packet.deepClone());
						packet.getWatchableCollectionModifier().read(0).forEach((x) -> {
							if (x.getIndex() == 2) x.setValue(name, true);
						});
					}
				} catch (NullPointerException ignored) {
				}
			}
		});*/
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(this)
				.serverSide()
				.listenerPriority(ListenerPriority.HIGHEST)
				.gamePhase(GamePhase.PLAYING)
				.options(ListenerOptions.SKIP_PLUGIN_VERIFIER)
				.types(PacketType.Play.Server.ENTITY_LOOK)) {

			/**
			 * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.PacketEvent)
			 */
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				final Entity entity = packet.getEntityModifier(event.getPlayer().getWorld()).read(0);
				float yaw = entity.getLocation().getYaw(), pitch = entity.getLocation().getPitch();
				for (LivingEntity shop : game.getShops().keySet()) {
					if (shop.getUniqueId() == entity.getUniqueId()) {
						yaw = game.getShops().get(shop).getYaw();
						pitch = game.getShops().get(shop).getPitch();
					}
				}
				for (LivingEntity upgrade : game.getUpgrades().keySet()) {
					if (upgrade.getUniqueId() == entity.getUniqueId()) {
						yaw = game.getUpgrades().get(upgrade).getYaw();
						pitch = game.getUpgrades().get(upgrade).getPitch();
					}
				}

				byte e = (byte) ((int) (yaw * 256.0F / 360.0F));
				byte f = (byte) ((int) (pitch * 256.0F / 360.0F));
				packet.getBytes().write(0, e);
				packet.getBytes().write(1, f);
			}
		});
	}

	public void restart(final Game game) {

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			game.getPlayers().clear();
			game.getSpectators().clear();
			game.getAllGamePlayers().clear();
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), restartCmd);
		}, 20);

	}

	private void registerCommands() {
		getCommand("shout").setExecutor(new Shout());
		getCommand("bed").setExecutor(new Bed());
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new BlockInteract(), this);
		getServer().getPluginManager().registerEvents(new CreatureSpawn(), this);
		getServer().getPluginManager().registerEvents(new EntityExplode(), this);
		getServer().getPluginManager().registerEvents(new EntitySpawn(), this);
		getServer().getPluginManager().registerEvents(new FoodLevel(), this);
		getServer().getPluginManager().registerEvents(new InventoryClick(), this);
		getServer().getPluginManager().registerEvents(new PlayerChat(), this);
		getServer().getPluginManager().registerEvents(new PlayerDamage(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeath(), this);
		getServer().getPluginManager().registerEvents(new PlayerDropItem(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractEntity(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
		getServer().getPluginManager().registerEvents(new PlayerLeave(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItem(), this);
		getServer().getPluginManager().registerEvents(new ProjectileHit(), this);
		getServer().getPluginManager().registerEvents(new ProjectileLaunch(), this);
		getServer().getPluginManager().registerEvents(new PvP(), this);
		getServer().getPluginManager().registerEvents(new WeatherChange(), this);
	}

	public MySQLManager getMySQLManager() {
		return this.mySQLManager;
	}

	public DataHandler getDataHandler() {
		return this.dataHandler;
	}

	public Game getGame() {
		return this.game;
	}
}
