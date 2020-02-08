package me.naptie.bukkit.game.bedwars.commands;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.permissions.Permissions;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import me.naptie.bukkit.player.utils.ConfigManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bed implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Game game = Main.getInstance().getGame();
			if (game != null && (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING))) {
				GamePlayer player = game.getGamePlayer((Player) sender);
				if (args.length == 0) {
					player.sendMessage(Messages.getMessage(player, "CURRENT_BED_MATERIAL").replace("%material%", player.getBedMaterial().getMaterial().name()));
					return true;
				}
				if (player.getPlayer().hasPermission(Permissions.ADMINISTRATION)) {
					if (Material.matchMaterial(args[0]) != null && StringUtils.isNumeric(args[1])) {
						Material material = Material.matchMaterial(args[0]);
						player.setBedMaterial(material, Short.parseShort(args[1]));
						player.sendMessage(Messages.getMessage(player, "BED_MATERIAL_SET").replace("%material%", material.name()));
					} else {
						player.sendMessage(Messages.getMessage(player, "MATERIAL_NOT_FOUND").replace("%material%", args[0].toUpperCase()));
					}
				} else {
					player.sendMessage(Messages.getMessage(player, "USAGE").replace("%usage%", "/bed"));
				}
				return true;
			} else {
				Player player = (Player) sender;
				player.sendMessage(Messages.getMessage(ConfigManager.getLanguageName(player), "COMMAND_UNAVAILABLE").replace("%command%", "/bed"));
				return true;
			}
		} else {
			sender.sendMessage(Messages.getMessage("zh-CN", "NOT_A_PLAYER"));
			return true;
		}
	}
}
