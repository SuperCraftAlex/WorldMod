package me.alex_s168.worldmod.utils;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldSynced {
    public static void worldSynced(Plugin plug, World world, Runnable run) {
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (world) {
                    run.run();
                }
            }
        }.runTask(plug);
    }
}
