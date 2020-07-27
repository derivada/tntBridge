package me.tntpablo.thebridge.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.tntpablo.thebridge.BridgeManager;
import me.tntpablo.thebridge.Main;
import me.tntpablo.thebridge.Utils;

public class BridgeListener implements Listener {

	private Main plugin;
	private BridgeManager bridge;
	private Map<Player, Long> goalDelay = new HashMap<Player, Long>();
	private Map<Player, Long> moveDelay = new HashMap<Player, Long>();

	public BridgeListener(Main plugin, BridgeManager bridge) {
		this.plugin = plugin;
		this.bridge = bridge;
		this.goalDelay.clear();
		this.moveDelay.clear();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.FALL) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onGoal(PlayerPortalEvent e) {

		Player p = e.getPlayer();
		if (e.getCause() == TeleportCause.END_PORTAL) {

			if (bridge.players.containsKey(p) || (goalDelay.get(p) != null && (goalDelay.get(p) + 1000) >= System.currentTimeMillis())) {
					e.setCancelled(true);
					if (goalDelay.get(p) != null && (goalDelay.get(p) + 1000) >= System.currentTimeMillis()) {
						return;
					}

					goalDelay.put(p, System.currentTimeMillis());

					if (bridge.players.get(e.getPlayer()) == 1)
						bridge.goal(1);
					if (bridge.players.get(e.getPlayer()) == 2)
						bridge.goal(2);

			}

		}
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			for (Player pl : bridge.players.keySet()) {
				Bukkit.broadcastMessage(pl.getName());
			}
			if (bridge.players.containsKey(p)) {
				if (bridge.getGamePhase() == 2) {
					// Nuevo thread que espera 3 segundos y despues le da una flecha al jugador
					// asociado
					new BukkitRunnable() {
						int exp = 60;

						@Override
						public void run() {
							p.setExp((float) (exp / 59.0));
							exp--;
							if (exp == 0) {
								p.setExp(0.0f);
								this.cancel();
							}
						}

					}.runTaskTimer(this.plugin, 0, 1);
					new BukkitRunnable() {

						@Override
						public void run() {
							p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
						}

					}.runTaskLater(this.plugin, 60);
				}
			}
		}
	}

	@EventHandler
	public void onArrowLand(ProjectileHitEvent e) {
		if (e.getEntityType() == EntityType.ARROW) {
			if (bridge.getGamePhase() == 2) {
				new BukkitRunnable() {

					@Override
					public void run() {
						e.getEntity().remove();
					}

				}.runTaskLater(this.plugin, 1);
			}
		}
	}

	@EventHandler
	public void onFall(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (moveDelay.get(p) == null || (moveDelay.get(p) + 1000) < System.currentTimeMillis())
			if (bridge.players.keySet().contains(p))
				if (bridge.getGamePhase() == 2) {
					moveDelay.put(p, System.currentTimeMillis());

					if (e.getPlayer().getLocation().getY() < plugin.bridgeConfig.getConfig().getInt("death-height")) {
						bridge.death(p);
						p.sendMessage(Utils.chat("Has muerto por caida!"));
					}
				}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == 2)
				// Mi mayor obra maestra
				if (!(plugin.bridgeConfig.getConfig().getInt("X1") <= e.getBlock().getLocation().getX()
						&& plugin.bridgeConfig.getConfig().getInt("Y1") <= e.getBlock().getLocation().getY()
						&& plugin.bridgeConfig.getConfig().getInt("Z1") <= e.getBlock().getLocation().getZ()
						&& plugin.bridgeConfig.getConfig().getInt("X2") >= e.getBlock().getLocation().getX()
						&& plugin.bridgeConfig.getConfig().getInt("Y2") >= e.getBlock().getLocation().getY()
						&& plugin.bridgeConfig.getConfig().getInt("Z2") >= e.getBlock().getLocation().getZ()))
					e.setCancelled(true);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == 2)
				e.setCancelled(true);
	}

	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == 2) {
				// sacadisimo de internet, es un lambda que revive al jugador en 2 ticks
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> p.spigot().respawn(), 2);
				bridge.death(p);
			}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == 2)
				if (e.getBlock().getType() == Material.STAINED_CLAY)
					e.setCancelled(true);

	}

	@EventHandler
	public void onPlayerLeaving(PlayerQuitEvent e){
		Player p = e.getPlayer();
		if(bridge.players.keySet().contains(p)){
			if(bridge.getGamePhase()==2 || bridge.getGamePhase()==1)
				bridge.checkList();
		}
	}
}