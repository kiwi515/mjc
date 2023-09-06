/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

import java.util.List;
import java.util.ArrayList;

/**
 * Common utility functions
 */
public final class Util {
    /**
     * Concatenate two lists
     */
    public static <T> List<T> concatList(final List<T> l1, final List<T> l2) {
        final List<T> result = new ArrayList<>();

        if (l1 != null) {
            result.addAll(l1);
        }

        if (l2 != null) {
            result.addAll(l2);
        }

        return result;
    }

    /**
     * Create list from a single element
     */
    public static <T> List<T> singleList(final T elem) {
        final List<T> list = new ArrayList<>();

        if (elem != null) {
            list.add(elem);
        }

        return list;
    }

    /**
     * Create list from elements
     */
    public static <T> List<T> makeList(final T... elems) {
        final List<T> list = new ArrayList<>();

        for (final T elem : elems) {
            if (elem != null) {
                list.add(elem);
            }
        }

        return list;
    }

    /**
     * Concatenate identifiers using the '$' delimiter
     */
    public static String concatNames(final String... args) {
        return String.join("$", args);
    }

    /**
     * Check whether a string is an integral number
     */
    public static boolean isIntegral(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
}
