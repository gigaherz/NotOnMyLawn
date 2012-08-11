package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements Listener {
    
    public final static List<String> reservedWords = new ArrayList<String>(Arrays.asList(
            "enable", "range", "below", "above", "count", "hard_limit",
            "blocks", "action", "priority", "fallback", "ignore_animals",
            "min_height"));

    // general
    boolean enable;
    boolean ignoreAnimals;
    int minHeight;
    List<Scanner> scanners = new ArrayList<Scanner>();

    @Override
    public void onEnable() {
        Configuration settings = getConfig();

        if (!settings.getString("version").equals("1.2")) {
            getServer().getLogger().info(
                    "[NotOnMyLawn] " +
                    "Unsupported config file, reloading defaults.");
            saveDefaultConfig();
        }

        if (!loadSettings()) {
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getLogger().info("[NotOnMyLawn] Is now Enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[NotOnMyLawn] disabled.");
    }

    private boolean loadSettings() {
        reloadConfig();

        Configuration settings = getConfig();

        enable = settings.getBoolean("enable");
        ignoreAnimals = settings.getBoolean("ignore_animals");
        minHeight = settings.getInt("min_height");

        ConfigurationSection section =
                settings.getConfigurationSection("scanners");

        try {
            for (String str : section.getKeys(false)) {
                Scanner nest = new Scanner(str);
                nest.loadConfig(section.getConfigurationSection(str),
                        getServer().getLogger());
                scanners.add(nest);
            }
            Collections.sort(scanners, Scanner.Comparator);
        } catch (ConfigException e) {
            getServer().getLogger().info("[NotOnMyLawn] "
                    + e.getMessage() + ".");
            setEnabled(false);
            return false;
        }

        getServer().getLogger().info("[NotOnMyLawn] Configuration loaded: "
                + scanners.size() + " top-level scanners created.");
        return true;
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
                if (!loadSettings()) {
                    return true;
                }
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
                    sender.sendMessage(config(args[1], null));
                }
            } else if (action.equalsIgnoreCase("set")) {
                if (args.length < 3) {
                    return false;
                }
                
                if (args[1].equalsIgnoreCase("enable")) {
                    config(args[1], args[2]);
                    if (enable) {
                        sender.sendMessage("The plugin is now enabled.");
                    } else {
                        sender.sendMessage("The plugin is now disabled.");
                    }
                } else {
                    sender.sendMessage(config(args[1], args[2]));
                }
            } else {
                return false;
            }

            return true;
        }
        return false;
    }

    private String config(String key, String newValue) {
        Configuration settings = getConfig();
        
        String result;

        int dot = key.indexOf('.');
        
        tryCatch:
        try {
            if (key.equalsIgnoreCase("enable")) {
                if(newValue != null) {
                    enable = parseBooleanStrict(newValue);
                    settings.set("enable", enable);
                }
                result = Boolean.toString(enable);
            } else if (key.equalsIgnoreCase("ignore_animals")) {
                if(newValue != null) {
                    ignoreAnimals = parseBooleanStrict(newValue);
                    settings.set("ignore_animals", ignoreAnimals);
                }
                result = Boolean.toString(ignoreAnimals);
            } else if (key.equalsIgnoreCase("min_height")) {
                if(newValue != null) {
                    minHeight = Integer.parseInt(newValue);
                    settings.set("min_height", minHeight);
                }
                result = Integer.toString(minHeight);
            } else if(dot >= 0) {                
                String str = key.substring(0, dot);
                String sub = key.substring(dot+1);

                for (String reserved : reservedWords) {
                    if (reserved.equalsIgnoreCase(str)) {
                        throw new ConfigException("key", "The specified scanner name is a reserved config keyword.");
                    }
                }
                
                ConfigurationSection config = getConfig().getConfigurationSection("scanners");

                for(Scanner scanner : scanners) {
                    if(scanner.name.equalsIgnoreCase(str)) {
                        ConfigurationSection conf = config.getConfigurationSection(str);
                        getLogger().warning("Found scanner '" + str + "'. " + (conf != null));
                        result = scanner.config(conf, sub, newValue);
                        break tryCatch;
                    }
                }
                
                throw new ConfigException("key", "The nested scanner was not found.");
            } else {
                throw new ConfigException("key", "The specified key does not correspond with any known setting key.");
            }
        } catch (NumberFormatException e) {
            return "Invalid setting '" + key + "': Unable to parse the value.";
        } catch (ConfigException e) {
            return e.getMessage();
        }

        saveConfig();
        
        if(newValue != null) {
           return "The new value of " + key + " is: " + result;
        } else {
           return "The current value of " + key + " is: " + result;
        }
    }

    public static boolean parseBooleanStrict(String text)
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
        if (wy < minHeight) {
            return;
        }

        int wx = loc.getBlockX();
        int wz = loc.getBlockZ();

        World world = event.getEntity().getWorld();

        loop:
        for (Scanner nest : scanners) {
            switch (nest.runScanner(world, wx, wy, wz)) {
                case PREVENT:
                    event.setCancelled(true);
                    return;
                case ALLOW:
                    return;
                case CONTINUE:
                    return;
                case COUNT:
                    getServer().getLogger()
                            .info("[NotOnMyLawn] Invalid state.");
                    setEnabled(false);
                    return;
                case SKIP:
                    getServer().getLogger()
                            .info("[NotOnMyLawn] Invalid state.");
                    setEnabled(false);
                    return;
            }
        }
    }
}