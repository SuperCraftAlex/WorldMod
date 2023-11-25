package me.alex_s168.worldmod.tasks;

import me.alex_s168.worldmod.pattern.BlockPattern;
import me.alex_s168.worldmod.selection.Selection;
import me.alex_s168.worldmod.utils.WorldSynced;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
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
        selection.parts().forEach(part -> {
            Set<Location> blocks = new LinkedHashSet<>();
            part.collect(blocks);
            WorldSynced.worldSynced(plugin, world, () -> blocks.forEach(loc ->
                    pattern.apply(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
            ));
        });
        callback.run();
    }

}
