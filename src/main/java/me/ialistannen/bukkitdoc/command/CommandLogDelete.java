package me.ialistannen.bukkitdoc.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;

/**
 * Deletes the log file
 */
public class CommandLogDelete extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLogDelete.class);

    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    @Override
    public Type execute(IChannel channel, IMessage message, String[] arguments) {
        if (!MessageUtil.checkAndSendAdminOnlyMessage(channel, message)) {
            return Type.SUCCESSFULLY_INVOKED;
        }

        Path path = Paths.get("BukkitDocsLog.log");
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.warn("Couldn't delete log file", e);
        }
        MessageUtil.sendMessage(
                new MessageBuilder(Bot.getClient()).withChannel(channel)
                        .appendContent("Deleted the log file!", Styles.ITALICS)
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
        return "delete";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "log delete";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Deletes the log file";
    }
}
