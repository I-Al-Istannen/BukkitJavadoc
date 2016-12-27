package me.ialistannen.bukkitdoc.command.javadoc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import me.ialistannen.bukkitdoc.util.StringUtil;

/**
 * Parses all methods of a class
 */
class MethodParser {
    private JavadocClass javadocClass;

    private List<JavadocMethod> methodList;

    /**
     * @param javadocClass The owning class
     */
    MethodParser(JavadocClass javadocClass) {
        this.javadocClass = javadocClass;
    }

    /**
     * Parses the class to get the methods
     */
    private void parse() {
        Document wholeHtml = javadocClass.getWholeHtml();

        Elements methods = wholeHtml.select("body > .contentContainer > .summary > .blockList > .blockList > " +
                ".blockList");
        methodList = methods
                .stream()
                .map(Element::getAllElements)
                .flatMap(Collection::stream)
                .filter(element -> element.tagName().equals("a") && element.hasAttr("href"))
                .filter(element -> element.absUrl("href").contains("#"))
//                  .peek(element -> {
//                      if (element.text().contains("getTargetBlock")) {
//                          System.out.println(element + " '" + element.text() + "'");
//                      }
//                  })
                .map(element -> {
                    List<String> parameterTypes = new ArrayList<>();

                    String descriptionString = element.attr("href").replace("\n", "");
                    descriptionString = StringUtil.stripFormatting(descriptionString)
                            .replace(":A", "[]");

                    Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(descriptionString);
                    if (matcher.find()) {
                        parameterTypes = extractParamTypes(matcher.group(1));
                    }
                    else {
                        matcher = Pattern.compile("-(.*)-").matcher(descriptionString);
                        if (matcher.find()) {
                            parameterTypes = extractParamTypes(matcher.group(1).replace("-", ","));
                        }
                    }
                    return new JavadocMethod(element.text(), javadocClass, element.absUrl("href"), parameterTypes);
                })
                .collect(Collectors.toList());
    }

    /**
     * Extracts the Parameter types from the Named param string
     *
     * @param parameters The whole parameter String (e.g. "CommandSender sender" or "String one, String two")
     */
    private List<String> extractParamTypes(String parameters) {
        List<String> params = new ArrayList<>();
        if (!parameters.contains(",")) {
            String type = getTypeFromNamedParam(parameters.trim());
            if (!type.isEmpty()) {
                params.add(type);
            }
        }
        else {
            for (String typeRaw : parameters.trim().split(",")) {
                String type = typeRaw.trim();
                type = getTypeFromNamedParam(type);

                if (!type.isEmpty()) {
                    params.add(type);
                }
            }
        }

        return params;
    }

    /**
     * Strips the Type from the parameter name
     *
     * @param namedParam The named parameter (e.g. "String commandLine")
     *
     * @return The type (e.g. "String")
     */
    private String getTypeFromNamedParam(String namedParam) {
        // non breaking space...
        return Parser.unescapeEntities(namedParam.split("(\\s|[\u00a0])+")[0], true)
                .replace("%20", " ")
                .trim();
    }

    /**
     * @return The method list
     */
    List<JavadocMethod> getMethodList() {
        if (methodList == null) {
            parse();
        }
        return methodList;
    }
}
