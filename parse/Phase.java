/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package parse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import main.Logger;
import parse.javacc.MiniJavaParser;
import parse.javacc.ParseException;

import syntax.Program;

/**
 * Wrapper for "parse phase" of the compiler
 */
public final class Phase {
    // Root program AST node
    private static Program s_astRoot;

    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("Parse");
        s_astRoot = null;
    }

    /**
     * Perform parse phase of the compiler.
     * Scan and parse the source code, and produce an AST tree.
     * 
     * @param fileName Source file name
     * @return Success
     */
    public static boolean execute(final String fileName) {
        // Reset state (in case phase is being run again)
        initialize();

        // Open file
        final FileInputStream file = openFile(fileName);

        if (file == null) {
            Logger.logVerboseLn("Failed to open file %s", fileName);
            return false;
        }

        /**
         * Parse program to AST
         */

        // Open parser
        final MiniJavaParser parser = new MiniJavaParser(file);
        // Parse file into AST
        s_astRoot = parseProgram(parser);

        // Lexer/parser error, don't proceed further in the compilation
        if (s_astRoot == null || Logger.isError()) {
            Logger.logVerboseLn("Failed to convert program to AST");
            return false;
        }

        return true;
    }

    /**
     * Get root program AST node
     */
    public static Program getAstRoot() {
        return s_astRoot;
    }

    /**
     * Open stream to file specified by path
     * 
     * @param path Filepath
     * @return File input stream (null if error)
     */
    private static FileInputStream openFile(final String path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Parse a program into AST
     * 
     * @param parser MiniJava (JavaCC) parser
     * @return AST root node (null if error)
     */
    private static Program parseProgram(final MiniJavaParser parser) {
        try {
            return parser.Goal();
        } catch (ParseException e) {
            return null;
        }
    }
}
