package me.ialistannen.bukkitdoc.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * A command
 */
public abstract class Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    private List<Command> children = new ArrayList<>();

    /**
     * Returns all child commands
     *
     * @return All the children, unmodifiable
     */
    public List<Command> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns <b>all</b> children of this command
     *
     * @return All children, recursive.
     */
    private List<Command> getChildrenRecursive() {
        List<Command> list = new LinkedList<>();
        list.addAll(getChildren());

        for (Command command : getChildren()) {
            list.addAll(command.getChildrenRecursive());
        }

        return list;
    }

    /**
     * @param command The child to add
     */
    public void addChild(Command command) {
        children.add(command);
    }

    /**
     * Transparent commands will be considered as auto-matching and the keyword will be distributed to the children
     * instead
     *
     * @return True if this command is transparent.
     */
    public boolean isTransparent() {
        return false;
    }

    /**
     * Executes a command
     *
     * @param query The query to search the Command with
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     *
     * @return The {@link CommandExecuteResult}
     */
    public CommandExecuteResult executeCommand(String query, IChannel channel, IMessage message) {
        FindCommandResult commandResult = findCommand(query.trim());
        if (!commandResult.isFound()) {
            return CommandExecuteResult.NOT_FOUND;
        }
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Command command = commandResult.getCommand().get();

        LOGGER.info("Executing command {} ({})", command.getKeyword(), command.getClass().getSimpleName());
        Type result = command.execute(channel, message, commandResult.getRestArgs().toArray(new String[0]));
        if (result == Type.SEND_USAGE) {
            return new CommandExecuteResult(Type.SEND_USAGE, command);
        }
        return new CommandExecuteResult(Type.SUCCESSFULLY_INVOKED, command);
    }

    /**
     * Tries to find a command
     *
     * @param query The query to use
     */
    public FindCommandResult findCommand(String query) {
        return findCommand(new LinkedList<>(Arrays.asList(query.split(" "))));
    }

    /**
     * Finds a command
     *
     * @param query The query to use
     *
     * @return The found command
     */
    private FindCommandResult findCommand(Queue<String> query) {
        if (!isTransparent()) {
            String keyword = query.poll();

            if (!isYourKeyword(keyword)) {
                return new FindCommandResult(null, new LinkedList<>(query));
            }

            for (Command child : getChildren()) {
                FindCommandResult command = child.findCommand(new LinkedList<>(query));
                if (command.getCommand().isPresent()) {
                    return new FindCommandResult(command.getCommand().get(), command.getRestArgs());
                }
            }

            return new FindCommandResult(this, query);
        }
        else {
            for (Command child : getChildren()) {
                FindCommandResult command = child.findCommand(new LinkedList<>(query));
                if (command.getCommand().isPresent()) {
                    return new FindCommandResult(command.getCommand().get(), command.getRestArgs());
                }
            }

            // this node is transparent
            return new FindCommandResult(null, Collections.emptyList());
        }
    }

    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    public abstract Type execute(IChannel channel, IMessage message, String[] arguments);

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    public abstract String getKeyword();

    /**
     * @param keyword The keyword to check
     *
     * @return True if it is your keyword
     */
    protected boolean isYourKeyword(String keyword) {
        return getKeyword().equalsIgnoreCase(keyword);
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    public abstract String getUsage();

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    public abstract String getDescription();

    /**
     * The result of the Command{@link #findCommand(String)} method
     */
    public static class FindCommandResult {
        private Command command;
        private List<String> restArgs;

        /**
         * @param command The command
         * @param restArgs The rest arguments
         */
        private FindCommandResult(Command command, Collection<String> restArgs) {
            this.command = command;
            this.restArgs = restArgs == null ? new LinkedList<>() : new LinkedList<>(restArgs);
        }

        /**
         * @return Checks if a command was found
         */
        public boolean isFound() {
            return getCommand().isPresent();
        }

        /**
         * @return The Command, if any
         */
        public Optional<Command> getCommand() {
            return Optional.ofNullable(command);
        }

        /**
         * @return The arguments still left
         */
        public ArrayList<String> getRestArgs() {
            return new ArrayList<>(restArgs);
        }
    }
}
