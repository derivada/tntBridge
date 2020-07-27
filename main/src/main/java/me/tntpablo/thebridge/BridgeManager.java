package me.tntpablo.thebridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BridgeManager {

	private Main plugin;
	public Map<Player, Team> players = new HashMap<Player, Team>();
	private int maxPlayers = 2;
	Location  bboxCorner1, bboxCorner2;
	public int countdown = 0;
	BukkitTask task;
	private int gamePhase = 0;
	// false si el juego no ha empezado, true si ya empezo
	private int scoreT1 = 0, scoreT2 = 0;
	private Team team1, team2;
	@SupressWarnings("unused")
	private Location spawnT1, spawnT2, spawnG;

	public BridgeManager(Main plugin) {
		this.plugin = plugin;
		loadConfig();
	}

	public void loadConfig() {
		FileConfiguration config = plugin.bridgeConfig.getConfig();

		// Equipos
		String team1name = config.getString("team1name");
		String team2name = config.getString("team2name");
		boolean t1set = false, t2set = false;
		try {
			for (Team t : Team.values()) {
				if (team1name.equalsIgnoreCase(t.getName()) || team1name.equalsIgnoreCase(t.getNombre())) {
					team1 = Team.valueOf(t.getEnumName());
					t1set = true;
				}
				if (team2name.equalsIgnoreCase(t.getName()) || team2name.equalsIgnoreCase(t.getNombre())) {
					team2 = Team.valueOf(t.getEnumName());
					t2set = true;
				}
			}
			if (t1set == false || t2set == false || team1 == team2) {
				this.plugin.log.log(Level.SEVERE, "No se pudieron cargar correctamente los equipos!");
				return;
			}
		} catch (NullPointerException e) {
			this.plugin.log.log(Level.SEVERE, "No se pudieron cargar correctamente los equipos!");
			return;
		}

		// TODO: Locations, BBOx y spawns (usar config section)

		try {
			this.plugin.log.log(Level.FINE, "Cargando locs!");
			spawnT1 = new Location(null, config.getDouble("team1-spawn.x"), config.getDouble("team1-spawn.y"),
					config.getDouble("team1-spawn.z"), (float) config.getDouble("team1-spawn.yaw"),
					(float) config.getDouble("team1-spawn.pitch"));
			this.plugin.log.log(Level.FINE, "Cargando locs, T1 cargada!");

			spawnT2 = new Location(null, config.getDouble("team2-spawn.x"), config.getDouble("team2-spawn.y"),
					config.getDouble("team2-spawn.z"), (float) config.getDouble("team2-spawn.yaw"),
					(float) config.getDouble("team2-spawn.pitch"));
			this.plugin.log.log(Level.FINE, "Cargando locs, T2 cargada!");

			spawnG = new Location(null, config.getDouble("general-spawn.x"), config.getDouble("general-spawn.y"),
					config.getDouble("general-spawn.z"), (float) config.getDouble("general-spawn.yaw"),
					(float) config.getDouble("general-spawn.pitch"));
			this.plugin.log.log(Level.FINE, "Cargando locs, General cargada!");

		} catch (NullPointerException e) {
			this.plugin.log.log(Level.SEVERE, "No se pudieron cargar correctamente los spawns y la bounding box!");
			return;
		}
	}

	public void setMaxPlayers(int n) {
		maxPlayers = n;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void addPlayer(Player p) {

		if (gamePhase != 0) {
			p.sendMessage(Utils.pluginMsg("hasStarted"));
			return;
		}

		// Primero comprobar si el equipo esta lleno
		if (players.size() >= maxPlayers) {
			p.sendMessage(Utils.pluginMsg("full"));
			return;
		}

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
		if (selectedTeam == 1)
			players.put(p, team1);

		if (selectedTeam == 1)
			players.put(p, team2);

		p.sendMessage(Utils.chat("Te has unido al equipo: " + players.get(p).getNameMsg()));
		updateScoreboard(p);
	}

	public void attempStart(boolean sendMsg) {
		if (gamePhase != 0) {
			return;
		}

		if (players.size() != maxPlayers) {
			if (sendMsg == true) {
				msgAll("Se necesitan &b" + maxPlayers + " &fjugadores para iniciar la partida!");
			}
			return;
		}
		preGameStart();
	}

	public void preGameStart() {

		gamePhase = 1;
		// 1. Poner el estado del juego en jugando y resetear las puntuaciones
		scoreT1 = 0;
		scoreT2 = 0;

		// 2. Mensajear a los jugadores e inicializar el blocksaver
		World world = null;
		int i = 0;
		for (Player p : players.keySet()) {
			p.sendMessage(Utils.chat("Comenzando la partida!"));
			if (i == 0)
				world = p.getWorld();
			i++;
		}

		plugin.blockSaver.setWorld(world);

		// 3. Configurar el spawn de los jugadores
		// TODO: Coger el spawn de la configuracion

		spawnT1.setWorld(world);
		spawnT2.setWorld(world);

		// 4. Crear el task de updates para correr cada 20 ticks (1 segundo, este dara
		// updates cuando se acabe un tiempo)
		// El countdown lo ponemos al starting time, y se actualizara cuando este acabe

		countdown = plugin.bridgeConfig.getConfig().getInt("starting-time");
		task = new BridgeUpdate(this.plugin, this).runTaskTimer(this.plugin, 0, 20);

		return;
	}

	public void gameStart() {
		msgAll("Comenzando partida!");
		task.cancel();
		gamePhase = 2;

		// Este se llamara cuando el countdown sea 0 en el BridgeUpdate
		countdown = plugin.bridgeConfig.getConfig().getInt("match-time");
		task = new BridgeUpdate(this.plugin, this).runTaskTimer(this.plugin, 0, 20);

		for (Player p : players.keySet()) {
			respawn(p);
		}
	}

	public void stop() {

		msgAll("Acabando partida!");
		// Bukkit.broadcastMessage("CANCELANDO TASK" + task.getTaskId());
		task.cancel();
		if (gamePhase == 0) {
			return;
		}
		gamePhase = 0;

		msgAll("Partida acabada! Gracias por jugar!");
		for (Player p : players.keySet()) {
			updateScoreboard(p);
			playerExit(p, false);
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

		// TPEAR A LOS JUGADORES A SUS POSICIONES, HEALEARLOS, MANDAR FEEDBACK DEL GOL
		for (Player p : players.keySet()) {
			respawn(p);
			updateScoreboard(p);
		}
	}

	public void respawn(Player p) {
		if (players.get(p) == team1) {
			p.teleport(spawnT1);
			giveInventory(p, team1);
			Utils.heal(p);
		}
		if (players.get(p) == team2) {
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
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) 11);
			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			// COLOREAR ARMADURA
			armor = Utils.changeColor(armor, Color.BLUE);
			p.getEquipment().setArmorContents(armor);

		}

		else if (team == team2) {
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) 14);

			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			armor = Utils.changeColor(armor, Color.RED);
			p.getEquipment().setArmorContents(armor);

		}

	}

	private void victory(Team team) {
		// TODO: Secuencia de victoria a los 5 goles de un equipo

		msgAll("El equipo &l " + team.getNameMsg() + "&r&fha ganado!");
		stop();
	}

	public int getGamePhase() {
		return gamePhase;
	}

	public String getPhaseDescription() {
		if (this.gamePhase == 1)
			return "empezar";
		if (this.gamePhase == 2)
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

		Location loc = new Location(p.getWorld(), plugin.bridgeConfig.getConfig().getDouble("spawnX"),
				plugin.bridgeConfig.getConfig().getDouble("spawnY"),
				plugin.bridgeConfig.getConfig().getDouble("spawnZ"));
		p.teleport(loc);
		p.sendMessage(Utils.chat("Has salido del juego!"));
		players.remove(p);
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

		switch (this.gamePhase) {
			case 0:
				info1 = obj.getScore(Utils.color("Esperando jugadores..."));
				info1.setScore(4);
				separator1.setScore(3);
				info2 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info2.setScore(2);
				separator1.setScore(1);
				break;
			case 1:
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
			case 2:
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
}