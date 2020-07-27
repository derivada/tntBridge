package me.tntpablo.thebridge;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BridgeUpdate extends BukkitRunnable {

	// La clase con operaciones in-game de The Bridge
	@SupressWarnings("unused")
	private Main plugin;
	private BridgeManager bridge;

	// Lista de los jugadores que ESTABAN (O SIGUEN ESTANDO) cuando empezo la
	// partida

	public BridgeUpdate(Main plugin, BridgeManager bridge) {
		this.plugin = plugin;
		this.bridge = bridge;
	}

	@Override
	public void run() {
		
		// Cuenta atras, detectar scores, cambiar estado de exit
		if (bridge.countdown == 0 && bridge.getGamePhase() == 1) {
			bridge.gameStart();
		}
		if (bridge.countdown == 0 && bridge.getGamePhase() == 2) {
			bridge.stop();
		}

		if (Utils.timeReminder(bridge.countdown) != null) {

				bridge.msgAll(("Quedan " + Utils.timeReminder(bridge.countdown) + bridge.countdown
						+ " &fsegundos para " + bridge.getPhaseDescription()));

		}
		bridge.countdown--;
		for(Player p: bridge.players.keySet()){
			bridge.updateScoreboard(p);
		}
		

	}

}
