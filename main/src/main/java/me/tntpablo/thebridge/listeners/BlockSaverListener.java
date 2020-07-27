package me.tntpablo.thebridge.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;

import me.tntpablo.thebridge.BridgeManager;
import me.tntpablo.thebridge.Main;

public class BlockSaverListener implements Listener {

	private Main plugin;
	private BridgeManager bridge;

	public BlockSaverListener(Main plugin, BridgeManager bridge) {
		this.plugin = plugin;
		this.bridge = bridge;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (bridge.getGamePhase() == 2)
			plugin.blockSaver.saveBlock(event.getBlock());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {

		if (bridge.getGamePhase() == 2)
			plugin.blockSaver.saveBlock(event.getBlock(), true);

	}

	// AUN NO FUNCIONA, ES INDIFERENTE PARA BRIDGES
	@EventHandler
	public void onExplosion(BlockExplodeEvent event) {
		if (bridge.getGamePhase() == 2) {
			for (Block block : event.blockList()) {
				plugin.blockSaver.saveBlock(block);
			}
		}
	}

	@EventHandler
	public void onBurn(BlockBurnEvent event) {
		if (bridge.getGamePhase() == 2)
			plugin.blockSaver.saveBlock(event.getBlock());
	}

	@EventHandler
	public void onDecay(LeavesDecayEvent event) {
		if (bridge.getGamePhase() == 2)
			plugin.blockSaver.saveBlock(event.getBlock());
	}
}
