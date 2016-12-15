package me.ialistannen.bukkitdoc.command.javadoc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * A Javadoc method
 */
public class JavadocMethod {

    private String link;

    private String name;
    private List<String> parameterTypes;

    private JavadocClass containingClass;
    private MethodParser parser;

    private Element description;

    /**
     * @param name            The name of the method
     * @param containingClass The containing class
     * @param link            The link that points to the method
     * @param parameterTypes  The parameter types. May be fully qualified
     */
    JavadocMethod(String name, JavadocClass containingClass, String link, Collection<String> parameterTypes) {
        Objects.requireNonNull(link, "link can not be null!");
        Objects.requireNonNull(name, "name can not be null!");
        Objects.requireNonNull(containingClass, "containingClass can not be null!");

        this.name = name;
        this.link = link;
        this.containingClass = containingClass;
        this.parameterTypes = new ArrayList<>(parameterTypes);
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The class that contains this method
     */
    public JavadocClass getContainingClass() {
        return containingClass;
    }

    /**
     * @return The link to the method
     */
    public String getLink() {
        return link;
    }

    /**
     * @return True if the method is abstract
     */
    public boolean isAbstract() {
        return getContainingClass().isAbstract()
                || getContainingClass().isInterface()
                || (getDescription().children().size() > 1 && getDescription().child(1).text().contains("abstract"));
    }

    /**
     * @return The Parameter types
     */
    public List<String> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }

    private void parse() {
        String cutLink = (link.contains("#") ? link.substring(0, link.indexOf("#")) : link).replace(".html", "");

        int methodRefIndex = link.indexOf("#");

        int endIndex = link.contains("(")
                       ? link.indexOf('(', methodRefIndex)
                       : link.indexOf('-', methodRefIndex);

        if (endIndex < 0) {
            endIndex = link.length();
        }
        
        String methodName = link.substring(
                methodRefIndex + 1,
                endIndex
        );

        // is declared in this class
        if (cutLink.replace("/", ".").endsWith(getContainingClass().getPackageName())) {
            parser = new MethodParser(methodName, containingClass.getWholeHtml(), parameterTypes);
        }
        else {
            String className = cutLink.substring(cutLink.lastIndexOf("/") + 1, cutLink.length());
            parser = new MethodParser(methodName, new JavadocClass(className, cutLink + ".html", className)
                    .getWholeHtml(), parameterTypes);
        }

        parser.parse();
    }

    /**
     * @return The method description
     */
    public synchronized Element getDescription() {
        if (description == null) {
            if (parser == null) {
                parse();
            }
            description = parser.getDescription();
        }
        return description;
    }

    @Override
    public String toString() {
        return "JavadocMethod{" +
                "name='" + name + '\'' +
                ", containingClass=" + containingClass.getName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavadocMethod)) {
            return false;
        }
        JavadocMethod that = (JavadocMethod) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }

    private static class MethodParser {
        private Element description;
        private String name;
        private Document classHtml;
        private List<String> parameterTypes;

        /**
         * @param name           The name of the method
         * @param classHtml      The HTML of the class where it is declared in some way
         * @param parameterTypes The parameter of the method
         */
        private MethodParser(String name, Document classHtml, List<String> parameterTypes) {
            this.name = name;
            this.classHtml = classHtml;
            this.parameterTypes = parameterTypes;
        }

        private void parse() {
            classHtml.select(".details > ul:nth-child(1) > li:nth-child(1) > ul > li")
                    .stream()
                    .map(element -> element.getElementsByTag("li"))
                    .flatMap(Collection::stream)
                    .filter(element -> element.children().size() > 0)
                    .filter(element -> element.child(0).tagName().equals("h4") && element.child(0).text().equals(name))
                    .filter(element -> {
                        if (parameterTypes.isEmpty()) {
                            return true;
                        }

                        String text = element.text().toLowerCase();

                        for (String parameterType : parameterTypes) {
                            String unqualifiedName = parameterType.contains(".")
                                                     ? parameterType.substring(parameterType.lastIndexOf('.') + 1)
                                                     : parameterType;
                            unqualifiedName = unqualifiedName.toLowerCase();

                            Matcher matcher = Pattern.compile("\\(([\\s\\S]+?)\\)").matcher(text);
                            if (!matcher.find()) {
                                return false;
                            }
                            String paramParenthesis = matcher.group(1).replace("\n", "").replaceAll("\\s+", " ");
                            if (!Pattern.compile("^" + unqualifiedName + "|,.?" + unqualifiedName,
                                    Pattern.CASE_INSENSITIVE)
                                    .matcher(paramParenthesis)
                                    .find()
                                    && !Pattern.compile("^" + parameterType + "|,.?" + parameterType,
                                    Pattern.CASE_INSENSITIVE)
                                    .matcher(paramParenthesis)
                                    .find()) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .limit(1)
                    .forEach(element -> {
                        description = element.clone();
                        description.getElementsByTag("h4").get(0).remove();
                    });
        }

        /**
         * @return The description of the method
         */
        private Element getDescription() {
            return description;
        }
    }
}
