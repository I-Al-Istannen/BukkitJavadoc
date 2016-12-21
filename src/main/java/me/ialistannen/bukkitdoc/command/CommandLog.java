package me.ialistannen.bukkitdoc.command;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Sends the log file
 */
public class CommandLog extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLog.class);

    {
        addChild(new CommandLogDelete());
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
        if (!MessageUtil.checkAndSendAdminOnlyMessage(channel, message)) {
            return Type.SUCCESSFULLY_INVOKED;
        }

        Path path = Paths.get("BukkitDocsLog.log");

        try {
            channel.sendFile(path.toFile());
        } catch (FileNotFoundException e) {
            LOGGER.warn("Error, log file not found!", e);
        } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
            LOGGER.warn("Error sending log", e);
        }

        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "log";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "log"
                + getChildren()
                .stream()
                .map(Command::getUsage)
                .collect(Collectors.joining("\n\t\t| ", "\n\t\t| ", ""));
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Sends the log file";
    }
}
