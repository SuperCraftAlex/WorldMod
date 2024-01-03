package me.alex_s168.worldmod.utils;

import org.bukkit.Location;

import java.util.List;

public class BlockMath {

    public static List<Location> rotate(List<Location> locs, Location center, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        for (Location loc : locs) {
            double x = loc.getX() - center.getX();
            double z = loc.getZ() - center.getZ();
            loc.setX(x * cos - z * sin + center.getX());
            loc.setZ(x * sin + z * cos + center.getZ());
        }
        return locs;
    }

}
