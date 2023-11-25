package me.alex_s168.worldmod.selection;

import org.bukkit.Location;

import java.util.Set;

public class BlockSelection implements SelectionPart{

    private final Location loc;

    public BlockSelection(Location loc) {
        this.loc = loc;
    }

    @Override
    public void collect(Set<Location> list) {
        list.add(loc);
    }

    @Override
    public void moveRel(int x, int y, int z) {
        loc.add(x, y, z);
    }

    @Override
    public SelectionPart copy() {
        return new BlockSelection(loc.clone());
    }

}
