package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements Listener {

    // general
    boolean enable;
    boolean ignoreAnimals;
    // section detect
    int detectRadius;
    int detectHeight;
    int detectLimit;
    int detectHardLimit;
    int detectDepthLimit;
    List<Material> detectBlockTypes = new ArrayList<Material>();
    // section detect structures
    boolean detectStructures;
    int structureRadius;
    int structureHeight;
    int structureLimit;
    // section cobwebs
    boolean allowNearCobwebs;
    int cobwebLimit;
    // section spawners
    boolean allowMobSpawners;
    // section treetops
    boolean preventTreetopSpawn;
    int leafLimit;

    @Override
    public void onEnable() {
        Configuration settings = getConfig();

        settings.options().copyDefaults(true);
        saveConfig();

        loadSettings();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getLogger().info("[NotOnMyLawn] Is now Enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[NotOnMyLawn] disabled.");
    }

    private void loadSettings() {
        Configuration settings = getConfig();
        enable = settings.getBoolean("enable");
        ignoreAnimals = settings.getBoolean("ignore_animals");
        detectStructures = settings.getBoolean("structures.detect");
        structureRadius = settings.getInt("structures.radius");
        structureHeight = settings.getInt("structures.height");
        structureLimit = settings.getInt("structures.limit");
        allowMobSpawners = settings.getBoolean("spawners.allow");
        preventTreetopSpawn = !settings.getBoolean("treetops.allow");
        leafLimit = settings.getInt("treetops.limit");
        allowNearCobwebs = settings.getBoolean("cobwebs.allow");
        cobwebLimit = settings.getInt("cobwebs.limit");
        detectRadius = settings.getInt("detect.radius");
        detectHeight = settings.getInt("detect.height");
        detectLimit = settings.getInt("detect.limit");
        detectHardLimit = settings.getInt("detect.hard_limit");
        detectDepthLimit = settings.getInt("detect.depth_limit");
        detectBlockTypes.clear();

        List<String> blockTypes = settings.getStringList("detect.block_types");
        for (String bType : blockTypes) {
            detectBlockTypes.add(Material.getMaterial(bType));
        }

        getServer().getLogger().info("[NotOnMyLawn] Configuration loaded.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("notonmylawn")
                || cmd.getName().equalsIgnoreCase("noml")) {

            if (args.length < 1) {
                return false;
            }

            String action = args[0];
            if (action.equalsIgnoreCase("enable")) {
                if (!enable) {
                    enable = true;
                    sender.sendMessage("The plugin is now enabled.");
                }
            } else if (action.equalsIgnoreCase("disable")) {
                if (enable) {
                    enable = false;
                    sender.sendMessage("The plugin is now disabled.");
                }
            } else if (action.equalsIgnoreCase("reload")) {
                setEnabled(false);
                loadSettings();
                setEnabled(true);
            } else if (action.equalsIgnoreCase("show")) {
                if (args.length < 2) {
                    return false;
                }

                if (args[1].equalsIgnoreCase("enable")) {
                    if (enable) {
                        sender.sendMessage("The plugin is currently enabled.");
                    } else {
                        sender.sendMessage("The plugin is currently disabled.");
                    }
                } else {
                    sender.sendMessage(
                            "The current value of " + args[1] + " is: "
                            + getConfig().get(args[1]).toString());
                }
            } else if (action.equalsIgnoreCase("set")) {
                if (args.length < 3) {
                    return false;
                }
                switch (setConfig(args[1], args[2])) {
                    case 0:
                        if (args[1].equalsIgnoreCase("enable")) {
                            if (enable) {
                                sender.sendMessage(
                                        "The plugin is now enabled.");
                            } else {
                                sender.sendMessage(
                                        "The plugin is now disabled.");
                            }
                        } else {
                            sender.sendMessage(
                                    "The new value of " + args[1] + " is: "
                                    + getConfig().get(args[1]).toString());
                        }
                    case -1:
                        sender.sendMessage("Unknown setting.");
                        break;
                    case -2:
                        sender.sendMessage("Invalid value.");
                        break;
                    default:
                }
            } else {
                return false;
            }

            return true;
        }
        return false;
    }

    private int setConfig(String setting, String value) {
        Configuration settings = getConfig();

        try {
            if (setting.equalsIgnoreCase("enable")) {
                enable = parseBooleanStrict(value);
                settings.set("enable", enable);
            } else if (setting.equalsIgnoreCase("ignore_animals")) {
                ignoreAnimals = parseBooleanStrict(value);
                settings.set("ignore_animals", ignoreAnimals);
            } else if (setting.equalsIgnoreCase("structures.detect")) {
                detectStructures = parseBooleanStrict(value);
                settings.set("structures.detect", detectStructures);
            } else if (setting.equalsIgnoreCase("structures.radius")) {
                structureRadius = Integer.parseInt(value);
                settings.set("structures.radius", structureRadius);
            } else if (setting.equalsIgnoreCase("structures.height")) {
                structureHeight = Integer.parseInt(value);
                settings.set("structures.height", structureHeight);
            } else if (setting.equalsIgnoreCase("structures.limit")) {
                structureLimit = Integer.parseInt(value);
                settings.set("structures.limit", structureLimit);
            } else if (setting.equalsIgnoreCase("spawners.allow")) {
                allowMobSpawners = parseBooleanStrict(value);
                settings.set("spawners.allow", allowMobSpawners);
            } else if (setting.equalsIgnoreCase("treetops.allow")) {
                preventTreetopSpawn = parseBooleanStrict(value);
                settings.set("treetops.allow", preventTreetopSpawn);
            } else if (setting.equalsIgnoreCase("treetops.limit")) {
                leafLimit = Integer.parseInt(value);
                settings.set("treetops.limit", leafLimit);
            } else if (setting.equalsIgnoreCase("cobwebs.allow")) {
                allowNearCobwebs = parseBooleanStrict(value);
                settings.set("cobwebs.allow", allowNearCobwebs);
            } else if (setting.equalsIgnoreCase("cobwebs.limit")) {
                cobwebLimit = Integer.parseInt(value);
                settings.set("cobwebs.limit", cobwebLimit);
            } else if (setting.equalsIgnoreCase("detect.radius")) {
                detectRadius = Integer.parseInt(value);
                settings.set("detect.radius", detectRadius);
            } else if (setting.equalsIgnoreCase("detect.height")) {
                detectHeight = Integer.parseInt(value);
                settings.set("detect.height", detectHeight);
            } else if (setting.equalsIgnoreCase("detect.limit")) {
                detectLimit = Integer.parseInt(value);
                settings.set("detect.limit", detectLimit);
            } else if (setting.equalsIgnoreCase("detect.hard_limit")) {
                detectHardLimit = Integer.parseInt(value);
                settings.set("detect.hard_limit", detectHardLimit);
            } else if (setting.equalsIgnoreCase("detect.depth_limit")) {
                detectDepthLimit = Integer.parseInt(value);
                settings.set("detect.depth_limit", detectDepthLimit);
            } else {
                return -1;
            }
        } catch (NumberFormatException e) {
            return -2;
        }

        saveConfig();
        return 0;
    }

    public boolean parseBooleanStrict(String text)
            throws NumberFormatException {
        if (text.equalsIgnoreCase("true") || text.equals("1")) {
            return true;
        }
        if (text.equalsIgnoreCase("false") || text.equals("0")) {
            return false;
        }
        throw new NumberFormatException();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enable) {
            return;
        }

        if (ignoreAnimals && ((event.getEntity() instanceof Animals))) {
            return;
        }

        Location loc = event.getLocation();

        int wy = loc.getBlockY();
        if (wy < detectDepthLimit) {
            return;
        }

        int wx = loc.getBlockX();
        int wz = loc.getBlockZ();

        World world = event.getEntity().getWorld();

        if (preventTreetopSpawn || allowNearCobwebs || allowMobSpawners) {
            int leafBlocks = 0;
            int webBlocks = 0;

            for (int y = wy - 2; y < wy + 1; y++) {
                for (int x = wx - 5; x < wx + 5; x++) {
                    for (int z = wz - 5; z < wz + 5; z++) {
                        Material mat = Material.getMaterial(
                                world.getBlockTypeIdAt(x, y, z));

                        if (preventTreetopSpawn
                                && mat == Material.LEAVES
                                && (++leafBlocks >= leafLimit)) {
                            event.setCancelled(true);
                            return;
                        }

                        if (allowNearCobwebs
                                && mat == Material.WEB
                                && (++webBlocks >= cobwebLimit)) {
                            return;
                        }

                        if (allowMobSpawners
                                && mat == Material.MOB_SPAWNER) {
                            return;
                        }
                    }
                }
            }
        }

        int totalBlocks = 0;
        int detectBlocks = 0;
        for (int y = Math.max(detectDepthLimit, wy - detectHeight);
                y < wy + detectHeight;
                y++) {
            for (int x = wx - detectRadius;
                    x < wx + detectRadius;
                    x++) {
                for (int z = wz - detectRadius;
                        z < wz + detectRadius;
                        z++) {

                    Material mat = Material.getMaterial(
                            world.getBlockTypeIdAt(x, y, z));

                    if (!detectBlockTypes.contains(mat)) {
                        continue;
                    }

                    if (++totalBlocks >= detectHardLimit) {
                        event.setCancelled(true);
                        return;
                    }

                    if (detectStructures) {
                        int structureBlocks = 0;

                        structureDetectionLoop:
                        for (int sy = wy - structureHeight;
                                sy < wy + structureHeight;
                                sy++) {
                            for (int sx = wx - structureRadius;
                                    sx < wx + structureRadius;
                                    sx++) {
                                for (int sz = wz - structureRadius;
                                        sz < wz + structureRadius;
                                        sz++) {

                                    Material smat = Material.getMaterial(
                                            world.getBlockTypeIdAt(sx, sy, sz));

                                    if (!detectBlockTypes.contains(smat)) {
                                        continue;
                                    }

                                    if (++structureBlocks < structureLimit) {
                                        continue;
                                    }

                                    if (++detectBlocks >= detectLimit) {
                                        event.setCancelled(true);
                                        return;
                                    }

                                    break structureDetectionLoop;
                                }
                            }
                        }
                    } else if (++detectBlocks >= detectLimit) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}