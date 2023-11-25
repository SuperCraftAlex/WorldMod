package me.alex_s168.worldmod.tasks;

import me.alex_s168.worldmod.pattern.BlockPattern;
import me.alex_s168.worldmod.selection.Selection;
import me.alex_s168.worldmod.selection.SelectionPart;
import me.alex_s168.worldmod.utils.LoadBalancer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class FilteringTask implements Runnable {

    private final Selection selection;
    private final World world;
    private final BlockPattern pattern;
    private final Consumer<Set<Location>> callback;

    public FilteringTask(Selection selection, World world, BlockPattern pattern, Consumer<Set<Location>> callback) {
        this.selection = selection;
        this.world = world;
        this.pattern = pattern;
        this.callback = callback;
    }

    @Override
    public void run() {
        final int count = selection.size();
        Set<Location> list = new LinkedHashSet<>(count);
        for (SelectionPart s : selection.parts()) {
            s.collect(list);
        }

        Set<Location> filtered = new CopyOnWriteArraySet<>();
        Thread[] threads = LoadBalancer.sched(list, (loc) -> {
            if (pattern.isMatch(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                filtered.add(loc);
            }
        });
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        callback.accept(filtered);
    }
}
