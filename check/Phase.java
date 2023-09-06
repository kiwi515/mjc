/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import main.Logger;

import syntax.Program;

/**
 * Wrapper for check phase of compiler
 */
public final class Phase {
    // Symbol table for program
    private static SymbolTable s_symbolTable;

    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("Check");
        s_symbolTable = new SymbolTable();
    }

    /**
     * Perform check phase of compiler.
     * Build symbol table, and perform semantic checks
     * 
     * @return Success
     */
    public static boolean execute() {
        // Reset state (in case phase is being run again)
        initialize();

        // Root program AST node
        final Program root = parse.Phase.getAstRoot();

        // Build symbol table
        root.accept(new SymbolTableVisitor());
        s_symbolTable.resetScope();

        // Symbol table error(s)
        if (Logger.isError()) {
            Logger.logVerboseLn("Error(s) while building symbol table");
            return false;
        }

        // Check semantics
        root.accept(new SemanticsVisitor());

        // Semantic error(s)
        if (Logger.isError()) {
            Logger.logVerboseLn("Semantic error(s) detected");
            return false;
        }

        // Check def-use pairs (find uninitialized variables)
        root.accept(new DefUseVisitor());

        // Uninitialized variable error(s)
        if (Logger.isError()) {
            Logger.logVerboseLn("Uninitialized variable(s) detected");
            return false;
        }

        return true;
    }

    /**
     * Access program symbol table
     */
    public static SymbolTable getSymbolTable() {
        return s_symbolTable;
    }
}
