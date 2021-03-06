package me.ialistannen.bukkitdoc.command;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;

/**
 * Exists the bot
 */
public class CommandExit extends Command {
    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    @Override
    public Type execute(IChannel channel, IMessage message, String[] arguments) {
        if(!MessageUtil.checkAndSendAdminOnlyMessage(channel, message)) {
            return Type.SUCCESSFULLY_INVOKED;
        }
        IUser author = message.getAuthor();

        MessageUtil.sendMessage(new MessageBuilder(Bot.getClient()).withChannel(channel)
                .appendContent("Shutting down!", Styles.BOLD_ITALICS)
                .appendContent("\n")
                .appendContent("Initiated by", Styles.ITALICS)
                .appendContent(" ")
                .appendContent(author.getName(), Styles.INLINE_CODE)
        );
        System.exit(0);
        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "exit";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "exit";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Exits the bot";
    }
}
