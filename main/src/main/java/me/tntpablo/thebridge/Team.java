package me.tntpablo.thebridge;

import org.bukkit.ChatColor;

public enum Team {
    BLACK(0x0, ChatColor.BLACK, "BLACK", "NEGRO"), DARK_BLUE(0x1, ChatColor.DARK_BLUE, "DARK BLUE", "AZUL OSCURO"),
    DARK_GREEN(0x2, ChatColor.DARK_GREEN, "DARK GREEN", "VERDE OSCURO"),
    DARK_AQUA(0x3, ChatColor.DARK_AQUA, "DARK AQUA", "AZUL OSCURO"),
    DARK_RED(0x4, ChatColor.DARK_RED, "DARK RED", "ROJO OSCURO"),
    DARK_PURPLE(0x5, ChatColor.DARK_PURPLE, "DARK PURPLE", "MORADO OSCURO"), GOLD(0x6, ChatColor.GOLD, "GOLD", "ORO"),
    GRAY(0x7, ChatColor.GRAY, "GRAY", "GRIS"), DARK_GRAY(0x8, ChatColor.DARK_GRAY, "DARK GRAY", "GRIS OSCURO"),
    BLUE(0x9, ChatColor.BLUE, "BLUE", "AZUL"), GREEN(0xa, ChatColor.GREEN, "GREEN", "VERDE"),
    AQUA(0xb, ChatColor.AQUA, "AQUA", "AZUL CLARO"), RED(0xc, ChatColor.RED, "RED", "ROJO"),
    LIGHT_PURPLE(0xd, ChatColor.LIGHT_PURPLE, "LIGHT PURPLE", "ROSA"),
    YELLOW(0xe, ChatColor.YELLOW, "YELLOW", "AMARILLO"), WHITE(0xf, ChatColor.WHITE, "WHITE", "BLANCO");

    private int ID;
    private ChatColor COLOR;
    private String NAME;
    private String NOMBRE;

    Team(final int ID, ChatColor color, String name, String nombre) {
        this.ID = ID;
        this.COLOR = color;
        this.NAME = name;
        this.NOMBRE = nombre;
    }

    public int getID() {
        return ID;
    }

    public ChatColor getColor() {
        return COLOR;
    }

    public String getName() {
        return NAME;
    }

    public String getNombre() {
        return NOMBRE;
    }
}