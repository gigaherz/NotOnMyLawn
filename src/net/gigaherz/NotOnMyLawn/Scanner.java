package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
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
    Scanner parent;
    String name;
    boolean enable;
    Actions action;
    Actions fallback;
    int priority;
    int range;
    int below;
    int above;
    int count;
    int hardLimit;
    List<Material> blocks = null;
    List<Scanner> nested = new ArrayList<Scanner>();

    public Scanner(String name) {
        this.name = name;
        setDefaults();
    }

    public Scanner(String name, Scanner parent) {
        this.parent = parent;
        this.name = name;
        setDefaults();
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

    private void setDefaults() {
        enable = true;
        priority = 1;
        if (parent == null) {
            action = Actions.PREVENT;
            fallback = Actions.CONTINUE;
            range = 5;
            below = 2;
            above = 2;
            count = 1;
        } else {
            action = Actions.COUNT;
            fallback = Actions.SKIP;
            range = 0;
            below = 0;
            above = 0;
            count = 1;
        }
    }

    public void loadConfig(ConfigurationSection config, Logger logger)
            throws ConfigException {

        if (config == null) {
            throw new ConfigException("config");
        }

        setDefaults();

        String act = config.getString("action", action.toString());
        String fal = config.getString("fallback", fallback.toString());

        action = Actions.valueOf(act.toUpperCase());
        fallback = Actions.valueOf(fal.toUpperCase());

        if (parent == null) {
            if (action == Actions.COUNT || action == Actions.SKIP) {
                throw new ConfigException("action", "Cannot use COUNT or SKIP in a root scanner.");
            }
            if (fallback == Actions.COUNT || fallback == Actions.SKIP) {
                throw new ConfigException("fallback", "Cannot use COUNT or SKIP in a root scanner.");
            }
        }

        enable = config.getBoolean("enable", enable);
        range = config.getInt("range", range);
        below = config.getInt("below", below);
        above = config.getInt("above", above);
        count = config.getInt("count", count);

        int base = (range * 2 + 1) * (range * 2 + 1);
        int height = below + above + 1;
        int totalBlocks = base * height;

        hardLimit = totalBlocks / 10; // default hardLimit to 10% of the total blocks
        hardLimit = config.getInt("hard_limit", hardLimit);

        if (config.contains("blocks")) {
            String sBlocks = config.getString("blocks");
            List<String> oBlocks = config.getStringList("blocks");

            if (oBlocks.isEmpty() && sBlocks != null) {

                if (!sBlocks.equalsIgnoreCase("inherit")) {
                    if (blocks == null) {
                        blocks = new ArrayList<Material>();
                    } else {
                        blocks.clear();
                    }

                    blocks.add(Material.getMaterial(sBlocks));
                } else {
                    blocks = null;
                }
            } else if (!oBlocks.isEmpty()) {
                if (blocks == null) {
                    blocks = new ArrayList<Material>();
                } else {
                    blocks.clear();
                }

                List<String> blockTypes = (List<String>) oBlocks;
                for (String bType : blockTypes) {
                    Material mat = Material.getMaterial(bType);
                    if (mat == null) {
                        throw new ConfigException("blocks", "Invalid material name '" + bType + "'.");
                    } else {
                        blocks.add(mat);
                    }
                }
            } else {
                throw new ConfigException("blocks", "A scanner requires at least one block to match.");
            }
        } else if (parent == null) {
            throw new ConfigException("blocks", "Cannot inherit blocks in a root scanner.");
        }

        keys:
        for (String str : config.getKeys(false)) {
            for (String reserved : BukkitPlugin.reservedWords) {
                if (reserved.equalsIgnoreCase(str)) {
                    continue keys;
                }
            }
            Scanner nest = new Scanner(str, this);
            ConfigurationSection sect =
                    config.getConfigurationSection(str);
            if (sect != null) {
                nest.loadConfig(sect, logger);
                nested.add(nest);
            } else {
                logger.warning("Skipped key " + str + " because it was not a section.");
            }
        }

        Collections.sort(nested, Scanner.Comparator);
    }

    public String config(ConfigurationSection config, String key, String newValue)
            throws ConfigException {

        int dot = key.indexOf('.');
        if (dot >= 0) {
            String str = key.substring(0, dot);
            String sub = key.substring(dot + 1);

            for (String reserved : BukkitPlugin.reservedWords) {
                if (reserved.equalsIgnoreCase(str)) {
                    throw new ConfigException("key", "The specified scanner name is a reserved config keyword.");
                }
            }

            for (Scanner scanner : nested) {
                if (scanner.name.equalsIgnoreCase(str)) {
                    ConfigurationSection conf = config.getConfigurationSection(str);
                    return scanner.config(conf, sub, newValue);
                }
            }

            throw new ConfigException("key", "The nested scanner was not found.");
        } else {
            if (key.equalsIgnoreCase("enable")) {
                if (newValue != null) {
                    enable = BukkitPlugin.parseBooleanStrict(newValue);
                    config.set("enable", enable);
                }
                return Boolean.toString(enable);
            } else if (key.equalsIgnoreCase("action")) {
                if (newValue != null) {
                    action = Actions.valueOf(newValue.toUpperCase());
                    config.set("action", action.toString());
                }
                return action.toString();
            } else if (key.equalsIgnoreCase("fallback")) {
                if (newValue != null) {
                    fallback = Actions.valueOf(newValue.toUpperCase());
                    config.set("fallback", fallback.toString());
                }
                return fallback.toString();
            } else if (key.equalsIgnoreCase("priority")) {
                if (newValue != null) {
                    priority = Integer.parseInt(newValue);
                    config.set("priority", priority);
                }
                return Integer.toString(priority);
            } else if (key.equalsIgnoreCase("range")) {
                if (newValue != null) {
                    range = Integer.parseInt(newValue);
                    config.set("range", range);
                }
                return Integer.toString(range);
            } else if (key.equalsIgnoreCase("below")) {
                if (newValue != null) {
                    below = Integer.parseInt(newValue);
                    config.set("below", below);
                }
                return Integer.toString(below);
            } else if (key.equalsIgnoreCase("above")) {
                if (newValue != null) {
                    above = Integer.parseInt(newValue);
                    config.set("above", above);
                }
                return Integer.toString(above);
            } else if (key.equalsIgnoreCase("count")) {
                if (newValue != null) {
                    count = Integer.parseInt(newValue);
                    config.set("count", count);
                }
                return Integer.toString(count);
            } else if (key.equalsIgnoreCase("hard_limit")) {
                if (newValue != null) {
                    hardLimit = Integer.parseInt(newValue);
                    config.set("hard_limit", hardLimit);
                }
                return Integer.toString(hardLimit);
            } else {
                throw new ConfigException("key", "The specified key does not correspond with any known setting key.");
            }
        }
    }

    public Actions runScanner(World world, int wx, int wy, int wz) {
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

                    loop:
                    for (Scanner nest : nested) {
                        switch (nest.runScanner(world, x, y, z)) {
                            case PREVENT:
                                return Actions.PREVENT;
                            case ALLOW:
                                return Actions.ALLOW;
                            case CONTINUE:
                                continue loop;
                            case COUNT:
                                if (++counter >= count) {
                                    return action;
                                }
                                continue scanning;
                            case SKIP:
                                continue scanning;
                        }
                    }

                    // if there's no nested scanners enabled,
                    // or all nested scanners said "continue", count.
                    if (++counter >= count) {
                        return action;
                    }
                }
            }
        }

        return fallback;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Scanner ");
        sb.append(name);
        sb.append(" { ");
        sb.append(enable ? "enabled, " : "disabled, ");
        sb.append("priority ");
        sb.append(priority);
        sb.append(", ");
        sb.append("range [");
        sb.append(range);
        sb.append(", ");
        sb.append(-below);
        sb.append(", ");
        sb.append(above);
        sb.append("], ");
        sb.append("limit ");
        sb.append(count);
        sb.append(" hard ");
        sb.append(hardLimit);
        sb.append(", ");

        // blocks
        List<Material> mats = getBlocks();
        if (mats.isEmpty()) {
            sb.append("NO BLOCKS, ");
        } else {
            sb.append(" blocks [");
            sb.append(mats.get(0).toString());
            for (int i = 1; i < mats.size(); i++) {
                sb.append(", ");
                sb.append(mats.get(i).toString());
            }
            sb.append("], ");
        }
        sb.append(nested.size());
        sb.append(" nested scanners");
        sb.append("};");

        return sb.toString();
    }
}
