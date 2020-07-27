package me.tntpablo.thebridge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


public class GiveArrow extends BukkitRunnable {

	Main plugin;
	Player p;
	public GiveArrow(Main plugin, Player p) {
		this.plugin= plugin;
		this.p = p;

	}

	@Override
	public void run() {
		p.getInventory().addItem(new ItemStack(Material.ARROW,1));
	}

}
