/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import java.util.ArrayList;
import java.util.HashMap;

import main.Arch;
import main.Logger;

/**
 * Class symbol
 */
public final class ClassSymbol extends BaseSymbol {
    // Base class
    public ClassSymbol base;
    // Class fields
    public ArrayList<VarSymbol> fields = new ArrayList<>();
    // Class methods
    public HashMap<String, MethodSymbol> methods = new HashMap<>();

    public ClassSymbol(final String name, final ClassSymbol base) {
        super(name);
        this.base = base;
    }

    public ClassSymbol(final String name) {
        this(name, null);
    }

    /**
     * Get class size, in bytes
     */
    public int byteSize() {
        // Sum up class fields
        // Object fields are pointers, so each field is one word
        final int mySize = Arch.get().getWordSize() * fields.size();
        // Add base class size if applicable
        return base != null ? base.byteSize() + mySize : mySize;
    }

    /**
     * Check whether this class is derived from the other class
     * 
     * @param other Other class
     */
    public boolean isDerivedFrom(final ClassSymbol other) {
        for (ClassSymbol iter = this; iter != null; iter = iter.base) {
            if (iter.name.equals(other.name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get class variable (base class variables allowed)
     * 
     * @param name Variable name
     */
    public VarSymbol getVar(final String name) {
        // Linear search here is bad, but I didn't want to have both a HashMap and List
        for (final VarSymbol sym : fields) {
            if (sym.name.equals(name)) {
                return sym;
            }
        }

        // Not found, try base class?
        if (base != null) {
            return base.getVar(name);
        }

        // Sorry...
        return null;
    }

    /**
     * Get offset of class variable (base class variables allowed)
     * 
     * @param name Variable name
     * @return Offset into object
     */
    public int getVarOffset(final String name) {
        // Linear search here is bad, but I didn't want to have both a HashMap and List
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).name.equals(name)) {
                final int offset = i * Arch.get().getWordSize();
                // Convert "this-class-relative" offset to "object-relative" offset
                return base != null ? base.byteSize() + offset : offset;
            }
        }

        // Not found, try base class?
        if (base != null) {
            return base.getVarOffset(name);
        }

        // Sorry...
        Logger.logVerbose("Failed to resolve field %s offset (class %s)", name, this.name);
        return 0;
    }

    /**
     * Get class method (base class method allowed)
     * 
     * @param name Method name
     */
    public MethodSymbol getMethod(final String name) {
        MethodSymbol sym = methods.get(name);

        // Check derived classes if possible
        if (sym == null && base != null) {
            sym = base.getMethod(name);
        }

        return sym;
    }
}
