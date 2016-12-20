package me.ialistannen.bukkitdoc.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.javadoc.CommandJavadoc;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;

/**
 * Executes commands
 */
public class CommandExecutor implements IListener<MessageReceivedEvent> {

    public static final String PREFIX = "-";
    
    private Command root;
    private ExecutorService executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE,
            5L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    /**
     * @param root The root {@link Command}
     */
    private CommandExecutor(Command root) {
        this.root = root;
    }

    /**
     * Uses the default root
     *
     * @see #CommandExecutor(Command)
     */
    public CommandExecutor() {
        this(new RootCommand());
        
        addCommandToRoot(new CommandJavadoc());
        addCommandToRoot(new CommandExit());
        addCommandToRoot(new CommandVersion());
    }

    /**
     * Adds the command to the root
     *
     * @param command The command to add
     */
    private void addCommandToRoot(Command command) {
        root.addChild(command);
    }

    /**
     * @return The root node
     */
    public Command getRoot() {
        return root;
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        if (!event.getMessage().getContent().startsWith(PREFIX)) {
            return;
        }

        executor.execute(() -> {
            CommandExecuteResult commandExecuteResult = root.executeCommand(
                    event.getMessage().getContent().trim().substring(PREFIX.length()),
                    event.getMessage().getChannel(),
                    event.getMessage()
            );
            
            if (commandExecuteResult.getType() == Type.SEND_USAGE) {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                Command command = commandExecuteResult.getCommand().get();

                MessageBuilder usage = new MessageBuilder(Bot.getClient()).withChannel(event.getMessage().getChannel())
                        .appendContent("Usage: ", Styles.BOLD)
                        .appendContent(command.getUsage(), Styles.INLINE_CODE);

                MessageUtil.sendSelfDestructingMessage(
                        MessageUtil.sendMessage(usage),
                        TimeUnit.SECONDS,
                        5
                );
            }
        });
    }

    /**
     * A root command
     */
    private static class RootCommand extends Command {

        /**
         * Executes the command
         *
         * @param channel The channel the message was sent in
         * @param message The message that was sent
         * @param arguments The arguments after the command name
         */
        @Override
        public Type execute(IChannel channel, IMessage message, String[] arguments) {
            return Type.SUCCESSFULLY_INVOKED;
        }

        @Override
        public boolean isTransparent() {
            return true;
        }

        /**
         * Returns the command keyword
         *
         * @return The keyword for the command
         */
        @Override
        public String getKeyword() {
            return "root";
        }

        /**
         * Returns the command usage
         *
         * @return The usage for the command
         */
        @Override
        public String getUsage() {
            return "root usage";
        }

        /**
         * Returns a command description
         *
         * @return The Description for this command
         */
        @Override
        public String getDescription() {
            return "root description";
        }
    }
}
