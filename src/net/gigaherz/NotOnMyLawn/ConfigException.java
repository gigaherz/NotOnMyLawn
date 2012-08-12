
package net.gigaherz.NotOnMyLawn;

class ConfigException extends Exception {
    public ConfigException(String parameter) {
        super("Invalid setting '" + parameter + "'");
    }
    public ConfigException(String parameter, String reason) {
        super("Invalid setting '" + parameter + "': " + reason);
    }
}
