package me.tntpablo.thebridge;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class BoundingBox {

    private Location corner1, corner2;
    private String name;
    double maxX, maxY, maxZ, minX, minY, minZ;
    private HashMap<Location, Material> blockList = new HashMap<Location,Material>();

    public HashMap<Location,Material> getBlockList() {
    	return this.blockList;
    }
    
    public BoundingBox(String name) {
        corner1 = new Location(null, 0.0, 0.0, 0.0);
        corner2 = new Location(null, 0.0, 0.0, 0.0);
        getMinMax();
        this.name = name;
    }

    public BoundingBox(Location corner1, Location corner2, String name) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.name = name;
        getMinMax();
    }

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, String name) {
        corner1 = new Location(null, 0.0, 0.0, 0.0);
        corner2 = new Location(null, 0.0, 0.0, 0.0);

        corner1.setX(x1);
        corner1.setY(y1);
        corner1.setZ(z1);
        corner2.setX(x2);
        corner2.setY(y2);
        corner2.setZ(z2);
        this.name = name;
        getMinMax();
    }

    public BoundingBox(FileConfiguration config, String pathName, String bboxName) throws NullPointerException {
        // Traer desde la configuracion
        corner1 = new Location(null, 0.0, 0.0, 0.0);
        corner2 = new Location(null, 0.0, 0.0, 0.0);
        corner1.setX(config.getDouble(pathName + ".corner1.x"));
        corner1.setY(config.getDouble(pathName + ".corner1.y"));
        corner1.setZ(config.getDouble(pathName + ".corner1.z"));
        corner2.setX(config.getDouble(pathName + ".corner2.x"));
        corner2.setY(config.getDouble(pathName + ".corner2.y"));
        corner2.setZ(config.getDouble(pathName + ".corner2.z"));
        getMinMax();
        this.name = bboxName;
    }

    private void getMinMax() {
        maxX = Math.max(corner1.getX(), corner2.getX());
        maxY = Math.max(corner1.getY(), corner2.getY());
        maxZ = Math.max(corner1.getZ(), corner2.getZ());
        minX = Math.min(corner1.getX(), corner2.getX());
        minY = Math.min(corner1.getY(), corner2.getY());
        minZ = Math.min(corner1.getZ(), corner2.getZ());
    }

    public boolean isInside(Location loc) {

        if (loc.getX() >= minX && loc.getX() <= maxX && loc.getY() >= minY && loc.getY() <= maxY && loc.getZ() >= minZ
                && loc.getZ() <= maxZ)
            return true;
        return false;
    }

    public boolean isInside(double x, double y, double z) {
        if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ)
            return true;
        return false;
    }

    public void setCorner(int corner, Location loc) {
        if (corner == 1)
            corner1 = loc;
        if (corner == 2)
            corner2 = loc;
    }

    public void setCorner(int corner, double x, double y, double z) {
        if (corner == 1) {
            corner1.setX(x);
            corner1.setY(y);
            corner1.setZ(z);
        }
        if (corner == 2) {
            corner2.setX(x);
            corner2.setY(y);
            corner2.setZ(z);
        }
    }

    public Location getCorner(int corner) {
        if (corner == 1)
            return corner1;
        if (corner == 2)
            return corner2;
        return null;
    }

    public int getVolume() {
        if (corner1 != null && corner2 != null)
            return (int) (Math.floor(maxX - minX) * Math.floor(maxY - minY) * Math.floor(maxZ - minZ));
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void saveBlocks(World world) {
        // Guarda los bloques de la bbox
        for (int i = (int) minX; i <= (int) maxX; i++) {
            for (int j = (int) minY; j <= (int) maxY; j++) {
                for (int k = (int) minZ; k <= (int) maxZ; k++) {
                    Location loc = new Location(world, i, j, k);
                    this.blockList.put(loc, loc.getBlock().getType());
                }
            }
        }
    }

    public void clearBlocks() {
        // Elimina los bloques de la bounding box ( hay que guardarlos primero lol )
        // TODO: Puede que no sea tan necesario pero se podrian quitar antes de guardarlos

        for (Map.Entry<Location, Material> e : this.blockList.entrySet()) {
            e.getKey().getBlock().setType(Material.AIR);
        }
    }

    public void putBlocks() {
        // Pone los bloques de la lista en el mundo
        for (Map.Entry<Location, Material> e : this.blockList.entrySet()) {
            e.getKey().getBlock().setType(e.getValue());
        }
    }

    public String toString() {
        return "Bounding box de &b" + this.getName() + "&f  Esquina &b1&f: &l" + this.corner1.getBlockX() + "&l "
                + this.corner1.getBlockY() + "&l " + this.corner1.getBlockZ() + "&r&fEsquina &b2&f: &l"
                + this.corner2.getBlockX() + "&l " + this.corner2.getBlockY() + "&l " + this.corner2.getBlockZ() + "&r";
    }

}