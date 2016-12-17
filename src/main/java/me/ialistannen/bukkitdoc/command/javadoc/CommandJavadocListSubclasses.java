package me.ialistannen.bukkitdoc.command.javadoc;

import java.awt.Color;
import java.util.Optional;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocClass;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.NumberUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;


/**
 * Lists subclasses
 */
class CommandJavadocListSubclasses extends CommandJavadoc {

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
        String classAndMethodName = arguments[0];

        String className = classAndMethodName;
        if (classAndMethodName.contains("#")) {
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


        StringBuilder builder = new StringBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .withColor(new Color(NumberUtil.getRandomInt()));

        {
            for (JavadocClass subClass : javadocClass.getSubclasses()) {
                if (builder.length() > 1000) {
                    String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
                    embedBuilder.appendField("Subclasses for " + javadocClass.getName(), content, false);
                    builder = new StringBuilder();
                }

                builder.append(subClass.getName())
                        .append("\n");
            }
            String content = "```\n" + StringUtil.trimToSize(builder.toString(), 1010) + "\n```";
            embedBuilder.appendField("Subclasses for " + javadocClass.getName(), content, true);
        }

        MessageBuilder finalMessage = new MessageBuilder(Bot.getClient()).withChannel(channel)
                .withEmbed(embedBuilder.build());

        MessageUtil.sendMessage(finalMessage);

        return Type.SUCCESSFULLY_INVOKED;
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "listSubclasses";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "javadoc listSubclasses <Class name>";
    }

    /**
     * Returns a command description
     *
     * @return The Description for this command
     */
    @Override
    public String getDescription() {
        return "Lists all classes executing the current one";
    }
}
