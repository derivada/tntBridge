package me.tntpablo.thebridge;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BridgeCmd implements CommandExecutor {

	@SuppressWarnings("unused")
	private Main plugin;
	private BridgeManager bridge;
	
	
	public BridgeCmd(Main plugin) {
		this.plugin = plugin;
		this.bridge = plugin.bridgeManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		/*
		 * Subcomandos:
		 * bridge join
		 * 
		 * 
		 * 
		 */

		if (args.length == 0) {
			Bukkit.broadcastMessage(Utils.pluginMsg("usage"));
		}
		if (args.length == 1) {
			switch (args[0].toLowerCase()) {
			case "join":
				if(sender instanceof Player) {
					Player p = (Player) sender;
					bridge.addPlayer(p);
					bridge.attempStart(false);
				}
				break;
			case "leave":
				Bukkit.broadcastMessage(Utils.pluginMsg("noimplement"));
				break;
			case "menu":
				Bukkit.broadcastMessage(Utils.pluginMsg("noimplement"));
				break;
			case "setplayers":
				Bukkit.broadcastMessage(Utils.pluginMsg("noimplement"));
				break;
			case "stop":
				bridge.stop();
			case "reset":
				Bukkit.broadcastMessage(Utils.pluginMsg("noimplement"));
				break;
			case "start":
				bridge.attempStart(true);
				break;
			default:
				Bukkit.broadcastMessage(Utils.pluginMsg("usage"));
				break;
			}
			return true;
		}
		return false;
	}

}
