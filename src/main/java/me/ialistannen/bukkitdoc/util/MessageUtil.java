package me.ialistannen.bukkitdoc.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.ialistannen.bukkitdoc.Bot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Some message utilities
 */
public class MessageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

    /**
     * @param message The RestAction to queue and then to delete
     * @param unit The {@link TimeUnit}
     * @param amount The amount of time
     */
    public static void sendSelfDestructingMessage(IMessage message, TimeUnit unit, long amount) {
        try {
            Thread.sleep(unit.toMillis(amount));
            message.delete();
        } catch (InterruptedException | DiscordException | MissingPermissionsException | RateLimitException e) {
            LOGGER.warn("Error sending a self destructing message", e);
        }
    }

    /**
     * @param builder The {@link MessageBuilder} to build
     *
     * @return The message or null
     */
    public static IMessage sendMessage(MessageBuilder builder) {
        try {
            return builder.send();
        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            LOGGER.warn("Error while sending a message: '" + builder + "'", e);
        }
        return null;
    }

    /**
     * @param message The message to edit
     * @param newContent The new content
     *
     * @return The message or null
     */
    public static IMessage editMessage(IMessage message, String newContent) {
        try {
            return message.edit(newContent);
        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            LOGGER.warn("Error while editing the message: '" + newContent + "'", e);
        }
        return null;
    }

    /**
     * Checks if they are allowed to use a command that needs higher rights
     *
     * @param channel The channel The message was send in
     * @param message The message
     *
     * @return True if they are allowed to use it
     */
    public static boolean checkAndSendAdminOnlyMessage(IChannel channel, IMessage message) {
        IUser author = message.getAuthor();
        if (!author.getPermissionsForGuild(channel.getGuild()).contains(Permissions.ADMINISTRATOR)
                && !author.getID().equals("155954930191040513") // ARSEN
                && !author.getID().equals("138235433115975680") // MYSELF
                ) {

            IMessage errorMessage = MessageUtil.sendMessage(new MessageBuilder(Bot.getClient()).withChannel(channel)
                    .appendContent("Error!", Styles.BOLD_ITALICS)
                    .appendContent(" ")
                    .appendContent("No permission. You are not")
                    .appendContent(" ")
                    .appendContent("Administrator", Styles.INLINE_CODE)
                    .appendContent(" ")
                    .appendContent("Or <Arsen> or <I Al Istannen>", Styles.ITALICS)
            );

            // destroy the warning
            sendSelfDestructingMessage(errorMessage, TimeUnit.SECONDS, 20);
            return false;
        }

        return true;
    }
}
