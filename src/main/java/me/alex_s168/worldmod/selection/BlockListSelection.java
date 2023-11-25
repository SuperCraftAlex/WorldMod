package me.alex_s168.worldmod.selection;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BlockListSelection implements SelectionPart {

    private final Collection<Location> blocks;

    public BlockListSelection(Collection<Location> blocks) {
        this.blocks = blocks;
    }


    @Override
    public void collect(Set<Location> list) {
        list.addAll(blocks);
    }

    @Override
    public void moveRel(int x, int y, int z) {
        for (Location loc : blocks) {
            loc.add(x, y, z);
        }
    }

    @Override
    public SelectionPart copy() {
        List<Location> copy = new ArrayList<>(blocks.size());
        for (Location loc : blocks) {
            copy.add(loc.clone());
        }
        return new BlockListSelection(copy);
    }
}
