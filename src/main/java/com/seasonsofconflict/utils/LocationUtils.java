package com.seasonsofconflict.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class LocationUtils {

    public static boolean isIndoors(Location location) {
        // Check if there's a solid block above the player
        Block block = location.getBlock();
        for (int y = block.getY(); y < location.getWorld().getMaxHeight(); y++) {
            Block above = location.getWorld().getBlockAt(block.getX(), y, block.getZ());
            if (above.getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    public static String formatCoordinates(Location location) {
        return String.format("(%d, %d, %d)",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    public static double distance2D(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
