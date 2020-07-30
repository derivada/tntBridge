package me.tntpablo.thebridge;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum Team {
    BLACK(0x0, 0x15, ChatColor.BLACK, "BLACK", "NEGRO", Color.BLACK),
    DARK_BLUE(0x1, 0x11, ChatColor.DARK_BLUE, "DARK BLUE", "AZUL OSCURO", Color.NAVY),
    DARK_GREEN(0x2, 0x13, ChatColor.DARK_GREEN, "DARK GREEN", "VERDE OSCURO", Color.OLIVE),
    DARK_AQUA(0x3, 0x11, ChatColor.DARK_AQUA, "DARK AQUA", "MARINO OSCURO", Color.TEAL),
    DARK_RED(0x4, 0x14, ChatColor.DARK_RED, "DARK RED", "ROJO OSCURO", Color.MAROON),
    DARK_PURPLE(0x5, 0x10, ChatColor.DARK_PURPLE, "DARK PURPLE", "MORADO OSCURO", Color.PURPLE),
    GOLD(0x6, 0x1, ChatColor.GOLD, "GOLD", "ORO", Color.ORANGE),
    GRAY(0x7, 0x9, ChatColor.GRAY, "GRAY", "GRIS", Color.GRAY),
    DARK_GRAY(0x8, 0x9, ChatColor.DARK_GRAY, "DARK GRAY", "GRIS OSCURO", Color.SILVER),
    BLUE(0x9, 0x11, ChatColor.BLUE, "BLUE", "AZUL", Color.BLUE),
    GREEN(0xa, 0x13, ChatColor.GREEN, "GREEN", "VERDE", Color.GREEN),
    AQUA(0xb, 0x3, ChatColor.AQUA, "AQUA", "MARINO CLARO", Color.AQUA),
    RED(0xc, 0x14, ChatColor.RED, "RED", "ROJO", Color.RED),
    LIGHT_PURPLE(0xd, 0x6, ChatColor.LIGHT_PURPLE, "LIGHT PURPLE", "ROSA", Color.FUCHSIA),
    YELLOW(0xe, 0x4, ChatColor.YELLOW, "YELLOW", "AMARILLO", Color.YELLOW),
    WHITE(0xf, 0x0, ChatColor.WHITE, "WHITE", "BLANCO", Color.WHITE);

    private int ID;
    private int blockID;
    private ChatColor CHATCOLOR;
    private String NAME;
    private String NOMBRE;
    private Color COLOR;

    Team(final int ID, int blockID, ChatColor chatColor, String name, String nombre, Color color) {
        this.ID = ID;
        this.blockID = blockID;
        this.CHATCOLOR = chatColor;
        this.NAME = name;
        this.NOMBRE = nombre;
        this.COLOR = color;
    }

    public int getID() {
        return ID;
    }

    public int getBlockID() {
        return blockID;
    }

    public ChatColor getChatColor() {
        return CHATCOLOR;
    }

    public Color getColor() {
        return COLOR;
    }

    public String getNameMsg() {
        return "&" + Integer.toHexString(ID) + NAME.substring(0, 1) + NAME.substring(1, NAME.length()).toLowerCase();
    }

    public String getNombreMsg() {
        return "&" + Integer.toHexString(ID) + NOMBRE.substring(0, 1)
                + NOMBRE.substring(1, NOMBRE.length()).toLowerCase();
    }

    public String getName() {
        return NAME;
    }

    public String getNombre() {
        return NOMBRE;
    }

    public String getEnumName() {
        return NAME.toUpperCase().replace(" ", "_");
    }

}