package me.ialistannen.bukkitdoc.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
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
}
