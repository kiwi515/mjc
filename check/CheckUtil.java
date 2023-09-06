/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import syntax.*;

/**
 * Check phase utilities
 */
public final class CheckUtil {
    /**
     * Check if type is primitive
     */
    public static boolean typeIsPrim(final Type t) {
        if (t == Type.THE_BOOLEAN_TYPE || t == Type.THE_INTEGER_TYPE || t == Type.THE_INT_ARRAY_TYPE
                || t == Type.THE_VOID_TYPE) {
            return true;
        }

        return !(t instanceof IdentifierType);
    }

    /**
     * Check for type equality
     */
    public static boolean typeEquals(final Type t1, final Type t2) {
        // Primitive type equality is checked by comparing the pointers
        if (typeIsPrim(t1) && typeIsPrim(t2)) {
            return t1 == t2;
        }

        // User type equality is checked by comparing the names
        return t1.getName().equals(t2.getName());
    }

    /**
     * Check if an upcast is possible between two types
     * 
     * @param from  Source type (to be casted from)
     * @param to    Destination type (to be casted to)
     * @param table Symbol table (contains class hierarchy)
     */
    public static boolean typeCanUpCast(final Type from, final Type to, final SymbolTable table) {
        // This only occurs if another semantic error occurred earlier.
        // We ignore this to silently fail and catch more errors
        if (from == Type.THE_VOID_TYPE || to == Type.THE_VOID_TYPE) {
            return true;
        }

        // Types are equal
        if (typeEquals(from, to)) {
            return true;
        }

        // Primitive types do not involve inheritance
        if (typeIsPrim(from) || typeIsPrim(to)) {
            return false;
        }

        // Get class symbols from types
        final ClassSymbol fromSym = table.getClass(from.getName());
        final ClassSymbol toSym = table.getClass(to.getName());
        assert fromSym != null && toSym != null;

        // Upcast is possible if casting from derived class to base class
        return fromSym.isDerivedFrom(toSym);
    }
}
