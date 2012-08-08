package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Scanner {

    public final static Comparator<Scanner> Comparator = new Comparator<Scanner>() {
        public int compare(Scanner o1, Scanner o2) {
            return o1.priority - o2.priority;
        }
    };
    final static List<String> reservedWords = new ArrayList<String>(Arrays.asList(
            "enable", "range", "below", "above", "count", "hard_limit",
            "blocks", "action", "priority", "fallback"));
    Scanner parent = null;
    String name;
    boolean enable = true;
    Actions action = Actions.PREVENT;
    Actions fallback = Actions.CONTINUE;
    int priority = 1;
    int range = 5;
    int below = 2;
    int above = 2;
    int count = 1;
    int hardLimit = 50;
    List<Material> blocks = null;
    List<Scanner> nested = new ArrayList<Scanner>();

    public Scanner(String name) {
        this.name = name;
        blocks = new ArrayList<Material>();
    }

    public Scanner(String name, Scanner parent) {
        this.parent = parent;
        this.name = name;
        action = Actions.COUNT;
        fallback = Actions.SKIP;
        range = 0;
        below = 0;
        above = 0;
        count = 1;
    }

    public void LoadConfig(ConfigurationSection config, Logger logger)
            throws ConfigException {

        if (config == null) {
            throw new ConfigException("config");
        }

        String act = config.getString("action", action.toString());
        String fal = config.getString("fallback", fallback.toString());

        action = Actions.valueOf(act.toUpperCase());
        fallback = Actions.valueOf(fal.toUpperCase());

        if (parent == null) {
            if (action == Actions.COUNT || action == Actions.SKIP) {
                throw new ConfigException("action");
            }
            if (fallback == Actions.COUNT || fallback == Actions.SKIP) {
                throw new ConfigException("fallback");
            }
        }

        enable = config.getBoolean("enable", enable);
        range = config.getInt("range", range);
        below = config.getInt("below", below);
        above = config.getInt("above", above);
        count = config.getInt("count", count);
        hardLimit = config.getInt("hard_limit", hardLimit);

        if (config.contains("blocks")) {
            List<String> oBlocks = config.getStringList("blocks");

            if (oBlocks.size() == 1) {
                String sBlocks = oBlocks.get(0);

                if (!sBlocks.equalsIgnoreCase("inherit")) {
                    if (blocks == null) {
                        blocks = new ArrayList<Material>();
                    }

                    blocks.add(Material.getMaterial(sBlocks));
                }
            } else if (oBlocks instanceof List) {
                if (blocks == null) {
                    blocks = new ArrayList<Material>();
                }

                List<String> blockTypes = (List<String>) oBlocks;
                for (String bType : blockTypes) {
                    blocks.add(Material.getMaterial(bType));
                }
            }
        } else if (parent == null) {
            throw new ConfigException("blocks");
        }

        keys:
        for (String str : config.getKeys(false)) {
            for (String reserved : reservedWords) {
                if (reserved.equalsIgnoreCase(str)) {
                    continue keys;
                }
            }
            Scanner nest = new Scanner(str, this);
            ConfigurationSection sect =
                    config.getConfigurationSection(str);
            if (sect != null) {
                nest.LoadConfig(sect, logger);
                nested.add(nest);
            } else {
                logger.warning("Skipped key " + str + " because it was not a section.");
            }
        }

        Collections.sort(nested, Scanner.Comparator);
    }

    private List<Material> getBlocks() {
        if (blocks != null) {
            return blocks;
        }

        if (parent != null) {
            return parent.getBlocks();
        }

        return null;
    }

    public Actions RunScanner(World world, int wx, int wy, int wz) {
        int counter = 0;
        int hardCounter = 0;

        if (!enable) {
            return Actions.CONTINUE;
        }

        for (int y = wy - 2; y < wy + 1; y++) {
            for (int x = wx - 5; x < wx + 5; x++) {
                scanning:
                for (int z = wz - 5; z < wz + 5; z++) {
                    Material mat = Material.getMaterial(
                            world.getBlockTypeIdAt(x, y, z));

                    if (!getBlocks().contains(mat)) {
                        continue;
                    }

                    if (++hardCounter >= hardLimit) {
                        return action;
                    }

                    if (nested.size() > 0) {
                        loop:
                        for (Scanner nest : nested) {
                            switch (nest.RunScanner(world, x, y, z)) {
                                case PREVENT:
                                    return Actions.PREVENT;
                                case ALLOW:
                                    return Actions.ALLOW;
                                case CONTINUE:
                                    continue scanning;
                                case COUNT:
                                    if (++counter >= count) {
                                        return action;
                                    }
                                    break;
                                case SKIP:
                                    break loop;
                            }
                        }
                    } else {
                        if (++counter >= count) {
                            return action;
                        }
                    }
                }
            }
        }

        return fallback;
    }
}
