package me.alex_s168.worldmod.tasks;

import me.alex_s168.worldmod.pattern.BlockPattern;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class FloodFillTask implements Runnable {

    private final World world;
    private final int x, y, z;
    private final Callback callback;
    private final BlockPattern pattern;
    private final Collection<Location> blocks = new ArrayList<>();
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
        Queue<Location> queue = new LinkedBlockingQueue<>();
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

            queue.add(loc.clone().add(1, 0, 0));
            queue.add(loc.clone().add(-1, 0, 0));
            queue.add(loc.clone().add(0, 1, 0));
            queue.add(loc.clone().add(0, -1, 0));
            queue.add(loc.clone().add(0, 0, 1));
            queue.add(loc.clone().add(0, 0, -1));

            if (diagonals) {
                queue.add(loc.clone().add(1, 1, 0));
                queue.add(loc.clone().add(-1, 1, 0));
                queue.add(loc.clone().add(1, -1, 0));
                queue.add(loc.clone().add(-1, -1, 0));
                queue.add(loc.clone().add(1, 0, 1));
                queue.add(loc.clone().add(-1, 0, 1));
                queue.add(loc.clone().add(1, 0, -1));
                queue.add(loc.clone().add(-1, 0, -1));
                queue.add(loc.clone().add(0, 1, 1));
                queue.add(loc.clone().add(0, -1, 1));
                queue.add(loc.clone().add(0, 1, -1));
                queue.add(loc.clone().add(0, -1, -1));
            }
        }
        callback.done(blocks);
    }
}
