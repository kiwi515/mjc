/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

public final class Main {
    public static void main(final String[] args) {
        // Inspect flags
        if (!Config.initialize(args)) {
            Config.showHelp();
            return;
        }

        // Initialize architecture
        Arch.initialize();

        // Path to source file
        final String src = Config.getSrcFilePath();
        System.out.println(src);

        // Compile source file
        Logger.begin(src);
        compile(src);
        Logger.end();
    }

    /**
     * Compile source file
     * 
     * @param fileName Source file name
     */
    private static void compile(final String fileName) {
        /**
         * Convert source file path to assembly file path.
         */
        final String asmFileName = fileName.replace(".java", ".s");

        /**
         * I like to think my error handling is pretty robust, but in the case
         * that some unknown exception occurs during testing, we silently fail
         */
        try {
            // Compiler parse phase
            if (!parse.Phase.execute(fileName)) {
                return;
            }

            // Compiler check phase
            if (!check.Phase.execute()) {
                return;
            }

            // Compiler translate phase
            if (!translate.Phase.execute()) {
                return;
            }

            // Compiler codegen phase
            if (!codegen.Phase.execute()) {
                return;
            }

            // Compiler optimization phase
            if (!optimize.Phase.execute()) {
                return;
            }

            // Compiler register allocation phase
            if (!regalloc.Phase.execute()) {
                return;
            }

            // Compiler write phase
            if (!write.Phase.execute(asmFileName)) {
                return;
            }
        } catch (final Exception e) {
            Logger.logVerboseLn(e.toString());
        }
    }
}