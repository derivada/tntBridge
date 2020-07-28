package me.tntpablo.thebridge;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Utils {
	public static String error(String msg){
		return ChatColor.translateAlternateColorCodes('&', "&l&a[ &fThe &2&lBridge&l&a ]&r&f &4&l ERROR: &r&f" + msg);
	}
	public static String chat(String msg) {
		// Mensajes generales
		return ChatColor.translateAlternateColorCodes('&', "&l&a[ &fThe &2&lBridge&l&a ]&r&f " + msg);
	}
	public static String color(String msg){
		return ChatColor.translateAlternateColorCodes('&', "&f"+msg);
	}
	public static String pluginMsg(String label) {

		// Mensajes por excepciones, errores del usuario...
		switch (label.toLowerCase()) {
		case "usage":
			return chat("Uso correcto: ...");
		case "lowplayer":
			return chat("No hay suficientes jugadores!");
		case "noimplement":
			return chat("Opcion aun no implementada!");
		case "full":
			return chat("La partida esta llena!");
		case "notenoughplayers":
			return chat("No hay suficientes jugadores para empezar la partida");
		case "stopping":
			return chat("El juego esta terminando!");
		}
		return null;
	}

	public static String timeReminder(int n) {

		if ((n >= 60) && n < 15 && (n % 60 == 0))
			// 60, 120, 180...
			return "&a";
		else if (n >= 15 && n < 60 && (n % 15 == 0))
			// 45 30 15
			return "&e";
		else if (n >= 5 && n<15 && (n % 5 == 0))
			// 10 5
			return "&6";
		else if (n >= 1 && n < 5)
			// 4 3 2 1
			return "&c";
		else
			return null;

	}
	public static ItemStack[] changeColor(ItemStack[] a, Color color) {

		for (ItemStack item : a) {
			try {
				if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_LEGGINGS
						|| item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET) {
					LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
					meta.setColor(color);
					item.setItemMeta(meta);
				}
			} catch (Exception e) {
				// no hacer nada si no hay armor
			}
		}
		return a;
	}
	
	public static void heal(Player p) {
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setSaturation(20);
	}

}
