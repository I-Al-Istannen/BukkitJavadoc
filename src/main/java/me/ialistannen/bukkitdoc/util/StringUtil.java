package me.ialistannen.bukkitdoc.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Some static String utility functions
 */
public class StringUtil {

    /**
     * Matches Markdown links in the
     */
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.+?)]\\((.+?)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Strips formatting from the string
     *
     * @param string The string to strip formatting from
     *
     * @return A mostly stripped version of it
     */
    public static String stripFormatting(String string) {
        String replaced = string.replaceAll("[]\\[+&|!(){}^\"~*?:\\\\-]", "");

        Matcher matcher = LINK_PATTERN.matcher(replaced);
        while (matcher.find()) {
            replaced = matcher.replaceAll(matcher.group(1));
        }

        return replaced;
    }

    /**
     * Trims a string to a given size
     *
     * @param string    The String to trim
     * @param maxLength The max length
     *
     * @return The trimmed string
     */
    public static String trimToSize(String string, int maxLength) {
        if (string.length() < maxLength) {
            return string;
        }
        return string.substring(0, maxLength - 3) + "...";
    }

    /**
     * Joins the Array to a single String
     *
     * @param delimiter The delimiter to use
     * @param strings   The Strings to use
     *
     * @return The joined String
     */
    public static String join(@SuppressWarnings("SameParameterValue") String delimiter, String... strings) {
        return Arrays.stream(strings).collect(Collectors.joining(delimiter));
    }
}
