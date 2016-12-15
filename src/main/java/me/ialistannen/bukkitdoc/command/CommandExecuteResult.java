package me.ialistannen.bukkitdoc.command;

import java.util.Optional;

/**
 * The result of executing a command
 */
public class CommandExecuteResult {

    public static final CommandExecuteResult NOT_FOUND = new CommandExecuteResult(Type.NOT_FOUND, null);

    private Type type;
    private Command command;

    /**
     * @param type    The {@link Type}
     * @param command The {@link Command}
     */
    CommandExecuteResult(Type type, Command command) {
        this.type = type;
        this.command = command;
    }

    /**
     * @return True if the command was found
     */
    public boolean isFound() {
        return getCommand().isPresent();
    }

    /**
     * @return The {@link Type}
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The command
     */
    public Optional<Command> getCommand() {
        return Optional.ofNullable(command);
    }

    /**
     * The type of the result
     */
    public enum Type {
        /**
         * The command was not found
         */
        NOT_FOUND,
        /**
         * The command was successfully invoked
         */
        SUCCESSFULLY_INVOKED,
        /**
         * Send the usage!
         */
        SEND_USAGE
    }
}
