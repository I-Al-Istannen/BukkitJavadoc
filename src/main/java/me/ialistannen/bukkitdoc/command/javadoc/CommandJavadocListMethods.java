package me.ialistannen.bukkitdoc.command.javadoc;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocClass;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocMethod;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.NumberUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import me.ialistannen.htmltodiscord.MapperCollection;
import me.ialistannen.htmltodiscord.StandardMappers;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;


/**
 * Lists methods
 */
class CommandJavadocListMethods extends CommandJavadoc {

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

        String className = arguments[0];
        if (arguments[0].contains("#")) {
            className = className.substring(0, className.indexOf("#"));
        }

        String baseUrl = JavadocConstants.INSTANCE.getBaseUrl();
        //        String baseUrl = "https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/";
        //        String baseUrl = "http://home.dv8tion.net:8080/job/JDA/98/javadoc/";
        baseUrl += "allclasses-noframe.html";

        Optional<JavadocClass> classOptional = getClass(baseUrl, channel, message, className);

        if (!classOptional.isPresent()) {
            return Type.SUCCESSFULLY_INVOKED;
        }

        JavadocClass javadocClass = classOptional.get();

        Predicate<JavadocMethod> filter = javadocMethod -> true;
        if (arguments.length > 1) {
            filter = javadocMethod -> javadocMethod.getName().contains(arguments[1]);
        }

        MapperCollection collection = new MapperCollection();
        for (StandardMappers mappers : StandardMappers.values()) {
            collection.addMapper(mappers);
        }

        StringBuilder builder = new StringBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(new Color(NumberUtil.getRandomInt()));

        {
            for (JavadocMethod method : javadocClass.getMethods()) {
                if (!filter.test(method)) {
                    continue;
                }

                if (builder.length() > 1000) {
                    String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
                    embedBuilder.appendField("Methods", content, false);
                    builder = new StringBuilder();
                }

                builder.append(method.getName())
                        .append(" [")
                        .append(joinParamNames(method.getParameterTypes()))
                        .append("]");
                builder.append("\n");
            }
            String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
            embedBuilder.appendField("Methods", content, true);
        }

        MessageUtil.sendSelfDestructingMessage(
                MessageUtil.sendMessage(
                        new MessageBuilder(Bot.getClient()).withChannel(channel)
                                .withEmbed(embedBuilder.build())
                ),
                TimeUnit.SECONDS, 20
        );

        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Joins the method parameters in a single String
     *
     * @param parameters The parameters
     *
     * @return The names for the parameter
     */
    private String joinParamNames(List<String> parameters) {
        if (parameters.isEmpty()) {
            return "Void";
        }
        return parameters
                .stream()
                .map(s -> {
                    if (!s.contains(".")) {
                        return s;
                    }
                    return s.substring(s.lastIndexOf('.') + 1);
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "listMethods";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "javadoc listMethods <class name> [filter]";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Lists some methods of a class";
    }
}
