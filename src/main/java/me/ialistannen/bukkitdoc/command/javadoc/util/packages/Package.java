package me.ialistannen.bukkitdoc.command.javadoc.util.packages;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A package
 */
public class Package {

    private DescriptionParser parser;
    private Element description;
    private String detailUrl;
    private String name;

    /**
     * @param detailUrl The detail URL
     * @param name The name
     */
    public Package(String detailUrl, String name) {
        this.detailUrl = detailUrl;
        this.name = name;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The Url to this packages detail page
     */
    public String getDetailUrl() {
        return detailUrl;
    }

    /**
     * @return The Description element
     */
    public Element getDescription() {
        if (description == null) {
            if (parser == null) {
                parser = new DescriptionParser(detailUrl);
            }
            parser.parse();
            description = parser.getDescription();
        }
        return description;
    }

    @Override
    public String toString() {
        return "Package{" +
                "detailUrl='" + detailUrl + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    private static class DescriptionParser {
        private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionParser.class);

        private Element description;
        private String detailURL;
        private Document document;

        private DescriptionParser(String detailURL) {
            this.detailURL = detailURL;
        }

        private void parse() {
            try {
                document = Jsoup.connect(detailURL).get();
                
                List<Element> descriptionTags = document.getElementsByTag("a").stream()
                        .filter(element -> {
                            if (!element.hasAttr("name")) {
                                return false;
                            }
                            switch (element.attr("name")) {
                                case "package.description":
                                case "package_description": {
                                    return true;
                                }
                                default: {
                                    return false;
                                }
                            }
                        })
                        .collect(Collectors.toList());

                if (descriptionTags.size() > 1) {
                    LOGGER.warn("More than one element found!", descriptionTags);
                    return;
                }
                if (descriptionTags.isEmpty()) {
                    description = null;
                    return;
                }

                Element contentContainer = descriptionTags.get(0).parent();

                contentContainer = contentContainer.clone();

                // Remove content container
                for (Element child : contentContainer.children()) {
                    if (child.tagName().equalsIgnoreCase("ul")
                            || child.tagName().equalsIgnoreCase("h2")) {
                        child.remove();
                    }
                }

                description = contentContainer;
            } catch (IOException e) {
                LOGGER.warn("Error connecting with JSoup", e);
            }
        }

        /**
         * @return The Element or null if not found
         */
        private Element getDescription() {
            return description;
        }
    }
}
