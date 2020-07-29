package me.tntpablo.thebridge.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.tntpablo.thebridge.BridgeManager;
import me.tntpablo.thebridge.GamePhase;
import me.tntpablo.thebridge.Main;
import me.tntpablo.thebridge.Team;
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

			if (bridge.players.containsKey(p)
					|| (goalDelay.get(p) != null && (goalDelay.get(p) + 1000) >= System.currentTimeMillis())) {
				e.setCancelled(true);
				if (goalDelay.get(p) != null && (goalDelay.get(p) + 1000) >= System.currentTimeMillis()) {
					return;
				}

				goalDelay.put(p, System.currentTimeMillis());
				Team team = bridge.players.get(p);
				bridge.goal(team);
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
				if (bridge.getGamePhase() == GamePhase.RUNNING) {
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
			if (bridge.getGamePhase() == GamePhase.RUNNING) {
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
				if (bridge.getGamePhase() == GamePhase.RUNNING) {
					moveDelay.put(p, System.currentTimeMillis());

					if (e.getPlayer().getLocation().getY() < plugin.bridgeConfig.getConfig().getInt("death-height")) {
						bridge.respawn(p);
						p.sendMessage(Utils.chat("Has muerto por caida!"));
					}
				}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == GamePhase.RUNNING || bridge.getGamePhase() == GamePhase.WAITING)
				if (!bridge.bridge.isInside(e.getBlock().getLocation())
						|| !(e.getBlock().getType() == Material.STAINED_CLAY))
					e.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == GamePhase.RUNNING || bridge.getGamePhase() == GamePhase.WAITING)
				if (!bridge.bridge.isInside(e.getBlock().getLocation())
						|| !(e.getBlock().getType() == Material.STAINED_CLAY))
					e.setCancelled(true);

	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p))
			if (bridge.getGamePhase() == GamePhase.RUNNING || bridge.getGamePhase() == GamePhase.WAITING)
				e.setCancelled(true);
	}

	// TODO: Acabar esto
	@EventHandler
	public void onDeath(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (bridge.players.keySet().contains(p))
				if (bridge.getGamePhase() == GamePhase.RUNNING) {
					// Comprobar si va a morir por ese hit:
					if (p.getHealth() < e.getDamage()) {
						e.setCancelled(true);
						Bukkit.broadcastMessage("TEST");
						p.playSound(p.getLocation(), Sound.HURT_FLESH, 1f, 1f);
						bridge.respawn(p);
					}
				}
		}
	}

	@EventHandler
	public void onPlayerLeaving(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (bridge.players.keySet().contains(p)) {
			if (!(bridge.getGamePhase() != GamePhase.OFFLINE))
				bridge.checkList();
		}
	}

	@EventHandler
	public void onPlayerEating(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.GOLDEN_APPLE) {
			Player p = e.getPlayer();
			if (bridge.players.keySet().contains(p)) {
				if (bridge.getGamePhase() == GamePhase.WAITING || bridge.getGamePhase() == GamePhase.RUNNING) {
					new BukkitRunnable() {
						// Quitar la regeneracion y healear al jugador
						public void run() {
							plugin.logger.info("El jugador " + p.getName() + "ha comido una manzana de oro");

							p.removePotionEffect(PotionEffectType.REGENERATION);
							p.removePotionEffect(PotionEffectType.ABSORPTION);

							Utils.heal(p);

							// Dar absortion si es su primera gapple consumida en es avida
							if (p.getInventory().contains(Material.GOLDEN_APPLE, 7)) {
								plugin.logger.info("Se dara absortion a " + p.getName());
								PotionEffect absortion = new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0);
								p.addPotionEffect(absortion);
							}
						}
					}.runTaskLater(plugin, 2);
				}
			}
		}
	}
}