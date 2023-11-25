package me.alex_s168.worldmod.tasks;

import me.alex_s168.worldmod.pattern.BlockPattern;
import org.bukkit.Location;
import org.bukkit.World;
import org.magicwerk.brownies.collections.GapList;

import java.util.*;

public class FloodFillTask implements Runnable {

    private final World world;
    private final int x, y, z;
    private final Callback callback;
    private final BlockPattern pattern;
    private final Collection<Location> blocks = new HashSet<>();
    private final boolean diagonals;

    public interface Callback {
        void done(Collection<Location> blocks);
    }

    public FloodFillTask(
            World world,
            int x, int y, int z,
            Callback callback,
            BlockPattern pattern,
            boolean diagonals
    ) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.callback = callback;
        this.pattern = pattern;
        this.diagonals = diagonals;
    }

    private boolean matches(Location loc) {
        return pattern.isMatch(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public void run() {
        Queue<Location> queue = new GapList<>();
        queue.add(new Location(world, x, y, z));
        while (!queue.isEmpty()) {
            Location loc = queue.poll();
            if (blocks.contains(loc)) {
                continue;
            }
            if (!matches(loc)) {
                continue;
            }
            blocks.add(loc);

            final int x = loc.getBlockX();
            final int y = loc.getBlockY();
            final int z = loc.getBlockZ();

            queue.add(new Location(world, x+1, y, z));
            queue.add(new Location(world, x-1,y, z));
            queue.add(new Location(world, x, y+1, z));
            queue.add(new Location(world, x, y-1, z));
            queue.add(new Location(world, x, y, z+1));
            queue.add(new Location(world, x, y, z-1));

            if (diagonals) {
                queue.add(new Location(world, x+1, y+1, 0));
                queue.add(new Location(world, x-1, y+1, 0));
                queue.add(new Location(world, x+1, y-1, 0));
                queue.add(new Location(world, x-1, y-1, 0));
                queue.add(new Location(world, x+1, 0, z+1));
                queue.add(new Location(world, x-1, 0, z+1));
                queue.add(new Location(world, x+1, 0, z-1));
                queue.add(new Location(world, x-1, 0, z-1));
                queue.add(new Location(world, 0, y+1, z+1));
                queue.add(new Location(world, 0, y-1, z+1));
                queue.add(new Location(world, 0, y+1, z-1));
                queue.add(new Location(world, 0, y-1, z-1));
            }
        }
        callback.done(blocks);
    }
}
