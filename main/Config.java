/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

import java.util.Map;
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

    // Whether to run all test cases
    private static boolean s_test = false;
    // Whether to dump compiler phase info
    private static boolean s_verbose = false;
    // Target architecture (default SPARC)
    private static ArchType s_arch = ArchType.Sparc;

    /**
     * Load configuration from system properties
     */
    public static void initialize() {
        /**
         * Debug configuration
         */
        s_test = isPropertyDefined("test");
        s_verbose = isPropertyDefined("verbose");

        /**
         * Target architecture
         */
        if (isPropertyDefined("arch")) {
            final Map<String, ArchType> name2arch = Map.of(
                    "sparc", ArchType.Sparc,
                    "powerpc", ArchType.PowerPC);

            final String name = System.getProperty("arch").toLowerCase();

            if (name2arch.containsKey(name)) {
                Logger.logVerboseLn("Config error: Invalid architecture %s", name);
            } else {
                s_arch = name2arch.get(name);
            }
        }
    }

    /**
     * Whether to run compiler test cases
     */
    public static boolean isTest() {
        return s_test;
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
    public static ArchType getTargetArch() {
        return s_arch;
    }

    /**
     * Check if a system property with the given name is defined
     * 
     * @param name Property name
     */
    private static boolean isPropertyDefined(final String name) {
        try {
            final String value = System.getProperty(name);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }
}
