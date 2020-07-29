package me.tntpablo.thebridge;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.tntpablo.thebridge.files.DataManager;
import me.tntpablo.thebridge.listeners.BlockSaverListener;
import me.tntpablo.thebridge.listeners.BridgeListener;

public class Main extends JavaPlugin {

	Logger log = Bukkit.getLogger();
	public BlockSaver blockSaver;
	public BlockSaverListener blockSaverListener;
	public DataManager bridgeConfig = new DataManager(this, "bridgeconfig.yml");
	public BridgeManager bridgeManager = new BridgeManager(this);
	public BridgeListener bridgeListener;
	public BridgeListener auxListener;
	@Override
	public void onEnable() {
		log.info("CARGANDO PLUGIN TEST TEST TEST MARICON MARICON MARICON");
		log.log(Level.SEVERE, "TEST DEL LOGGER NUMERO 2 (SEVERO)");
		commandManager();
		blockSaverListener = new BlockSaverListener(this, bridgeManager);
		blockSaver = new BlockSaver(this, bridgeManager);
		auxListener = new BridgeListener(this, bridgeManager);
		for(Player p: Bukkit.getOnlinePlayers()){
			bridgeManager.updateScoreboard(p);
		}
	}

	@Override
	public void onDisable() {
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
