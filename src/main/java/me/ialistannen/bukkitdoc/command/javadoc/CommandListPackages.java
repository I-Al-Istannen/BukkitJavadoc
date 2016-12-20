package me.ialistannen.bukkitdoc.command.javadoc;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.Command;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.packages.Package;
import me.ialistannen.bukkitdoc.command.javadoc.util.packages.PackageParser;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.NumberUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;


/**
 * Lists the packages
 */
class CommandListPackages extends Command {
    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    @Override
    public Type execute(IChannel channel, IMessage message, String[] arguments) {
        String baseUrl = JavadocConstants.INSTANCE.getBaseUrl();

        PackageParser packageParser = new PackageParser(baseUrl);
        Collection<Package> packages = packageParser.parse();

        StringBuilder builder = new StringBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(new Color(NumberUtil.getRandomInt()))
                .withThumbnail("https://www.jetbrains.com/help/img/idea/2016.3/iconPackage.png")
                .withAuthorName("Package list")
                .withAuthorUrl(baseUrl);

        {
            for (Package aPackage : packages) {
                if (builder.length() + aPackage.getName().length() > 1010) {
                    String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
                    embedBuilder.appendField("Packages", content, false);
                    builder = new StringBuilder();
                }

                builder.append(aPackage.getName())
                        .append("\n");
            }
            String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
            embedBuilder.appendField("Packages", content, true);
        }
        embedBuilder.withDesc("\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_\\_");

        MessageUtil.sendSelfDestructingMessage(
                MessageUtil.sendMessage(
                        new MessageBuilder(Bot.getClient()).withChannel(channel)
                                .withEmbed(embedBuilder.build())
                ),
                TimeUnit.SECONDS, 20);

        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "list";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "javadoc package list";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Lists all packages";
    }
}
