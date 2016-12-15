package me.ialistannen.bukkitdoc.command.javadoc;

/**
 * Some Javadoc constants
 */
public enum JavadocConstants {

    INSTANCE;

    private String baseUrl = "https://hub.spigotmc.org/javadocs/bukkit/";

    /**
     * @param baseUrl The base Javadoc Url
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return The base Javadoc Url
     */
    public String getBaseUrl() {
        return baseUrl;
    }

}
