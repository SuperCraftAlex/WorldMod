package me.alex_s168.worldmod.tasks;

import me.alex_s168.worldmod.pattern.BlockPattern;
import me.alex_s168.worldmod.selection.Selection;
import me.alex_s168.worldmod.utils.LoadBalancer;
import me.alex_s168.worldmod.utils.WorldSynced;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class SetTask implements Runnable {

    private final Selection selection;
    private final World world;
    private final BlockPattern pattern;
    private final Runnable callback;
    private final Plugin plugin;

    public SetTask(Selection selection, World world, BlockPattern pattern, Runnable callback, Plugin plugin) {
        this.selection = selection;
        this.world = world;
        this.pattern = pattern;
        this.callback = callback;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Set<Location> blocks = new HashSet<>();
        selection.parts().forEach(part -> part.collect(blocks));
        Thread[] ths = LoadBalancer.schedAdv(blocks, (range) -> {
            final Location[] arr = blocks.toArray(new Location[0]);
            final int start = range.first;
            final int end = range.second;
            WorldSynced.worldSynced(plugin, world, () -> {
                for (int k = start; k < end; k++) {
                    Location loc = arr[k];
                    pattern.apply(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                }
            });
        });
        for (Thread th : ths) {
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        callback.run();
    }

}
