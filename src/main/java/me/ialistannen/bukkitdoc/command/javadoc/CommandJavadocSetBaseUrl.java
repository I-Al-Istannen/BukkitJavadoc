package me.ialistannen.bukkitdoc.command.javadoc;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.Command;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;

/**
 * Sets the Javadoc base url
 */
public class CommandJavadocSetBaseUrl extends Command {

    private Map<String, String> knownUrlsMap = new HashMap<>();

    {
        knownUrlsMap.put("bukkit", "https://hub.spigotmc.org/javadocs/bukkit/");
        knownUrlsMap.put("spigot", "https://hub.spigotmc.org/javadocs/spigot/");
        knownUrlsMap.put("javafx", "https://docs.oracle.com/javase/8/javafx/api/");
        knownUrlsMap.put("java", "https://docs.oracle.com/javase/8/docs/api/");
    }

    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    @Override
    public Type execute(IChannel channel, IMessage message, String[] arguments) {
        if (arguments.length < 1) {
            return Type.SEND_USAGE;
        }

        if (!message.getAuthor().getPermissionsForGuild(channel.getGuild()).contains(Permissions.ADMINISTRATOR)) {
            MessageBuilder builder = new MessageBuilder(Bot.getClient()).withChannel(channel)
                    .appendContent("Error!", Styles.BOLD_ITALICS)
                    .appendContent(" ")
                    .appendContent("You do not have the permission")
                    .appendContent(" ")
                    .appendContent("Administrator", Styles.INLINE_CODE)
                    .appendContent(".");
            MessageUtil.sendMessage(builder);
            return Type.SUCCESSFULLY_INVOKED;
        }

        String baseUrl = arguments[0];

        if (knownUrlsMap.containsKey(baseUrl)) {
            baseUrl = knownUrlsMap.get(baseUrl);
        }

        JavadocConstants.INSTANCE.setBaseUrl(baseUrl);

        MessageUtil.sendMessage(
                new MessageBuilder(Bot.getClient()).withChannel(channel)
                        .appendContent("Set the base url to", Styles.ITALICS)
                        .appendContent(" ")
                        .appendContent(baseUrl, Styles.INLINE_CODE)
        );

        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "setBaseUrl";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "javadoc setBaseUrl <base url>\n"
                + "\t\t\t| " + CommandExecutor.PREFIX + "javadoc setBaseUrl <"
                + knownUrlsMap.keySet().stream().collect(Collectors.joining("|"))
                + ">";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Sets the javadoc base url";
    }
}
