package me.tntpablo.thebridge;

import org.bukkit.Bukkit;
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


		for (Player p : bridge.players.keySet()) {
			bridge.updateScoreboard(p);
		}

		// Feedback al jugador de la cuenta atras
		if (Utils.timeReminder(bridge.countdown) != null) {
			bridge.msgAll(("Quedan " + Utils.timeReminder(bridge.countdown) + bridge.countdown + " &fsegundos para "
					+ bridge.getPhaseDescription()));

		}
		// Solo se contara atras si no esta suspendido el juego
		bridge.countdown--;

		if (bridge.countdown == 0) {
			switch (bridge.getGamePhase()) {
				case STARTING:
					bridge.cageWaiting();
					return;
				case WAITING:
					bridge.runGame();
					return;
				case RUNNING:
					bridge.stop();
					return;
				case OFFLINE:
					Bukkit.broadcastMessage("FINALIZANDO TASK: " + this.getTaskId());
					this.cancel();
					return;
			}
		}
		
		if (bridge.getGamePhase() == GamePhase.RUNNING) {
			bridge.timePassed++;
		}
	}

}
