package me.tntpablo.thebridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
	public Map<Player, Integer> players = new HashMap<Player, Integer>();
	private int maxPlayers = 2;
	Location t1Spawn, t2Spawn;
	public int countdown = 0;
	BukkitTask task;
	private int gamePhase = 0;

	// false si el juego no ha empezado, true si ya empezo
	private boolean gameState = false;
	private int scoreT1 = 0, scoreT2 = 0;

	public BridgeManager(Main plugin) {
		this.plugin = plugin;
	}

	public void setMaxPlayers(int n) {
		maxPlayers = n;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void addPlayer(Player p) {

		if (gameState == true) {
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

		for (Map.Entry<Player, Integer> e : players.entrySet()) {
			if (e.getValue() == 1)
				team1count++;
			if (e.getValue() == 2)
				team2count++;
		}

		if (team1count < team2count)
			selectedTeam = 1;
		else if (team1count > team2count)
			selectedTeam = 2;
		else {
			Random r = new Random();
			selectedTeam = r.nextInt(1) + 1;
		}
		p.sendMessage(Utils.chat("Te has unido al equipo &l" + selectedTeam));
		players.put(p, selectedTeam);
		updateScoreboard(p);
	}

	public void attempStart(boolean sendMsg) {
		if (gameState == true) {
			return;
		}

		if (players.size() != maxPlayers) {
			if (sendMsg == true) {
				for (Map.Entry<Player, Integer> e : players.entrySet()) {
					e.getKey().sendMessage(Utils.chat("Se necesitan" + maxPlayers + "para comenzar la partida!"));
				}
			}
			return;
		}
		preGameStart();
	}

	public void preGameStart() {

		gamePhase = 1;
		// 1. Poner el estado del juego en jugando y resetear las puntuaciones
		gameState = true;
		scoreT1 = 0;
		scoreT2 = 0;

		// 2. Mensajear a los jugadores e inicializar el blocksaver
		World world = null;
		int i = 0;
		for (Map.Entry<Player, Integer> e : players.entrySet()) {
			e.getKey().sendMessage(Utils.chat("Comenzando la partida!"));
			if (i == 0)
				world = e.getKey().getWorld();
			i++;
		}

		plugin.blockSaver.setWorld(world);

		// 3. Configurar el spawn de los jugadores
		// TODO: Coger el spawn de la configuracion

		t1Spawn = new Location(world, 22.5, 95.0, 30.5, 90.0f, 0.0f);
		t2Spawn = new Location(world, -21.5, 95.0, 30.5, -90.0f, 0.0f);

		// 4. Crear el task de updates para correr cada 20 ticks (1 segundo, este dar�
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
		goal(0);
	}

	public void stop() {

		msgAll("Acabando partida!");
		// Bukkit.broadcastMessage("CANCELANDO TASK" + task.getTaskId());
		task.cancel();
		if (gameState == false) {
			return;
		}
		gamePhase = 0;
		gameState = false;

		msgAll("Partida acabada! Gracias por jugar!");
		for (Player p : players.keySet()) {
			updateScoreboard(p);
			playerExit(p, false);
		}
		plugin.blockSaver.putBlocks();
		players.clear();

		return;
	}

	public void goal(int team) {

		// TEAM INDICA EL EQUIPO QUE MARCO EL GOL, SI TEAM ES 0 ES EL GOL DE INICIO

		if (team == 1)
			scoreT1++;
		if (team == 2)
			scoreT2++;
		if (scoreT1 == 5) {
			// VICTORIA EQUIPO 1
			victory(1);
		}
		if (scoreT2 == 5) {
			// VICTORIA EQUIPO 2
			victory(2);
		}

		// TPEAR A LOS JUGADORES A SUS POSICIONES, HEALEARLOS, MANDAR FEEDBACK DEL GOL

		if (team == 1 || team == 2)
			msgAll("El equipo &l" + team + " ha marcado un gol!");

		for (Map.Entry<Player, Integer> e : players.entrySet()) {
			if (e.getValue() == 1)
				e.getKey().teleport(t1Spawn);
			else if (e.getValue() == 2)
				e.getKey().teleport(t2Spawn);

			giveInventory(e.getKey(), e.getValue());
			Utils.heal(e.getKey());
			updateScoreboard(e.getKey());
		}
	}

	public void death(Player p) {
		if (players.get(p) == 1) {
			p.teleport(t1Spawn);
			giveInventory(p, 1);
			Utils.heal(p);
		}
		if (players.get(p) == 2) {
			p.teleport(t2Spawn);
			giveInventory(p, 2);
			Utils.heal(p);
		}
	}

	private void giveInventory(Player p, int team) {

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

		if (team == 1) {
			// BLOQUES
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) 11);
			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			// COLOREAR ARMADURA
			armor = Utils.changeColor(armor, Color.BLUE);
			p.getEquipment().setArmorContents(armor);

		}

		else if (team == 2) {
			ItemStack clay = new ItemStack(Material.STAINED_CLAY, 64, (short) 14);

			p.getInventory().setItem(2, clay);
			p.getInventory().setItem(4, clay);

			armor = Utils.changeColor(armor, Color.RED);
			p.getEquipment().setArmorContents(armor);

		}

	}

	private void victory(int team) {
		// TODO: Secuencia de victoria a los 5 goles de un equipo

		msgAll("El equipo &l " + team + "&r&fha ganado!");
		stop();
	}

	public boolean getGameState() {
		return gameState;
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

		p.getInventory().clear();

		Location loc = new Location(p.getWorld(), plugin.bridgeConfig.getConfig().getDouble("spawnX"),
				plugin.bridgeConfig.getConfig().getDouble("spawnY"),
				plugin.bridgeConfig.getConfig().getDouble("spawnZ"));
		p.teleport(loc);
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


		Score info, blankSpace, info3, info4, info5, info6;
		blankSpace = obj.getScore(Utils.color(" "));


		switch (this.gamePhase) {
			case 0:
				info3 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info3.setScore(2);
				break;
			case 1:
				blankSpace.setScore(2);
				info = obj.getScore(Utils.color("Empezando en: &b" + timeLeft));
				info.setScore(3);
				blankSpace.setScore(4);
				info4 = obj.getScore(Utils.color("Equipo: &l" + players.get(p)));
				info4.setScore(5);
				blankSpace.setScore(6);
				info3 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info3.setScore(7);


				break;
			case 2:
				blankSpace.setScore(2);
				info5 = obj.getScore(Utils.color("Goles equipo 1: &b" + scoreT1));
				info6 = obj.getScore(Utils.color("Goles equipo 2: &b" + scoreT2));
				info5.setScore(3);
				info6.setScore(4);


				info = obj.getScore(Utils.color("Empezando en: &b" + timeLeft));
				info.setScore(6);
				blankSpace.setScore(7);

				info4 = obj.getScore(Utils.color("Equipo: &l" + players.get(p)));
				info4.setScore(8);

				blankSpace.setScore(9);

				info3 = obj.getScore(Utils.color("Jugadores: &b" + players.size() + "/" + maxPlayers));
				info3.setScore(10);
				break;
		}
		p.setScoreboard(board);
	}
}
