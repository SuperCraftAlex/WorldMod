package me.alex_s168.worldmod;

import me.alex_s168.worldmod.pattern.BlockPattern;
import me.alex_s168.worldmod.selection.*;
import me.alex_s168.worldmod.tasks.FloodFillTask;
import me.alex_s168.worldmod.tasks.SetTask;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public final class WorldMod extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        this.getCommand("wm").setExecutor(this);
    }

    @Override
    public void onDisable() {
    }

    private Selection copy(Selection sel) {
        List<SelectionPart> parts = new ArrayList<>();
        for (SelectionPart part : sel.parts()) {
            parts.add(part.copy());
        }
        return new Selection(sel.size(), parts);
    }

    private final Map<String, Stack<Selection>> selections = new HashMap<>();

    private final Map<String, Collection<Thread>> threads = new HashMap<>();

    private int parseLoc(int playerPos, String pos) {
        if (pos == null) {
            return 0;
        }
        if (pos.length() == 0) {
            return 0;
        }
        if (pos.charAt(0) == '~') {
            if (pos.length() == 1) {
                return playerPos;
            }
            return playerPos + Integer.parseInt(pos.substring(1));
        }
        return Integer.parseInt(pos);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] argsIn) {
        if (!(sender instanceof Player p)) {
            return false;
        }

        ArrayList<String> argList = new ArrayList<>();
        ArrayList<String> specialArgs = new ArrayList<>();
        for (String arg : argsIn) {
            if (arg.length() == 0) {
                continue;
            }
            if (arg.charAt(0) == ':') {
                specialArgs.add(arg);
                continue;
            }
            argList.add(arg);
        }
        String[] args = argList.toArray(new String[0]);

        if (args.length == 0) {
            return false;
        }

        if (!threads.containsKey(p.getName())) {
            threads.put(p.getName(), new ArrayList<>());
            selections.put(p.getName(), new Stack<>());
        }
        Collection<Thread> th = threads.get(p.getName());
        Stack<Selection> sels = selections.get(p.getName());

        if (args[0].equals("cancel")) {
            for (Thread t : th) {
                t.interrupt();
            }
            th.clear();
            p.sendMessage("Cancelled all tasks");
            return true;
        }

        if (args[0].equals("sel") || args[0].equals("select") || args[0].equals("selection")) {


            if (args.length == 1 || args[1].equals("list")) {
                p.sendMessage("BOTTOM");
                sels.forEach(sel -> p.sendMessage("Selection: " + sel.size() + " blocks"));
                p.sendMessage("TOP");
                return true;
            }

            if (args[1].equals("remove")) {
                if (sels.isEmpty()) {
                    p.sendMessage("No selections to remove");
                    return true;
                }
                sels.pop();
                p.sendMessage("Removed selection");
                return true;
            }

            if (args[1].equals("clear")) {
                if (args.length == 3 && args[2].equals("all")) {
                    sels.clear();
                    p.sendMessage("Cleared all selections");
                    return true;
                }
                p.sendMessage("Usage: /wm sel clear all");
                return false;
            }

            if (args[1].equals("clone")) {
                if (sels.isEmpty()) {
                    p.sendMessage("No selections to clone");
                    return true;
                }
                sels.push(copy(sels.peek()));
                p.sendMessage("Cloned selection");
                return true;
            }

            if (args[1].equals("swap")) {
                if (sels.size() < 2) {
                    p.sendMessage("Not enough selections to swap");
                    return true;
                }
                Selection a = sels.pop();
                Selection b = sels.pop();
                sels.push(a);
                sels.push(b);
                p.sendMessage("Swapped selections");
                return true;
            }

            if (args[1].equals("add")) {
                if (args.length == 2) {
                    p.sendMessage("Usage: /wm sel add <type> [args...]");
                    p.sendMessage("Types: box, single, flood");
                    return false;
                }

                String type = args[2];

                if (type.equals("box")) {
                    if (args.length != 9) {
                        p.sendMessage("Usage: /wm sel add box <x1> <y1> <z1> <x2> <y2> <z2>");
                        return false;
                    }
                    int x1 = parseLoc(p.getLocation().getBlockX(), args[3]);
                    int y1 = parseLoc(p.getLocation().getBlockY(), args[4]);
                    int z1 = parseLoc(p.getLocation().getBlockZ(), args[5]);
                    int x2 = parseLoc(p.getLocation().getBlockX(), args[6]);
                    int y2 = parseLoc(p.getLocation().getBlockY(), args[7]);
                    int z2 = parseLoc(p.getLocation().getBlockZ(), args[8]);
                    int size = (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
                    Location min = new Location(p.getWorld(), Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
                    Location max = new Location(p.getWorld(), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
                    List<SelectionPart> parts = new ArrayList<>();
                    parts.add(new AABBSelection(min, max));
                    sels.push(new Selection(size, parts));
                    p.sendMessage("Added box selection");
                    return true;
                }

                if (type.equals("single")) {
                    if (args.length != 6) {
                        p.sendMessage("Usage: /wm sel add single <x> <y> <z>");
                        return false;
                    }
                    int x = parseLoc(p.getLocation().getBlockX(), args[3]);
                    int y = parseLoc(p.getLocation().getBlockY(), args[4]);
                    int z = parseLoc(p.getLocation().getBlockZ(), args[5]);
                    sels.push(new Selection(1, List.of(
                            new BlockSelection(new Location(p.getWorld(), x, y, z))
                    )));
                    p.sendMessage("Added block selection");
                    return true;
                }

                if (type.equals("flood")) {
                    if (args.length != 7) {
                        p.sendMessage("Usage: /wm sel add flood <x> <y> <z> <blocks>");
                        return false;
                    }
                    int x = parseLoc(p.getLocation().getBlockX(), args[3]);
                    int y = parseLoc(p.getLocation().getBlockY(), args[4]);
                    int z = parseLoc(p.getLocation().getBlockZ(), args[5]);
                    String blocks = args[6];
                    BlockPattern pattern = BlockPattern.parse(blocks);

                    Runnable task = new FloodFillTask(
                            p.getWorld(),
                            x, y, z,
                            (Collection<Location> blocksOut) -> {
                                sels.push(new Selection(blocksOut.size(), List.of(
                                        new BlockListSelection(blocksOut)
                                )));
                                p.sendMessage("Flood-fill selection complete: " + blocksOut.size() + " blocks");
                                th.remove(Thread.currentThread());
                            },
                            pattern,
                            specialArgs.contains(":diagonals")
                    );
                    Thread t = new Thread(task);
                    th.add(t);
                    t.start();

                    p.sendMessage("Started flood-fill selection");
                    p.sendMessage("This may take a while, please wait...");
                    p.sendMessage("You can use `/wm cancel` if this was a mistake");
                    return true;
                }

                p.sendMessage("Unknown selection type: " + type);
                p.sendMessage("Available types: box, single, flood");
                return false;
            }

            if (args[1].equals("combine")) {
                if (sels.size() < 2) {
                    p.sendMessage("Not enough selections to combine (needs 2)");
                    return true;
                }
                Selection a = sels.pop();
                Selection b = sels.pop();
                a.parts().addAll(b.parts());
                sels.push(new Selection(a.size() + b.size(), a.parts()));
                p.sendMessage("Combined selections");
                return true;
            }

            if (args[1].equals("move")) {
                if (sels.isEmpty()) {
                    p.sendMessage("No selections to move");
                    return true;
                }
                if (args.length == 2) {
                    p.sendMessage("Usage: /wm sel move <type>");
                    p.sendMessage("Types: relative, facing");
                    return false;
                }

                String type = args[2];

                if (type.equals("relative")) {
                    if (args.length != 6) {
                        p.sendMessage("Usage: /wm sel move pos <x> <y> <z>");
                        return false;
                    }
                    int x = Integer.parseInt(args[3]);
                    int y = Integer.parseInt(args[4]);
                    int z = Integer.parseInt(args[5]);
                    for (SelectionPart part : sels.peek().parts()) {
                        part.moveRel(x, y, z);
                    }
                    p.sendMessage("Moved selection (relative)");
                    return true;
                }

                if (type.equals("facing")) {
                    if (args.length != 4) {
                        p.sendMessage("Usage: /wm sel move facing <blocks>");
                        return false;
                    }

                    float blocks = Float.parseFloat(args[3]);

                    if (specialArgs.contains(":exact")) {
                        Vector v = p.getLocation().getDirection().multiply(blocks);
                        for (SelectionPart part : sels.peek().parts()) {
                            part.moveRel(
                                    (int) Math.round(v.getX()),
                                    (int) Math.round(v.getY()),
                                    (int) Math.round(v.getZ())
                            );
                        }
                        p.sendMessage("Moved selection (exact facing)");
                    }
                    else {
                        BlockFace f = p.getFacing();
                        for (SelectionPart part : sels.peek().parts()) {
                            part.moveRel(
                                    f.getModX() * (int) blocks,
                                    f.getModY() * (int) blocks,
                                    f.getModZ() * (int) blocks
                            );
                        }
                        p.sendMessage("Moved selection (facing)");
                    }
                    return true;
                }

                p.sendMessage("Unknown selection move type: " + type);
                p.sendMessage("Available types: relative, facing");
            }

            p.sendMessage("Unknown selection command: " + args[1]);
            p.sendMessage("Available commands: remove, clear, clone, swap, add, list, move");
            return false;
        }

        if (args[0].equals("set")) {
            if (args.length == 1) {
                p.sendMessage("Usage: /wm set <block>");
                return false;
            }
            if (sels.isEmpty()) {
                p.sendMessage("No selections to set");
                return true;
            }
            String block = args[1];
            BlockPattern pattern = BlockPattern.parse(block);
            Runnable task = new SetTask(
                    sels.pop(),
                    p.getWorld(),
                    pattern,
                    () -> {
                        p.sendMessage("Set task complete");
                        th.remove(Thread.currentThread());
                    },
                    this
            );
            Thread t = new Thread(task);
            th.add(t);
            t.start();
            p.sendMessage("Started set task");
            p.sendMessage("This may take a while, please wait...");
            p.sendMessage("You can use `/wm cancel` if this was a mistake");
            return true;
        }

        return false;
    }
}
