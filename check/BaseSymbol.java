/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

/**
 * Basic symbol
 */
public abstract class BaseSymbol {
    // Symbol name
    public String name;

    public BaseSymbol(final String name) {
        this.name = name;
    }
}
