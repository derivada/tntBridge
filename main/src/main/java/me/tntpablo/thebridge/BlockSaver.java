package me.tntpablo.thebridge;

import java.util.HashMap;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class BlockSaver {

	// Aqui se almacenan y manipulan las posiciones de los bloques puestos

	@SuppressWarnings("unused")
	private Main plugin;
	private World world;
	private BridgeManager bridge;
	private HashMap<Location, Material> oldBlock;
	private HashMap<Location, Byte> oldBlockData;

	public BlockSaver(Main plugin, BridgeManager bridge) {
		oldBlock = new HashMap<Location, Material>();
		oldBlockData = new HashMap<Location, Byte>();
		this.bridge = bridge;
		this.plugin = plugin;
		this.world = null;
	}

	public void saveBlock(Block block) {
		//Bukkit.broadcastMessage("Guardado bloque" + block.getType().toString() + "en " + block.getLocation().toString());
		oldBlock.putIfAbsent(block.getLocation(), block.getType());
		if (block.getType() == Material.STAINED_CLAY) {
			oldBlockData.putIfAbsent(block.getLocation(), block.getData());
		}
	}

	public void saveBlock(Block block, boolean placedFlag) {
		if (placedFlag == true)
			oldBlock.putIfAbsent(block.getLocation(), Material.AIR);
		else
			saveBlock(block);
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void putBlocks() {
		if (world == null) {
			return;
		}
		bridge.msgAll("Regenerando mapa...");

		for (Map.Entry<Location, Material> entry : oldBlock.entrySet()) {
			//Bukkit.broadcastMessage("Guardado bloque" + entry.getValue().toString() + "en " + entry.getKey().toString());
			entry.getKey().getBlock().setType(entry.getValue());
			if (entry.getValue() == Material.STAINED_CLAY)
				entry.getKey().getBlock().setData(oldBlockData.get(entry.getKey()));
		}
		bridge.msgAll("Mapa regenerado!");

		oldBlock.clear();
	}
}
