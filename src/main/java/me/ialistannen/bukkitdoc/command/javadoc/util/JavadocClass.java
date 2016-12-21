package me.ialistannen.bukkitdoc.command.javadoc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * A Javadoc class
 */
public class JavadocClass {

    private String name, packageName;
    private String targetUrl;
    private Element description;
    private Element declaration;
    private Document wholeHtml;

    private List<JavadocMethod> methods;
    private List<JavadocClass> subClasses;

    private ClassDescriptionParser classDescriptionParser;

    /**
     * @param name        The name
     * @param targetUrl   The target url
     * @param packageName The name of the package
     */
    JavadocClass(String name, String targetUrl, String packageName) {
        this.name = name;
        this.targetUrl = targetUrl;
        this.packageName = packageName;
    }

    /**
     * @return The URL for the class
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * @return The name HTML of the Class
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name of the package the class is in
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return True if the class is abstract
     */
    public boolean isAbstract() {
        return getDeclaration().text().contains("abstract");
    }

    /**
     * @return True if the class is an interface
     */
    public boolean isInterface() {
        return getDeclaration().text().contains("interface");
    }

    /**
     * @return True if the class is an enum
     */
    public boolean isEnum() {
        return getDeclaration().text().contains("enum");
    }

    /**
     * @return True if the class is final
     */
    public boolean isFinal() {
        return getDeclaration().text().contains("final");
    }

    // @formatter:off
    /**
     * Returns the type of the class.
     * 
     *     <ul>
     *         <li>"enum"</li>
     *         <li>"interface"</li>
     *         <li>"class"</li>
     *     </ul>
     * 
     * @return The type of the class
     */
    // @formatter:on
    public String getClassTypeName() {
        if (isEnum()) {
            return "enum";
        }
        else if (isInterface()) {
            return "interface";
        }

        return "class";
    }

    /**
     * @return The description of the class
     */
    public synchronized Element getDescription() {
        if (description == null) {
            if (classDescriptionParser == null) {
                classDescriptionParser = new ClassDescriptionParser(getTargetUrl());
                classDescriptionParser.parse();
            }
            description = classDescriptionParser.getDescription();
        }
        return description;
    }

    /**
     * @return The description of the class
     */
    public synchronized Element getDeclaration() {
        if (declaration == null) {
            if (classDescriptionParser == null) {
                classDescriptionParser = new ClassDescriptionParser(getTargetUrl());
                classDescriptionParser.parse();
            }
            declaration = classDescriptionParser.getDeclaration();
        }
        return declaration;
    }

    /**
     * @return All methods
     */
    public List<JavadocMethod> getMethods() {
        if (methods == null) {
            MethodParser parser = new MethodParser(this);
            methods = new ArrayList<>(parser.getMethodList());
        }
        return Collections.unmodifiableList(methods);
    }

    /**
     * @return The subclasses of the class
     */
    public List<JavadocClass> getSubclasses() {
        if (subClasses == null) {
            if (classDescriptionParser == null) {
                classDescriptionParser = new ClassDescriptionParser(getTargetUrl());
                classDescriptionParser.parse();
            }
            subClasses = classDescriptionParser.getSubClasses().orElse(new ArrayList<>());
        }
        return Collections.unmodifiableList(subClasses);
    }

    /**
     * @return The whole HTML of the class
     */
    Document getWholeHtml() {
        if (wholeHtml == null) {
            if (classDescriptionParser == null) {
                classDescriptionParser = new ClassDescriptionParser(getTargetUrl());
                classDescriptionParser.parse();
            }
            wholeHtml = classDescriptionParser.getWholeHtml();
        }
        return wholeHtml;
    }

    @Override
    public String toString() {
        return "JavadocClass{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                '}';
    }

    /**
     * Parses the description about a class
     */
    private static class ClassDescriptionParser {
        private String targetUrl;

        private Element description, declaration;
        private List<Element> subClassesElements;
        private Document wholeHtml;

        private List<JavadocClass> subClasses;

        private ClassDescriptionParser(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        /**
         * Parses the page to find the correct description and declaration
         */
        private void parse() {
            try {
                wholeHtml = Jsoup.connect(targetUrl).get();
                description = wholeHtml
                        .select("body > .contentContainer > .description > .blockList > .blockList > .block")
                        .first();

                declaration = wholeHtml
                        .select("body > .contentContainer > .description > .blockList > .blockList")
                        .stream()
                        .map(Element::children)
                        .flatMap(Collection::stream)
                        .filter(element -> element.tagName().equals("pre"))
                        .findAny()
                        .orElse(null);

                subClassesElements = wholeHtml
                        .select(".description > ul:nth-child(1) > li:nth-child(1)")
                        .stream()
                        .flatMap(element -> element.getElementsByTag("dl").stream())
                        .filter(element -> {
                            String text = element.text().toLowerCase();
                            return text.contains("direct known subclasses")
                                    || text.contains("all known implementing classes")
                                    || text.contains("all known subinterfaces")
                                    //|| text.contains("all superinterfaces")
                                    ;
                        })
                        .flatMap(element -> element.getAllElements()
                                .stream()
                                .filter(element1 -> element1.tagName().equalsIgnoreCase("dd")))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void parseSubClasses() {
            if (subClassesElements == null || subClassesElements.isEmpty()) {
                subClasses = null;
                return;
            }

            subClasses = new ArrayList<>();

            for (Element element : subClassesElements) {
                for (Element a : element.getElementsByTag("a")) {
                    if (!a.hasAttr("href")) {
                        continue;
                    }
                    String absUrl = a.absUrl("href");
                    String packageName = absUrl.replace("/", ".");
                    packageName = packageName.replaceAll("[^.a-zA-Z0-9]", "");

                    subClasses.add(new JavadocClass(a.text(), absUrl, packageName));
                }
            }
        }

        /**
         * @return The Declaration
         */
        Element getDeclaration() {
            return declaration;
        }

        private Optional<List<JavadocClass>> getSubClasses() {
            if (subClasses == null) {
                parseSubClasses();
            }
            return Optional.ofNullable(subClasses);
        }

        /**
         * @return The Description
         */
        private Element getDescription() {
            return description;
        }

        /**
         * @return The whole document
         */
        private Document getWholeHtml() {
            return wholeHtml;
        }
    }
}
