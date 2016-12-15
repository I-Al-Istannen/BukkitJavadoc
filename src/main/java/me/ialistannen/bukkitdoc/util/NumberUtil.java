package me.ialistannen.bukkitdoc.util;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.Random;

/**
 * utility methods for numbers
 */
public class NumberUtil {

    private static final Random RANDOM = new Random();

    /**
     * Parses a String to an Integer
     *
     * @param input The String to parse
     *
     * @return The parsed int, or null if not possible
     */
    public static OptionalInt parseInt(String input) {
        Objects.requireNonNull(input, "input can not be null!");

        try {
            return OptionalInt.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Returns a random integer
     *
     * @return A random integer
     */
    public static int getRandomInt() {
        return RANDOM.nextInt();
    }
}
