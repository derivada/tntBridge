package me.tntpablo.thebridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.tntpablo.thebridge.files.DataManager;
import me.tntpablo.thebridge.listeners.BlockSaverListener;
import me.tntpablo.thebridge.listeners.BridgeListener;

public class Main extends JavaPlugin {

	public BlockSaver blockSaver;
	public BlockSaverListener blockSaverListener;
	public DataManager bridgeConfig = new DataManager(this, "bridgeconfig.yml");
	public BridgeManager bridgeManager = new BridgeManager(this);
	public BridgeListener bridgeListener;
	public BridgeListener auxListener;

	@Override
	public void onEnable() {
		commandManager();
		blockSaverListener = new BlockSaverListener(this, bridgeManager);
		blockSaver = new BlockSaver(this, bridgeManager);
		auxListener = new BridgeListener(this, bridgeManager);
	}

	@Override
	public void onDisable() {
	}

	public void commandManager() {
		this.getCommand("bridge").setExecutor(new BridgeCmd(this));
		this.getCommand("bridge").setTabCompleter(new BridgeTab());
	}

}
