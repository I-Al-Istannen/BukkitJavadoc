package me.ialistannen.bukkitdoc.command.javadoc.util.packages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Parses Packages
 */
public class PackageParser {

    private String baseJavadocUrl;

    /**
     * @param baseJavadocUrl The base Javadoc url
     */
    public PackageParser(String baseJavadocUrl) {
        this.baseJavadocUrl = baseJavadocUrl;
    }

    public Collection<Package> parse() {
        String url = baseJavadocUrl + "overview-summary.html";

        List<Package> packages = new LinkedList<>();

        try {
            Document document = Jsoup.connect(url).get();

            List<Element> rows = new LinkedList<>();
            for (Element element : document.select("html body div.contentContainer table.overviewSummary")) {
                rows.addAll(element.getElementsByTag("tr"));
            }

            for (Element row : rows) {
                if (row.children().size() < 2) {
                    continue;
                }
                // first column
                Element columnOne = row.children().get(0);

                columnOne.getElementsByTag("a").stream()
                        .filter(element -> element.hasAttr("href"))
                        .limit(1)
                        .forEach(element -> {
                            Package aPackage = new Package(element.absUrl("href"), element.text());
                            packages.add(aPackage);
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packages;
    }
}
