package me.ialistannen.bukkitdoc.command.javadoc.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.jsoup.nodes.Element;

/**
 * Parses a class list
 */
public class ClassListParser {

    private Element html;

    /**
     * @param html The HTML of the list
     */
    public ClassListParser(Element html) {
        this.html = html;
    }

    /**
     * Parses the classes
     *
     * @return The parsed classes
     */
    public List<JavadocClass> find(Predicate<JavadocClass> predicate) {
        List<JavadocClass> classes = new LinkedList<>();

        for (Element listItem : html.getElementsByTag("li")) {
            String link = listItem.child(0).absUrl("href");
            String name = listItem.text();
            String packageName = listItem.child(0).attr("href").replace("/", ".");
            packageName = packageName.substring(0, packageName.lastIndexOf("."));

            JavadocClass javadocClass = new JavadocClass(name, link, packageName);

            if (predicate.test(javadocClass)) {
                classes.add(javadocClass);
            }
        }

        return classes;
    }
}
