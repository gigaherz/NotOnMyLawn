package net.gigaherz.NotOnMyLawn;

import java.util.ArrayList;
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

    // general
    boolean enable;
    boolean ignoreAnimals;
    int heightLimit;
    List<Scanner> scanners = new ArrayList<Scanner>();

    @Override
    public void onEnable() {
        Configuration settings = getConfig();

        if (!settings.getString("version").equals("1.2")) {
            getServer().getLogger().info(
                    "[NotOnMyLawn] Unsupported config file, reloading defaults.");
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
        heightLimit = settings.getInt("min_height");

        ConfigurationSection section =
                settings.getConfigurationSection("scanners");

        try {
            for (String str : section.getKeys(false)) {
                Scanner nest = new Scanner(str);
                nest.LoadConfig(section.getConfigurationSection(str), getServer().getLogger());
                scanners.add(nest);
            }
            Collections.sort(scanners, Scanner.Comparator);
        } catch (ConfigException e) {
            getServer().getLogger().info("[NotOnMyLawn] "+e.getMessage()+".");
            setEnabled(false);
            return false;
        }

        getServer().getLogger().info("[NotOnMyLawn] Configuration loaded: " + scanners.size() + " top-level scanners created.");
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
        if (wy < heightLimit) {
            return;
        }

        int wx = loc.getBlockX();
        int wz = loc.getBlockZ();

        World world = event.getEntity().getWorld();

        loop:
        for (Scanner nest : scanners) {
            switch (nest.RunScanner(world, wx, wy, wz)) {
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