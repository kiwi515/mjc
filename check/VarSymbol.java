/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import syntax.Type;

/**
 * Variable symbol
 */
public final class VarSymbol extends BaseSymbol {
    // Variable type
    public Type type;

    public VarSymbol(final String name, final Type type) {
        super(name);
        this.type = type;
    }

    /**
     * Whether the variable has a primitive type
     */
    public boolean isPrimitive() {
        return CheckUtil.typeIsPrim(type);
    }
}
