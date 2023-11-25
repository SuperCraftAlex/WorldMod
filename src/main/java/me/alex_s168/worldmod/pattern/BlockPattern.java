package me.alex_s168.worldmod.pattern;

import me.alex_s168.worldmod.exc.InvalidPatternException;
import me.alex_s168.worldmod.utils.Pair;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: parse blockstates
public class BlockPattern {

    private record Flag(
            boolean not
    ) {}

    private final Pair<Material, Flag>[] pattern;

    private BlockPattern(Pair<Material, Flag>[] pattern) {
        this.pattern = pattern;
    }

    private Block getBlock(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    public boolean isMatch(World world, int x, int y, int z) {
        return Arrays.stream(pattern).anyMatch(pair -> {
            Material mat = pair.first;
            Flag flag = pair.second;

            Material block = getBlock(world, x, y, z).getType();
            return flag.not ^ block == mat;
        });
    }

    public void apply(World world, int x, int y, int z) {
        Block block = getBlock(world, x, y, z);
        block.setType(Arrays
                .stream(pattern)
                .filter((it) -> !it.second.not())
                .toList()
                .get((int) (Math.random() * pattern.length))
                .first
        );
    }

    @SuppressWarnings("unchecked")
    public static BlockPattern parse(String pattern) throws InvalidPatternException {
        List<Pair<Material, Flag>> list = new ArrayList<>();
        for (String part : pattern.split(",")) {
            if (part.startsWith("!")) {
                Material mat = Material.matchMaterial(part.substring(1));
                if (mat == null) {
                    throw new InvalidPatternException("Invalid material: " + part);
                }
                list.add(new Pair<>(mat, new Flag(true)));
            } else {
                Material mat = Material.matchMaterial(part);
                if (mat == null) {
                    throw new InvalidPatternException("Invalid material: " + part);
                }
                list.add(new Pair<>(mat, new Flag(false)));
            }
        }
        return new BlockPattern(list.toArray(new Pair[0]));
    }

}
