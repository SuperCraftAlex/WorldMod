package me.alex_s168.worldmod.selection;

import org.bukkit.Location;

import java.util.Set;

public class AABBSelection implements SelectionPart {
    private final Location min;
    private final Location max;

    public AABBSelection(Location min, Location max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void collect(Set<Location> list) {
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    list.add(new Location(min.getWorld(), x, y, z));
                }
            }
        }
    }

    @Override
    public void moveRel(int x, int y, int z) {
        min.add(x, y, z);
        max.add(x, y, z);
    }

    @Override
    public SelectionPart copy() {
        return new AABBSelection(min.clone(), max.clone());
    }
}
