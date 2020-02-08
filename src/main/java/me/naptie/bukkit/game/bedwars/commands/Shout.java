package me.naptie.bukkit.game.bedwars.commands;

import me.naptie.bukkit.game.bedwars.Main;
import me.naptie.bukkit.game.bedwars.messages.Messages;
import me.naptie.bukkit.game.bedwars.objects.Game;
import me.naptie.bukkit.game.bedwars.objects.GamePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Shout implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Game game = Main.getInstance().getGame();
			if (game != null) {
				GamePlayer player = game.getGamePlayer((Player) sender);
				if (game.getState() == Game.GameState.LOBBY || game.getState() == Game.GameState.STARTING) {
					player.sendMessage(Messages.getMessage(player, "COMMAND_UNAVAILABLE").replace("%command%", "/shout"));
					return true;
				}
				if (args.length == 0) {
					player.sendMessage(Messages.getMessage(player, "USAGE").replace("%usage%", "/shout <message>"));
					return true;
				}
				if (game.getPlayersPerTeam() > 1) {
					for (GamePlayer online : game.getAllGamePlayers()) {
						StringBuilder message = new StringBuilder();
						for (int i = 0; i < args.length; i++) {
							if (i == 0)
								message.append(args[0]);
							else
								message.append(" ").append(args[i]);
						}
						online.sendMessage(Messages.getMessage(online, "SHOUT").replace("%player%", player.getPlayer().getDisplayName()).replace("%message%", message.toString()));
					}
				} else {
					player.sendMessage(Messages.getMessage(player, "COMMAND_UNAVAILABLE").replace("%command%", "/shout"));
				}
			}
		} else {
			sender.sendMessage(Messages.getMessage("zh-CN", "NOT_A_PLAYER"));
		}
		return true;
	}
}
