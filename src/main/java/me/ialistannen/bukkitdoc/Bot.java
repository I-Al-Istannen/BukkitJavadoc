package me.ialistannen.bukkitdoc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import me.ialistannen.bukkitdoc.command.CommandExecutor;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * The bot main class
 */
public class Bot {
    private static Bot instance;

    private static final String VERSION = "1.0.4-SNAPSHOT";

    private IDiscordClient client;

    private Bot(String token) throws DiscordException, RateLimitException {
        client = new ClientBuilder().withToken(token).build();
        client.getDispatcher().registerListener(new CommandExecutor());
        client.login();
    }

    /**
     * @return The client
     */
    public static IDiscordClient getClient() {
        return getInstance().client;
    }


    /**
     * @return The instance
     */
    public static Bot getInstance() {
        return instance;
    }

    /**
     * Returns the Bot version
     *
     * @return The Bot version
     */
    public static String getVersion() {
        return VERSION;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Error! No token given. Arguments: <token>");
            return;
        }
        String token = args[0];
        if (args.length >= 2) {
            String path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            token = Files.readAllLines(Paths.get(path)).get(0);
        }
        instance = new Bot(token);
    }
}
