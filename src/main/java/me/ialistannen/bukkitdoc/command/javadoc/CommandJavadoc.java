package me.ialistannen.bukkitdoc.command.javadoc;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.ialistannen.bukkitdoc.Bot;
import me.ialistannen.bukkitdoc.command.Command;
import me.ialistannen.bukkitdoc.command.CommandExecuteResult.Type;
import me.ialistannen.bukkitdoc.command.CommandExecutor;
import me.ialistannen.bukkitdoc.command.javadoc.util.ClassListParser;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocClass;
import me.ialistannen.bukkitdoc.command.javadoc.util.JavadocMethod;
import me.ialistannen.bukkitdoc.util.MessageUtil;
import me.ialistannen.bukkitdoc.util.NumberUtil;
import me.ialistannen.bukkitdoc.util.StringUtil;
import me.ialistannen.htmltodiscord.HtmlConverter;
import me.ialistannen.htmltodiscord.MapperCollection;
import me.ialistannen.htmltodiscord.StandardMappers;
import me.ialistannen.htmltodiscord.util.StringUtils;
import me.ialistannen.htmltodiscord.util.TableCreator;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

import static sx.blah.discord.util.MessageBuilder.Styles.INLINE_CODE;
import static sx.blah.discord.util.MessageBuilder.Styles.ITALICS;


/**
 * Resolves a Javadoc class and method
 */
public class CommandJavadoc extends Command {

    {
        // Ugly. So Ugly.
        if (getClass() == CommandJavadoc.class) {
            addChild(new CommandJavadocListMethods());
            addChild(new CommandJavadocListSubclasses());
            addChild(new CommandJavadocSetBaseUrl());
            addChild(new CommandJavadocPackage());
        }
    }

    /**
     * Executes the command
     *
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param arguments The arguments after the command name
     */
    @SuppressWarnings("Duplicates")
    @Override
    public Type execute(IChannel channel, IMessage message, String[] arguments) {
        if (arguments.length < 1) {
            return Type.SEND_USAGE;
        }
        String classAndMethodName = StringUtil.join(" ", arguments);

        String className = classAndMethodName;
        if (classAndMethodName.contains("#")) {
            className = className.substring(0, className.indexOf("#"));
        }

        String baseUrl = JavadocConstants.INSTANCE.getBaseUrl();
        //        String baseUrl = "https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/";
        //        String baseUrl = "http://home.dv8tion.net:8080/job/JDA/98/javadoc/";
        baseUrl += "allclasses-noframe.html";

        JavadocClass javadocClass;

        {
            Optional<JavadocClass> classOptional = getClass(baseUrl, channel, message, className);
            if (!classOptional.isPresent()) {
                return Type.SUCCESSFULLY_INVOKED;
            }
            javadocClass = classOptional.get();
        }

        MapperCollection collection = new MapperCollection();
        for (StandardMappers mappers : StandardMappers.values()) {
            collection.addMapper(mappers);
        }

        if (!classAndMethodName.contains("#")) {
            HtmlConverter converter = new HtmlConverter(javadocClass.getDeclaration().html(), collection);
            String declaration = converter.parse(javadocClass.getTargetUrl());

            String description = "*None*";
            if (javadocClass.getDescription() != null) {
                converter = new HtmlConverter(javadocClass.getDescription().html(), collection);
                description = converter.parse(javadocClass.getTargetUrl());
            }

            String nameWithType = declaration.contains("extends")
                                  ? declaration.substring(0, declaration.indexOf("extends"))
                                  : declaration;

            // Break down Annotations
            String descriptionPrefix = "";
            System.out.println("Prev: " + nameWithType);
            nameWithType = StringUtil.stripFormatting(nameWithType);
            System.out.println("After: " + nameWithType);
            {
                Pattern pattern = Pattern.compile("((@\\S+?\\s?\\([\\S\\s]+?\\))|(@(?!interface)\\S+))");
                Matcher matcher = pattern.matcher(nameWithType);
                boolean found = false;
                while (matcher.find()) {
                    found = true;
                    descriptionPrefix += matcher.group(1) + "\n";
                    nameWithType = matcher.replaceFirst("");
                    matcher = pattern.matcher(nameWithType);
                }
                if (found) {
                    descriptionPrefix = "```java\n" + descriptionPrefix + "\n```\n";
                }
                descriptionPrefix = descriptionPrefix.replaceAll(" ,", ", ");
                descriptionPrefix = descriptionPrefix.replaceAll("=(\\S)", "= $1");
            }


            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .withColor(getColorForClass(javadocClass));

            embedBuilder.withAuthorName(StringUtil.stripFormatting(nameWithType))
                    .withAuthorIcon(getIconUrlForClass(javadocClass))
                    .withAuthorUrl(javadocClass.getTargetUrl());

            // Add super classes
            if (javadocClass.getDeclaration().text().contains("extends")) {
                String extendContent = javadocClass.getDeclaration().text().replace("\n", " ");
                extendContent = javadocClass.getClassTypeName() + " " + extendContent.substring(extendContent.indexOf
                        ("extends"));
                extendContent = extendContent.trim();

                description = "```java\n"
                        + extendContent
                        + "\n```\n"
                        + description;
            }
            description = descriptionPrefix + description;

            embedBuilder.withDesc(StringUtil.trimToSize(description, 2048));

            MessageUtil.sendMessage(
                    new MessageBuilder(Bot.getClient()).withChannel(channel)
                            .withEmbed(embedBuilder.build())
            );
        }
        else {
            String methodName = classAndMethodName.substring(classAndMethodName.indexOf("#") + 1);
            String paramFilter = methodName.contains("(") ?
                                 methodName.substring(methodName.indexOf("(") + 1, methodName.length() - 1)
                                                          : "";

            if (!paramFilter.isEmpty()) {
                methodName = methodName.substring(0, methodName.indexOf("("));
            }

            final String finalMethodName = methodName.replace("()", "");

            List<JavadocMethod> methods = javadocClass
                    .getMethods()
                    .stream()
                    .filter(javadocMethod -> javadocMethod.getName().equalsIgnoreCase(finalMethodName))
                    .filter(javadocMethod -> {
                        List<String> params = javadocMethod.getParameterTypes()
                                .stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList());

                        if (paramFilter.isEmpty()) {
                            return true;
                        }

                        String[] searchParams = paramFilter.split(",");

                        if (searchParams.length != params.size()) {
                            return false;
                        }
                        for (int i = 0; i < searchParams.length; i++) {
                            String type = searchParams[i];

                            if (!params.get(i).endsWith(type.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .distinct()
                    .collect(Collectors.toList());

            if (methods.size() > 1 &&
                    paramFilter.isEmpty()) {
                List<JavadocMethod> collect = methods.stream()
                        .filter(javadocMethod -> javadocMethod.getParameterTypes().isEmpty())
                        .collect(Collectors.toList());

                if (collect.size() == 1) {
                    methods = collect;
                }
            }

            if (methods.size() > 1) {

                String joined = methods
                        .stream()
                        .map(javadocMethod -> javadocMethod.getName() + " " + javadocMethod.getParameterTypes())
                        .collect(Collectors.joining("\n"));

                MessageBuilder result = new MessageBuilder(Bot.getClient())
                        .withChannel(channel)
                        .appendContent("Multiple methods found for selector", ITALICS)
                        .appendContent(" ")
                        .appendContent(classAndMethodName, INLINE_CODE)
                        .appendContent(" ")
                        .appendContent(":", ITALICS)
                        .appendQuote(joined);

                MessageUtil.sendMessage(
                        result
                );
                return Type.SUCCESSFULLY_INVOKED;
            }

            if (methods.isEmpty()) {
                TableCreator tableCreator = new TableCreator(() -> " | ", 75);

                tableCreator.addLine(length -> StringUtils.repeat("=", length), () -> "Name", () -> "Params", () ->
                        "Class");
                tableCreator.addLine(length -> StringUtils.repeat("-", length), () -> finalMethodName, () ->
                        paramFilter, javadocClass::getName);

                EmbedBuilder builder = new EmbedBuilder()
                        .withColor(new Color(NumberUtil.getRandomInt()))
                        .withDesc("**No matching method found!**")
                        .appendField("Name", methodName, true)
                        .appendField("Parameters", paramFilter, true)
                        .appendField("Class", javadocClass.getName(), true);

                MessageBuilder error = new MessageBuilder(Bot.getClient())
                        .withChannel(channel)
                        .withEmbed(builder.build());


                MessageUtil.sendMessage(error);
                return Type.SUCCESSFULLY_INVOKED;
            }

            JavadocMethod method = methods.get(0);

            Element description = method.getDescription();

            HtmlConverter converter = new HtmlConverter(description.html(), collection);
            String converted = converter.parse(method.getContainingClass().getTargetUrl());

            String name = description.child(0).text();

            // Break down Annotations
            String descriptionPrefix = "";
            {
                Pattern pattern = Pattern.compile("(@\\S+\\n?)");
                Matcher matcher = pattern.matcher(name);
                boolean found = false;
                while (matcher.find()) {
                    found = true;
                    descriptionPrefix += matcher.group(1) + "\n";
                    name = matcher.replaceFirst("");
                    matcher = pattern.matcher(name);
                }
                if (found) {
                    descriptionPrefix = "```java\n" + descriptionPrefix + "\n```\n";
                }
                descriptionPrefix = descriptionPrefix.replaceAll(" ,", ", ");
                descriptionPrefix = descriptionPrefix.replaceAll("=(\\S)", "= $1");
            }

            {
                int endIndex = name.contains("\n") ? name.indexOf("\n") : name.length();
                name = name.substring(0, endIndex);
            }

            converted = converted.substring(converted.indexOf("\n") + 1);
            converted = descriptionPrefix + converted;

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .withAuthorIcon(getIconUrlForMethod(method))
                    .withAuthorName(StringUtil.stripFormatting(name))
                    .withAuthorUrl(method.getLink())
                    .withColor(new Color(NumberUtil.getRandomInt()))
                    .withDesc(StringUtil.trimToSize(converted, 2048));

            MessageUtil.sendMessage(
                    new MessageBuilder(Bot.getClient())
                            .withChannel(channel)
                            .withEmbed(embedBuilder.build())
            );
        }

        return Type.SUCCESSFULLY_INVOKED;
    }

    private String getIconUrlForMethod(JavadocMethod javadocMethod) {
        if (javadocMethod.isAbstract()) {
            return "https://www.jetbrains.com/help/img/idea/2016.3/method_abstract.png";
        }
        return "https://www.jetbrains.com/help/img/idea/2016.3/method.png";
    }

    private String getIconUrlForClass(JavadocClass javadocClass) {
        if (javadocClass.isAbstract()) {
            return "https://www.jetbrains.com/help/img/idea/2016.3/classTypeAbstract.png";
        }
        else if (javadocClass.isInterface()) {
            return "https://www.jetbrains.com/help/img/idea/2016.3/classTypeInterface.png";
        }
        else if (javadocClass.isEnum()) {
            return "https://www.jetbrains.com/help/img/idea/2016.3/classTypeEnum.png";
        }
        else if (javadocClass.isFinal()) {
            return "https://www.jetbrains.com/help/img/idea/2016.3/classTypeFinal.png";
        }
        return "https://www.jetbrains.com/help/img/idea/2016.3/classTypeJavaClass.png";
    }

    private Color getColorForClass(JavadocClass javadocClass) {
        if (javadocClass.isAbstract()) {
            return Color.LIGHT_GRAY;
        }
        else if (javadocClass.isInterface()) {
            return new Color(85, 66, 159);
        }
        else if (javadocClass.isEnum()) {
            return new Color(142, 92, 36);
        }
        else if (javadocClass.isFinal()) {
            return new Color(255, 65, 59);
        }
        return Color.GREEN;
    }

    /**
     * @param url The url to fetch it from
     * @param channel The channel the message was sent in
     * @param message The message that was sent
     * @param className The name of the class
     *
     * @return The found classes, if any
     */
    private Optional<List<JavadocClass>> getClasses(String url, IChannel channel, @SuppressWarnings("unused")
            IMessage message, String className) {
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        ClassListParser parser = new ClassListParser(document);

        List<JavadocClass> classes = parser.find(javadocClass -> javadocClass
                .getPackageName()
                .toLowerCase()
                .endsWith(className.toLowerCase()));

        if (classes.isEmpty()) {
            MessageBuilder error = new MessageBuilder(Bot.getClient()).withChannel(channel)
                    .appendContent("No class with the name", ITALICS)
                    .appendContent(" ")
                    .appendContent(className, INLINE_CODE)
                    .appendContent(" ")
                    .appendContent("found.", ITALICS);

            MessageUtil.sendMessage(error);
            return Optional.empty();
        }

        return Optional.of(classes);
    }

    Optional<JavadocClass> getClass(String url, IChannel channel, IMessage message, String className) {
        Optional<List<JavadocClass>> classes = getClasses(url, channel, message, className);
        if (!classes.isPresent()) {
            return Optional.empty();
        }
        List<JavadocClass> classList = classes.get();
        if (classList.size() > 1) {

            // allow a perfect match to overwrite others
            {
                List<JavadocClass> matchingClasses = classList
                        .stream()
                        .filter(javadocClass -> javadocClass.getName().equalsIgnoreCase(className))
                        .collect(Collectors.toList());

                if (matchingClasses.size() == 1) {
                    return Optional.ofNullable(matchingClasses.get(0));
                }
            }

            String joined = classList
                    .stream()
                    .map(JavadocClass::getPackageName)
                    .collect(Collectors.joining("\n"));

            MessageBuilder result = new MessageBuilder(Bot.getClient()).withChannel(channel)
                    .appendContent("Multiple classes found for selector", ITALICS)
                    .appendContent(" ")
                    .appendContent(className, INLINE_CODE)
                    .appendContent(" ")
                    .appendContent(":", ITALICS)
                    .appendQuote(joined);

            MessageUtil.sendSelfDestructingMessage(
                    MessageUtil.sendMessage(result),
                    TimeUnit.SECONDS, 5
            );
            return Optional.empty();
        }

        return Optional.ofNullable(classList.get(0));
    }

    /**
     * Returns the command keyword
     *
     * @return The keyword for the command
     */
    @Override
    public String getKeyword() {
        return "javadoc";
    }

    /**
     * Returns the command usage
     *
     * @return The usage for the command
     */
    @Override
    public String getUsage() {
        return CommandExecutor.PREFIX + "javadoc <class name>[#method name]"
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
        return "Looks up some javadoc";
    }
}
