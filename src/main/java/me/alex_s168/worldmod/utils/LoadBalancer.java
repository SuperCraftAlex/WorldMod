package me.alex_s168.worldmod.utils;

import org.bukkit.Location;

import java.util.Collection;
import java.util.function.Consumer;

public class LoadBalancer {
    public static Thread[] sched(Collection<Location> blocks, Consumer<Location> each) {
        return schedAdv(blocks, range -> {
            final Location[] arr = blocks.toArray(new Location[0]);
            final int start = range.first;
            final int end = range.second;
            for (int k = start; k < end; k++) {
                each.accept(arr[k]);
            }
        });
    }

    public static Thread[] schedAdv(Collection<Location> blocks, Consumer<Pair<Integer, Integer>> eachThread) {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int size = blocks.size();
        final int perThread = size / cores;
        final int remainder = size % cores;
        final int[] i = { 0 };
        final Thread[] threads = new Thread[cores];
        for (int j = 0; j < cores; j++) {
            final int start = i[0];
            final int end = start + perThread + (j < remainder ? 1 : 0);
            threads[j] = new Thread(() -> {
                eachThread.accept(new Pair<>(start, end));
            });
            threads[j].start();
            i[0] = end;
        }
        return threads;
    }
}
