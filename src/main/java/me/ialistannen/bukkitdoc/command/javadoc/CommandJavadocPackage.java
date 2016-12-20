package me.ialistannen.bukkitdoc.command.javadoc;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.Command;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.packages.Package;
import me.ialistannen.bukkitdoc.command.javadoc.util.packages.PackageParser;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.NumberUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import me.ialistannen.htmltodiscord.HtmlConverter;
import me.ialistannen.htmltodiscord.MapperCollection;
import me.ialistannen.htmltodiscord.StandardMappers;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;

/**
 * Describes a package
 */
class CommandJavadocPackage extends Command {

    {
        addChild(new CommandListPackages());
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
        if (arguments.length < 1) {
            return Type.SEND_USAGE;
        }

        String packageName = arguments[0].toLowerCase();

        String baseUrl = JavadocConstants.INSTANCE.getBaseUrl();

        PackageParser packageParser = new PackageParser(baseUrl);
        Collection<Package> packages = packageParser.parse();

        List<Package> possibleMatches = packages.stream()
                .filter(aPackage -> aPackage.getName().toLowerCase().endsWith(packageName))
                .collect(Collectors.toList());

        if (possibleMatches.isEmpty()) {
            MessageUtil.sendMessage(
                    new MessageBuilder(Bot.getClient()).withChannel(channel)
                            .appendContent("Error!", Styles.BOLD_ITALICS)
                            .appendContent(" ")
                            .appendContent("No package matching the selector", Styles.ITALICS)
                            .appendContent(" ")
                            .appendContent(arguments[0], Styles.INLINE_CODE)
                            .appendContent(" ")
                            .appendContent("found!", Styles.ITALICS)
            );
            return Type.SUCCESSFULLY_INVOKED;
        }

        if (possibleMatches.size() > 1) {
            List<Package> exactMatches = possibleMatches.stream()
                    .filter(aPackage -> aPackage.getName().toLowerCase().equals(packageName))
                    .collect(Collectors.toList());

            if (exactMatches.size() == 1) {
                handlePackage(exactMatches.get(0), channel);
                return Type.SUCCESSFULLY_INVOKED;
            }
            else {
                String joined = possibleMatches
                        .stream()
                        .map(Package::getName)
                        .collect(Collectors.joining("\n"));

                MessageBuilder builder = new MessageBuilder(Bot.getClient()).withChannel(channel)
                        .appendContent("Multiple packages found for selector", Styles.ITALICS)
                        .appendContent(" ")
                        .appendContent(arguments[0], Styles.INLINE_CODE)
                        .appendContent(" ")
                        .appendContent(":", Styles.ITALICS)
                        .appendCode("", joined);

                MessageUtil.sendSelfDestructingMessage(
                        MessageUtil.sendMessage(builder)
                        , TimeUnit.SECONDS, 5);
                return Type.SUCCESSFULLY_INVOKED;
            }
        }

        handlePackage(possibleMatches.get(0), channel);

        return Type.SUCCESSFULLY_INVOKED;
    }

    private void handlePackage(Package aPackage, IChannel channel) {
        Element description = aPackage.getDescription();

        String parsed = "*None*";
        if (description != null) {
            MapperCollection mapperCollection = new MapperCollection();
            for (StandardMappers standardMappers : StandardMappers.values()) {
                mapperCollection.addMapper(standardMappers);
            }

            HtmlConverter converter = new HtmlConverter(description.html(), mapperCollection);
            parsed = converter.parse(aPackage.getDetailUrl());
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(new Color(NumberUtil.getRandomInt()))
                .withAuthorName(aPackage.getName())
                .withAuthorUrl(aPackage.getDetailUrl())
                .withThumbnail("https://www.jetbrains.com/help/img/idea/2016.3/iconPackage.png")
                .withDesc(StringUtil.trimToSize(parsed, 2048));

        MessageBuilder messageBuilder = new MessageBuilder(Bot.getClient()).withChannel(channel)
                .withEmbed(embedBuilder.build());

        MessageUtil.sendMessage(
                messageBuilder
        );
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "package";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "package <name>"
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
        return "Describes a package";
    }
}
