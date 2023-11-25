package me.alex_s168.worldmod.pattern;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Arrays;

// TODO: parse blockstates
public class BlockPattern {

    private final Material[] pattern;

    public BlockPattern(Material[] pattern) {
        this.pattern = pattern;
    }

    private Block getBlock(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    public boolean isMatch(World world, int x, int y, int z) {
        return Arrays.stream(pattern).anyMatch(mat -> {
            Material block = getBlock(world, x, y, z).getType();
            return block == mat;
        });
    }

    public void apply(World world, int x, int y, int z) {
        Block block = getBlock(world, x, y, z);
        block.setType(pattern[ (int) (Math.random() * pattern.length) ]);
    }

    public static BlockPattern parse(String pattern) {
        Material[] mats = Arrays
                .stream(pattern.split(","))
                .map(Material::matchMaterial)
                .toArray(Material[]::new);
        return new BlockPattern(mats);
    }

}
