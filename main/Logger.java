/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import parse.javacc.TokenMgrError;
import parse.javacc.ParseException;

/**
 * Compiler error logger
 * 
 * Errors are collected over time and displayed only at the end of program
 * execution
 */
public final class Logger {
    /**
     * General compiler error (lexer, parser, semantics, etc.)
     */
    private static class Error {
        // Line in the source file where the error occurred
        public final int line;
        // Column in the source-file where the error occurred
        public final int column;
        // Error message
        public final String msg;

        public Error(final int line, final int col, final String msg) {
            this.line = line;
            this.column = col;
            this.msg = msg;
        }

        public Error(final int line, final int col, final String fmt, Object... args) {
            this(line, col, String.format(fmt, args));
        }
    }

    // File path of the current source file
    private static String s_filePath = null;
    // File stream for verbose output
    private static PrintStream s_verboseFileStrm = null;
    // Whether to terminate log calls with a newline
    private static boolean s_logNewLine = true;
    // Name of current compiler phase
    private static String s_currPhase = null;
    // List of errors found while compiling the current source file
    private static final LinkedList<Error> s_errorList = new LinkedList<>();
    // Regular expression for the lexer error strings
    private static final Pattern s_lexErrPattern = Pattern.compile(".*Lexical error at line (\\d+), column (\\d+)");

    /**
     * Begin logging for a new source file
     * 
     * @param filePath Source file path
     */
    public static void begin(final String filePath) {
        assert s_filePath == null : "Called begin twice!";

        // Setup fields for new context
        s_filePath = filePath;
        s_errorList.clear();

        logVerboseLn("begin %s", s_filePath);
    }

    /**
     * End logging for the current source file
     */
    public static void end() {
        assert s_filePath != null : "End not allowed without begin.";

        logVerboseLn("end %s", s_filePath);
        for (int i = 0; i < 5; i++) {
            logVerboseSeparator();
        }
        logVerboseLn("");

        // Show error count
        println("filename=%s, errors=%d", s_filePath, s_errorList.size());

        // Log errors
        for (final Error err : s_errorList) {
            if (err.column != -1 && err.line != -1) {
                errln(String.format("%s:%d:%d: %s", s_filePath, err.line, err.column, err.msg));
            } else {
                errln(String.format("%s: %s", s_filePath, err.msg));
            }
        }

        // Clear fields
        s_filePath = null;
        s_errorList.clear();
    }

    /**
     * Check whether the compiler is in an error state (> 0 errors)
     */
    public static boolean isError() {
        return !s_errorList.isEmpty();
    }

    /**
     * Add new error to the logger
     * 
     * @param line Source file line
     * @param col  Source file column
     * @param fmt  Error message
     * @param args Error message format args
     */
    public static void addError(final int line, final int col, final String fmt, final Object... args) {
        final Error err = new Error(line, col, fmt, args);
        s_errorList.add(err);
    }

    /**
     * Add new error to the logger
     * 
     * @param fmt  Error message
     * @param args Error message format args
     */
    public static void addError(final String fmt, final Object... args) {
        addError(-1, -1, fmt, args);
    }

    /**
     * Add new JavaCC lexer error (TokenMgrError) to the logger.
     * This error only gives us a string, so we have to hackily parse it.
     * 
     * @param e Lexer error
     */
    public static void addLexerError(final TokenMgrError e) {
        // Extract line/column number via regex
        final String errMsg = e.getMessage();
        final Matcher matcher = s_lexErrPattern.matcher(errMsg);

        // Fallback information is -1
        int line = -1;
        int col = -1;

        // Extract from regex
        if (matcher.find()) {
            line = Integer.parseInt(matcher.group(1));
            col = Integer.parseInt(matcher.group(2));
        }

        addError(line, col, "Lexer error");
    }

    /**
     * Add new JavaCC parser error (ParseException) to the logger.
     * 
     * @param e Parser error
     */
    public static void addParserError(final ParseException e) {
        final int line = e.currentToken.next.beginLine;
        final int col = e.currentToken.next.beginColumn;

        addError(line, col, "Parser error");
    }

    /**
     * Begin new compiler phase (for logging)
     * 
     * @param phase Name of phase
     */
    public static void registerPhase(final String phase) {
        s_currPhase = phase;
        logVerboseSeparator();
        logVerboseLn("[Entering %s phase]", s_currPhase);
        logVerboseSeparator();
    }

    /**
     * Log verbose (usually phase-specific) information to the console.
     * Nothing is printed if the system property "dumpPhases" is not set
     * *Do not terminate the string with a newline.*
     * 
     * @param fmt  Message (format string)
     * @param args Format arguments
     */
    public static void logVerbose(final String fmt, final Object... args) {
        s_logNewLine = false;
        logVerboseInner(fmt, args);
    }

    /**
     * Log verbose (usually phase-specific) information to the console.
     * Nothing is printed if the system property "dumpPhases" is not set
     * *Do not terminate the string with a newline.*
     * 
     * @param fmt  Message (format string)
     * @param args Format arguments
     */
    public static void logVerboseLn(final String fmt, final Object... args) {
        s_logNewLine = true;
        logVerboseInner(fmt, args);
    }

    /**
     * Log verbose (usually phase-specific) information to the console.
     * Nothing is printed if the system property "dumpPhases" is not set
     * 
     * @param fmt  Message (format string)
     * @param args Format arguments
     */
    private static void logVerboseInner(final String fmt, final Object... args) {
        // Verbose output disabled
        if (!Config.isVerbose()) {
            return;
        }

        // Open log file stream if necessary
        if (s_verboseFileStrm == null) {
            try {
                s_verboseFileStrm = new PrintStream("verbose.txt");
            } catch (FileNotFoundException e) {
                errln("Cannot open verbose.txt for logging!");
            }
        }

        // Backup reference to stdout stream
        final PrintStream stdout = System.out;

        // Temporarily redirect the print function
        System.setOut(s_verboseFileStrm);
        if (s_logNewLine) {
            println(fmt, args);
        } else {
            print(fmt, args);
        }
        System.setOut(stdout);
    }

    /**
     * Write separator line to verbose log
     */
    private static void logVerboseSeparator() {
        // Print lines to separate file output
        for (int j = 0; j < 80; j++) {
            logVerbose("=");
        }

        logVerboseLn("");
    }

    /**
     * Print to the standard output stream
     * 
     * @param fmt  Format string
     * @param args Format args
     */
    private static void print(final String fmt, final Object... args) {
        System.out.printf("%s", String.format(fmt, args));
    }

    /**
     * Print (with newline) to the standard output stream
     * 
     * @param fmt  Format string
     * @param args Format args
     */
    private static void println(final String fmt, final Object... args) {
        System.out.printf("%s%n", String.format(fmt, args));
    }

    /**
     * Print to the standard error stream
     * 
     * @param fmt  Format string
     * @param args Format args
     */
    private static void err(final String fmt, final Object... args) {
        System.err.printf("%s", String.format(fmt, args));
    }

    /**
     * Print (with newline) to the standard error stream
     * 
     * @param fmt  Format string
     * @param args Format args
     */
    private static void errln(final String fmt, final Object... args) {
        System.err.printf("%s%n", String.format(fmt, args));
    }
}