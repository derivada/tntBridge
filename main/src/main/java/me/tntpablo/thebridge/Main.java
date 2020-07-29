package me.tntpablo.thebridge;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.tntpablo.thebridge.files.DataManager;
import me.tntpablo.thebridge.listeners.BlockSaverListener;
import me.tntpablo.thebridge.listeners.BridgeListener;

public class Main extends JavaPlugin {

	public Logger logger;
	public BlockSaver blockSaver;
	public BlockSaverListener blockSaverListener;
	
	public DataManager bridgeConfig;
	public BridgeManager bridgeManager;

	public BridgeListener bridgeListener;
	public BridgeListener auxListener;

	@Override
	public void onEnable() {
		logger = Bukkit.getLogger();
		logger.info("CARGANDO PLUGIN TEST TEST TEST MARICON MARICON MARICON");
		logger.log(Level.SEVERE, "TEST DEL LOGGER NUMERO 2 (SEVERO)");
		
		bridgeManager = new BridgeManager(this);
		bridgeManager.loadConfig();

		commandManager();

		blockSaverListener = new BlockSaverListener(this, bridgeManager);
		blockSaver = new BlockSaver(this, bridgeManager);
		auxListener = new BridgeListener(this, bridgeManager);

	}

	@Override
	public void onDisable() {
		// Si se ha parado el plugin, intentar poner los bloques
		try{
			this.blockSaver.putBlocks();
		}catch(Exception e){
		}
		try{
			this.bridgeManager.cageT1.putBlocks();
			this.bridgeManager.cageT2.putBlocks();
		}catch(Exception e){
		}
	}

	public void commandManager() {
		this.getCommand("bridge").setExecutor(new BridgeCmd(this));
		this.getCommand("bridge").setTabCompleter(new BridgeTab());
	}

}
