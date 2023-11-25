package me.alex_s168.worldmod.selection;

import org.bukkit.Location;

import java.util.Set;

public interface SelectionPart {

    void collect(Set<Location> list);

    void moveRel(int x, int y, int z);

    SelectionPart copy();

}
