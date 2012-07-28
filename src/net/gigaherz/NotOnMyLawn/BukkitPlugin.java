package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
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

        enable = settings.getBoolean("enable");
        ignoreAnimals = settings.getBoolean("ignore_animals");
        detectStructures = settings.getBoolean("structures.detect");
        structureRadius = settings.getInt("structures.radius");
        structureHeight = settings.getInt("structures.height");
        structureLimit = settings.getInt("structures.limit");
        allowMobSpawners = settings.getBoolean("spawners.allow");
        preventTreetopSpawn = !settings.getBoolean("treetops.allow");
        leafLimit = settings.getInt("treetops.minimum_leaves");
        allowNearCobwebs = settings.getBoolean("cobwebs.allow");
        cobwebLimit = settings.getInt("cobwebs.limit");
        detectRadius = settings.getInt("detect.radius");
        detectHeight = settings.getInt("detect.height");
        detectLimit = settings.getInt("detect.limit");
        detectHardLimit = settings.getInt("detect.hard_limit");
        detectDepthLimit = settings.getInt("detect.depth_limit");

        List<String> blockTypes = settings.getStringList("detect.block_types");
        for (String bType : blockTypes) {
            detectBlockTypes.add(Material.getMaterial(bType));
        }

        getServer().getLogger().info("[NotOnMyLawn] Configuration loaded.");

        if (enable) {
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getLogger().info("[NotOnMyLawn] Is now Enabled.");
        }
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[NotOnMyLawn] disabled.");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
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

                        if (preventTreetopSpawn &&
                                mat == Material.LEAVES &&
                                (++leafBlocks >= leafLimit)) {
                            event.setCancelled(true);
                            return;
                        }

                        if (allowNearCobwebs &&
                                mat == Material.WEB &&
                                (++webBlocks >= cobwebLimit)) {
                            return;
                        }

                        if (allowMobSpawners &&
                                mat == Material.MOB_SPAWNER) {
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