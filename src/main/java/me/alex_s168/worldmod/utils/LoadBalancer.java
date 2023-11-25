package me.alex_s168.worldmod.utils;

import org.bukkit.Location;

import java.util.Collection;
import java.util.function.Consumer;

public class LoadBalancer {
    public static Thread[] sched(Collection<Location> blocks, Consumer<Location> each) {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int size = blocks.size();
        final int perThread = size / cores;
        final int remainder = size % cores;
        final int[] i = {0};
        final Thread[] threads = new Thread[cores];
        for (int j = 0; j < cores; j++) {
            final int start = i[0];
            final int end = start + perThread + (j < remainder ? 1 : 0);
            threads[j] = new Thread(() -> {
                for (int k = start; k < end; k++) {
                    each.accept((Location) blocks.toArray()[k]);
                }
            });
            threads[j].start();
            i[0] = end;
        }
        return threads;
    }
}
