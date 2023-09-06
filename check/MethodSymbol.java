/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import java.util.ArrayList;

import syntax.*;

/**
 * Method symbol
 */
public final class MethodSymbol extends BaseSymbol {
    // Method parent class
    public ClassSymbol parent;
    // Method return type
    public Type type;
    // Method formal arguments
    public ArrayList<VarSymbol> formals = new ArrayList<>();
    // Method local variables
    public ArrayList<VarSymbol> locals = new ArrayList<>();

    public MethodSymbol(final String name, final Type type) {
        super(name);
        this.type = type;
    }

    /**
     * Get formal parameter
     * 
     * @param name Formal name
     */
    public VarSymbol getFormal(final String name) {
        for (final VarSymbol vs : formals) {
            if (vs.name.equals(name)) {
                return vs;
            }
        }

        return null;
    }

    /**
     * Get local variable
     * 
     * @param name Local name
     */
    public VarSymbol getLocal(final String name) {
        for (final VarSymbol vs : locals) {
            if (vs.name.equals(name)) {
                return vs;
            }
        }

        return null;
    }

    /**
     * Get formal parameter
     * 
     * @param index Formal index (into argument list)
     */
    public VarSymbol getFormal(final int index) {
        return formals.get(index);
    }

    /**
     * Get local variable
     * 
     * @param index Local index (into variable list)
     */
    public VarSymbol getLocal(final int index) {
        return locals.get(index);
    }
}
