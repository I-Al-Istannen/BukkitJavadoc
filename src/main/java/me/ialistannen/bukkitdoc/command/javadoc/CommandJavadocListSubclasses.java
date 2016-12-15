package me.ialistannen.bukkitdoc.command.javadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocClass;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import me.ialistannen.htmltodiscord.util.StringUtils;
import me.ialistannen.htmltodiscord.util.TableCreator;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;


/**
 * Lists subclasses
 */
class CommandJavadocListSubclasses extends CommandJavadoc {

    /**
     * Executes the command
     *
     * @param channel   The channel the message was sent in
     * @param message   The message that was sent
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

        int width = 75;

        TableCreator creator = new TableCreator(() -> " | ", width);

        List<JavadocClass> subclasses = javadocClass.getSubclasses();

        int max = subclasses
                .stream()
                .mapToInt(value -> value.getName().length()).max()
                .orElse(1);

        int columnAmount = 1;

        while ((columnAmount + 1) * " | ".length() + max * columnAmount < width) {
            columnAmount++;
        }
        if (columnAmount > 1) {
            columnAmount--;
        }

        outer:
        for (int i = 0; i < subclasses.size(); i++) {
            List<TableCreator.Column> columns = new ArrayList<>(columnAmount);
            for (int j = 0; j < columnAmount; j++) {
                int index = j + i;
                if (index >= subclasses.size()) {
                    creator.addLine(length -> StringUtils.repeat("-", length), columns);
                    break outer;
                }
                columns.add(() -> subclasses.get(index).getName());
            }
            i += columnAmount - 1;
            creator.addLine(length -> StringUtils.repeat("-", length), columns);
        }

        String table;
        if (!subclasses.isEmpty()) {
            table = StringUtil.trimToSize(creator.build().print(), 1900);
        }
        else {
            table = "None";
        }

        MessageBuilder finalMessage = new MessageBuilder(Bot.getClient()).withChannel(channel)
                .appendContent("All known subclasses:", Styles.BOLD_ITALICS)
                .appendQuote(table);

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
