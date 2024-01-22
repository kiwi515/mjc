/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

import java.lang.System;

/**
 * Compiler configuration
 */
public final class Config {
    /**
     * Architecture
     */
    public enum ArchType {
        Sparc,
        PowerPC
    }

    /**
     * Garbage collection method
     */
    public enum GCType {
        None,
        Refcount,
        MarkSweep,
        Copying,
        Generational,
    }

    /**
     * Optimization level
     */
    public enum OptLevel {
        O0,
        O1,
        O2
    }

    // Whether to dump compiler phase info
    private static boolean s_verbose = false;

    // Target architecture (SPARC by default)
    private static ArchType s_arch = ArchType.Sparc;

    // Target GC method (OFF by default)
    private static GCType s_gcType = GCType.None;

    // Target optimization level (O0/no opt by default)
    private static OptLevel s_optLevel = OptLevel.O0;

    // Path to source file
    private static String s_srcFile = "";

    /**
     * Apply configuration from input arguments
     * 
     * @param args Runtime arguments
     * @return Whether parsing was successful
     */
    public static boolean initialize(final String[] args) {
        // Sanity check
        if (args == null) {
            return false;
        }

        // Process arguments
        for (final String arg : args) {
            // Cannot specify options after source file name
            if (!s_srcFile.isEmpty()) {
                return false;
            }

            final String[] tokens = arg.split("=");

            try {
                switch (tokens[0]) {
                    case "--help":
                        // Force show usage
                        return false;

                    case "--verbose":
                        s_verbose = true;
                        break;

                    case "--gc":
                        s_gcType = str2enum(GCType.class, tokens[1]);
                        break;

                    case "--opt":
                        s_optLevel = str2enum(OptLevel.class, tokens[1]);
                        break;

                    // Assume this is specifying the source file
                    default:
                        s_srcFile = tokens[0];
                        break;
                }
            }
            // Some option was not given enough arguments
            catch (final IndexOutOfBoundsException e) {
                return false;
            }
        }

        // At the very least, a source file must be specified.
        return !s_srcFile.isEmpty();
    }

    /**
     * Explain options available to the user
     */
    public static void showHelp() {
        final String str = String.join("\n",
                "Usage: {compiler-jar} [option(s)] input-file",
                "Options:",

                String.format("%-20s%s", "--help",
                        "Display this information again."),

                String.format("%-20s%s", "--verbose",
                        "Log verbose compiler information to \"/verbose.txt\"."),

                String.format("%-20s%s", "--gc=<type>",
                        "Set <type> as the garbage collection method in the main function."),
                String.format("%-20s%s", "",
                        enum2options(GCType.class)),

                String.format("%-20s%s", "--opt=<level>",
                        "Set <level> as the program optimization level."),
                String.format("%-20s%s", "",
                        enum2options(OptLevel.class)));

        System.out.println(str);
    }

    /**
     * Whether to dump compiler phase info
     */
    public static boolean isVerbose() {
        return s_verbose;
    }

    /**
     * Get compiler target architecture
     */
    public static ArchType getArchType() {
        return s_arch;
    }

    /**
     * Get runtime GC method
     */
    public static GCType getGcType() {
        return s_gcType;
    }

    /**
     * Get optimization level
     */
    public static OptLevel getOptLevel() {
        return s_optLevel;
    }

    /**
     * Get source code file path
     */
    public static String getSrcFilePath() {
        return s_srcFile;
    }

    /**
     * Attempt to convert string to an enum value (case *insensitive*).
     * If key is invalid, the first enum value is chosen.
     * 
     * @param <T> Enum type
     * @param e   Enum class
     * @param x   String key
     * @return Enum value
     */
    public static <T extends Enum<T>> T str2enum(final Class<T> e, final String x) {
        // Compare against all enum values (case insensitive)
        for (final T value : e.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(x)) {
                return value;
            }
        }

        // Default to first enum value
        final T fallback = e.getEnumConstants()[0];

        Logger.logVerboseLn("Config error: \"%s\" cannot be specified as %s. Defaulting to \"%s\"...",
                x, e.getSimpleName(), fallback.name());

        return fallback;
    }

    /**
     * Creates a string for the help menu which shows all possible enum options.
     * 
     * @param <T> Enum type
     * @param e   Enum class
     * @return "|" delimted string of enum values
     */
    private static <T extends Enum<T>> String enum2options(final Class<T> e) {
        String options = "";

        // Concatenate options just|like|this
        for (final T value : e.getEnumConstants()) {
            options = options.isEmpty() ? value.name()
                    : String.join("|", options, value.name());
        }

        // Enclose in parenthesis
        return String.format("(%s)", options);
    }
}
