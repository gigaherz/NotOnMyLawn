
package net.gigaherz.NotOnMyLawn;

class ConfigException extends Exception {
    public ConfigException(String parameter) {
        super("Invalid value in settings '" + parameter + "'");
    }
}
