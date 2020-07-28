package me.tntpablo.thebridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BridgeManager {

	private Main plugin;
	public Map<Player, Team> players = new HashMap<Player, Team>();
	private int maxPlayers = 2;
	Location bboxCorner1, bboxCorner2;
	public int countdown = 0;
	BukkitTask task;
	private GameState gameState = GameState.OFFLINE;
	// false si el juego no ha empezado, true si ya empezo
	private int scoreT1 = 0, scoreT2 = 0;
	private Team team1, team2;
	@SupressWarnings("unused")
	private Location spawnT1, spawnT2, spawnG;

	public BoundingBox bridge, cageT1, cageT2;

	public BridgeManager(Main plugin) {
		this.plugin = plugin;
		loadConfig();
		for (Player p : Bukkit.getOnlinePlayers()) {
			updateScoreboard(p);
		}
	}

	public void loadConfig() {
		FileConfiguration config = plugin.bridgeConfig.getConfig();
		// TODO: Aprender a loggear los errores a la consola y mover esto a una clase
		// separada (BridgeConfig) con las respectivas variables de instancia que se
		// llame desde main
		// Antes que la creacion de BridgeManager

		boolean t1set = false, t2set = false;

		/* nuevo metodo: cargar las bounding boxes por la clase pasandole la config */
		try {
			bridge = new BoundingBox(config, "bridge", "bridge");
			cageT1 = new BoundingBox(config, "cage-team1", "cage team 1");
			cageT2 = new BoundingBox(config, "cage-team2", "cage team 2");
			Bukkit.broadcastMessage(Utils.chat("Cargadas bounding boxes con exito:: "));
			Bukkit.broadcastMessage(Utils.chat(bridge.toString()));
			Bukkit.broadcastMessage(Utils.chat(cageT1.toString()));
			Bukkit.broadcastMessage(Utils.chat(cageT2.toString()));
		} catch (Exception e) {
			Bukkit.broadcastMessage(Utils.error("No se pudieron cargar las bounding boxes"));
			e.printStackTrace();
			return;
		}

		try {
			String team1name = config.getString("team1name");
			String team2name = config.getString("team2name");
			for (Team t : Team.values()) {
				if (team1name.equalsIgnoreCase(t.getName()) || team1name.equalsIgnoreCase(t.getNombre())) {
					team1 = Team.valueOf(t.getEnumName());
					t1set = true;
					Bukkit.broadcastMessage(Utils.chat("TEAM 1: " + team1.getNombreMsg()));
				}
				if (team2name.equalsIgnoreCase(t.getName()) || team2name.equalsIgnoreCase(t.getNombre())) {
					team2 = Team.valueOf(t.getEnumName());
					t2set = true;
					Bukkit.broadcastMessage(Utils.chat("TEAM 2: " + team2.getNombreMsg()));
				}
			}
			if (t1set == false || t2set == false || team1 == team2) {
				Bukkit.broadcastMessage(Utils
						.error("No se pudieron cargar los equipos, equipos duplicados o no presentes en el archivo"));
				return;
			}
		} catch (NullPointerException e) {
			Bukkit.broadcastMessage(Utils.error("No se pudieron cargar las los equipos"));
			return;
		}

		try {
			spawnT1 = new Location(null, config.getDouble("team1-spawn.x"), config.getDouble("team1-spawn.y"),
					config.getDouble("team1-spawn.z"), (float) config.getDouble("team1-spawn.yaw"),
					(float) config.getDouble("team1-spawn.pitch"));
			this.plugin.log.log(Level.FINE, "Cargando locs, T1 cargada!");
			Bukkit.broadcastMessage("CARGA LOC SPAWN 1: " + spawnT1.toString());

			spawnT2 = new Location(null, config.getDouble("team2-spawn.x"), config.getDouble("team2-spawn.y"),
					config.getDouble("team2-spawn.z"), (float) config.getDouble("team2-spawn.yaw"),
					(float) config.getDouble("team2-spawn.pitch"));
			this.plugin.log.log(Level.FINE, "Cargando locs, T2 cargada!");
			Bukkit.broadcastMessage("CARGA LOC SPAWN 2: " + spawnT2.toString());

			spawnG = new Location(null, config.getDouble("general-spawn.x"), config.getDouble("general-spawn.y"),
					config.getDouble("general-spawn.z"), (float) config.getDouble("general-spawn.yaw"),
					(float) config.getDouble("general-spawn.pitch"));
		} catch (NullPointerException e) {
			Bukkit.broadcastMessage(Utils.error("No se pudieron cargar correctamente los spawns!"));
			return;
		}
		Bukkit.broadcastMessage(Utils.chat("&l&aSe ha cargado la configuracion con exito!"));
	}

	public void setMaxPlayers(int n) {
		maxPlayers = n;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void playerEntry(Player p) {

		if (gameState == GameState.RUNNING || gameState == GameState.COUNTDOWN) {
			p.sendMessage(Utils.pluginMsg("hasStarted"));
			return;
		}

		gameState = GameState.WAITING;

		// Primero comprobar si el equipo esta lleno
		if (players.size() >= maxPlayers) {
			p.sendMessage(Utils.pluginMsg("full"));
			return;
		}
		p.sendMessage(Utils.chat("Buscando equipo... "));

		// Contar cuantos hay ya en cada equipo y decidir a cual se unira el jugador
		int team1count = 0, team2count = 0, selectedTeam = 0;
		for (Map.Entry<Player, Team> e : players.entrySet()) {
			if (e.getValue() == team1) {
				team1count++;
			}
			if (e.getValue() == team2) {
				team2count++;
			}
		}
		if (team1count < team2count)
			selectedTeam = 1;
		else if (team1count > team2count)
			selectedTeam = 2;
		else {
			Random r = new Random();
			selectedTeam = r.nextInt(1) + 1;
		}
		if (selectedTeam == 1) {
			players.put(p, team1);
		}
		if (selectedTeam == 2) {
			players.put(p, team2);
		}

		p.sendMessage(Utils.chat("Te has unido al equipo: " + players.get(p).getNameMsg()));
		p.sendMessage(Utils.chat("Jugadores t1: " + team1count + " Jugadores t2: " + team2count + " Total: "
				+ players.size() + "/" + maxPlayers));
		updateScoreboard(p);
		attemptStart(false);
	}

	public void attemptStart(boolean sendMsg) {
		if (gameState != GameState.WAITING) {
			return;
		}

		if (players.size() != maxPlayers) {
			if (sendMsg == true) {
				msgAll("Se necesitan &b" + maxPlayers + " &fjugadores para iniciar la partida!");
			}
			return;
		}
		cageWaiting();
	}

	public void cageWaiting() {

		gameState = GameState.COUNTDOWN;
		// 1. Poner el estado del juego en jugando y resetear las puntuaciones
		scoreT1 = 0;
		scoreT2 = 0;

		// 2. Mensajear a los jugadores e inicializar el blocksaver
		World world = null;
		for (Player p : players.keySet()) {
			p.sendMessage(Utils.chat("Comenzando la partida!"));
			if (world == null)
				world = p.getWorld();
		}
		try {
			plugin.blockSaver.setWorld(world);
		} catch (NullPointerException e) {
			Bukkit.broadcastMessage("Error. No se pudo encontrar un mundo");
			return;
		}
		// 3. Guardar los estados de las cajas y mandar a los jugadores a ellas
		cageT1.saveBlocks(world);
		cageT2.saveBlocks(world);

		// 3. Crear el task de updates para correr cada 20 ticks (1 segundo, este dara
		// updates cuando se acabe un tiempo)
		// El countdown lo ponemos al starting time, y se actualizara cuando este acabe

		countdown = plugin.bridgeConfig.getConfig().getInt("starting-time");
		task = new BridgeUpdate(this.plugin, this).runTaskTimer(this.plugin, 0, 20);

		return;
	}

	public void gameStart() {
		msgAll("Comenzando partida!");
		task.cancel();
		gameState = GameState.RUNNING;
		// Quitar las cajas
		cageT1.clearBlocks();
		cageT2.clearBlocks();

		// Este se llamara cuando el countdown sea 0 en el BridgeUpdate
		countdown = plugin.bridgeConfig.getConfig().getInt("match-time");
		task = new BridgeUpdate(this.plugin, this).runTaskTimer(this.plugin, 0, 20);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "toggledownfall");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0");
		for (Player p : players.keySet()) {
			respawn(p);
		}
	}

	public void stop() {

		msgAll("Acabando partida!");
		// Bukkit.broadcastMessage("CANCELANDO TASK" + task.getTaskId());
		task.cancel();
		if (gameState == GameState.OFFLINE || gameState == GameState.WAITING) {
			return;
		}
		gameState = GameState.OFFLINE;
		cageT1.putBlocks();
		cageT2.putBlocks();
		msgAll("Partida acabada! Gracias por jugar!");
		for (Player p : players.keySet()) {
			playerExit(p, false);
			updateScoreboard(p);
		}

		plugin.blockSaver.putBlocks();
		players.clear();

		return;
	}

	public void goal(Team team) {

		// TEAM INDICA EL EQUIPO QUE MARCO EL GOL, SI TEAM ES 0 ES EL GOL DE INICIO

		if (team == team1) {
			scoreT1++;
		}
		if (team == team2) {
			scoreT2++;
		}

		msgAll(Utils.chat("El equipo " + team.getNameMsg() + " ha marcado un gol!"));

		if (scoreT1 == 5 || scoreT2 == 5) {
			victory(team);
		}
		cageT1.putBlocks();
		cageT2.putBlocks();
		// TPEAR A LOS JUGADORES A SUS POSICIONES, HEALEARLOS, MANDAR FEEDBACK DEL GOL
		for (Player p : players.keySet()) {
			respawn(p);
			updateScoreboard(p);
		}
	}

	public void respawn(Player p) {
		if (players.get(p) == team1) {
			spawnT1.setWorld(p.getWorld());
			p.teleport(spawnT1);
			giveInventory(p, team1);
			Utils.heal(p);
		}
		if (players.get(p) == team2) {
			spawnT2.setWorld(p.getWorld());
			p.teleport(spawnT2);
			giveInventory(p, team2);
			Utils.heal(p);
		}
	}

	private void giveInventory(Player p, Team team) {

		p.getInventory().clear();
		// BASICOS

		ItemStack item = new ItemStack(Material.STONE_SWORD, 1);
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		p.getInventory().setItem(0, item);

		item.setType(Material.BOW);
		p.getInventory().setItem(1, item);

		item.setType(Material.GOLDEN_APPLE);
		item.setAmount(8);
		p.getInventory().setItem(3, item);

		item.setType(Material.DIAMOND_PICKAXE);
		item.setAmount(1);
		meta.addEnchant(Enchantment.DIG_SPEED, 3, true);
		item.setItemMeta(meta);
		p.getInventory().setItem(5, item);

		item.setType(Material.ARROW);
		item.setAmount(1);
		p.getInventory().setItem(8, item);

		// ARMADURA DEFAULT
		ItemStack[] armor = new ItemStack[4];
		armor[0] = new ItemStack(Material.LEATHER_BOOTS);
		armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
		armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
		armor[3] = new ItemStack(Material.LEATHER_HELMET);
		for (ItemStack armorpiece : armor) {
			armorpiece.setItemMeta(meta);
		}

		p.getEquipment().setArmorContents(armor);

		if (team == team1) {
			// BLOQUES
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) team.getID());
			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			// COLOREAR ARMADURA
			armor = Utils.changeColor(armor, team.getColor());
			p.getEquipment().setArmorContents(armor);

		}

		else if (team == team2) {
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) team.getID());

			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			armor = Utils.changeColor(armor, team.getColor());
			p.getEquipment().setArmorContents(armor);

		}

	}

	private void victory(Team team) {
		// TODO: Secuencia de victoria a los 5 goles de un equipo

		msgAll("El equipo &l " + team.getNameMsg() + "&r&fha ganado!");
		stop();
	}

	public GameState getGamePhase() {
		return gameState;
	}

	public String getPhaseDescription() {
		if (this.gameState == GameState.COUNTDOWN)
			return "empezar";
		if (this.gameState == GameState.RUNNING)
			return "terminar";
		return null;
	}

	public void msgAll(String msg) {
		for (Player p : players.keySet()) {
			p.sendMessage(Utils.chat(msg));
		}
	}

	public void playerExit(Player p, boolean checkList) {

		// Llamado cuando un jugador sale del juego, limpiar inventario, teleportar
		// fuera, feedback
		p.getInventory().clear();
		spawnG.setWorld(p.getWorld());
		p.teleport(spawnG);
		p.sendMessage(Utils.chat("Has salido del juego!"));
		if (checkList == true)
			checkList();
	}

	public void playerExit(Player p) {
		// TODO: Configurar secuencia de salida, quitar score, coger spawn central
		p.getInventory().clear();
		spawnG.setWorld(p.getWorld());
		p.teleport(spawnG);
		p.sendMessage(Utils.chat("Has salido del juego!"));
		players.remove(p);
		checkList();

	}

	public void checkList() {
		if (players.size() <= 1) {
			msgAll("No hay jugadores suficientes! Abortando la partida...");
			stop();
		}
	}

	public void updateScoreboard(Player p) {
		// TODO: Arreglar este sistema, aun no funciona

		Scoreboard board = p.getScoreboard();
		try {
			Objective oldObj = board.getObjective("Bridge-1");
			oldObj.unregister();
		} catch (Exception e) {
			Bukkit.broadcastMessage("first time");
		}
		Objective obj = board.registerNewObjective("Bridge-1", "dummy");
		obj.setDisplayName(Utils.color("&6&lThe &2&lBridge"));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score pname = obj.getScore(Utils.color("Jugador: " + p.getName()));
		pname.setScore(1);

		String minutos = String.valueOf(countdown / 60);
		String segundos = String.valueOf(countdown % 60);

		StringBuffer minutosFormatted = new StringBuffer(minutos);
		StringBuffer segundosFormatted = new StringBuffer(segundos);
		if (minutos.length() == 1) {
			minutosFormatted.insert(0, "0");
		}

		if (segundos.length() == 1) {
			segundosFormatted.insert(0, "0");
		}
		String timeLeft = minutosFormatted + ":" + segundosFormatted;

		Score separator1, separator2, separator3, separator4, info1, info2, info3, info4, info5;
		separator1 = obj.getScore(Utils.color("&7____"));
		separator2 = obj.getScore(Utils.color("&7____"));
		separator3 = obj.getScore(Utils.color("&7____"));
		separator4 = obj.getScore(Utils.color("&7____"));

		switch (gameState) {
			case OFFLINE:
				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				return;
			case WAITING:
				info1 = obj.getScore(Utils.color("Esperando jugadores..."));
				info1.setScore(4);
				separator1.setScore(3);
				info2 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info2.setScore(2);
				separator1.setScore(1);
				break;
			case COUNTDOWN:
				info1 = obj.getScore(Utils.color("Empezando en: &b" + timeLeft));
				info1.setScore(5);
				separator1.setScore(4);
				info2 = obj.getScore(Utils.color("Equipo: &l" + players.get(p).getNombreMsg()));
				info2.setScore(3);
				separator2.setScore(2);
				info3 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info3.setScore(1);
				separator3.setScore(0);
				break;
			case RUNNING:
				info1 = obj.getScore(Utils.color("Tiempo restante en: &b" + timeLeft));
				info1.setScore(8);
				separator1.setScore(7);
				info2 = obj.getScore(Utils.color("Goles equipo " + team1.getNombreMsg() + ": &b" + scoreT1 + "/5"));
				info3 = obj.getScore(Utils.color("Goles equipo " + team1.getNombreMsg() + ": &b" + scoreT2 + "/5"));
				info2.setScore(6);
				info3.setScore(5);
				separator2.setScore(4);
				info4 = obj.getScore(Utils.color("Tu equipo: &l" + players.get(p).getNombreMsg()));
				info4.setScore(3);
				separator3.setScore(2);
				info5 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info5.setScore(1);
				separator4.setScore(0);
				break;
		}
		p.setScoreboard(board);
	}

	public void cageTest(Team team) {
		new BukkitRunnable() {
			Player p = null;
			BoundingBox bbox = null;
			int phase = 0;

			@Override
			public void run() {
				if (phase == 0) {
					if (team == team1)
						bbox = cageT1;
					if (team == team2)
						bbox = cageT2;
					for (Player player : players.keySet()) {
						// buscar mundo, TODO automatizar esto con una variable global
						p = player;
					}
					bbox.saveBlocks(p.getWorld());
				}
				if (phase == 1)
					bbox.clearBlocks();
				if (phase == 2) {
					bbox.putBlocks();
					this.cancel();
				}
				phase++;
			}
		}.runTaskTimer(this.plugin, 0, 60);
	}

}