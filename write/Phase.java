/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package write;

import java.io.FileWriter;
import java.io.IOException;

import main.Logger;
import codegen.CodeFragment;

/**
 * Wrapper for "write phase" of the compiler
 */
public final class Phase {
    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("Write");
    }

    /**
     * Perform write phase of the compiler.
     * Write the generated assembly to an output file.
     * 
     * @param fileName Assembly file name
     * @return Success
     */
    public static boolean execute(final String fileName) {
        // Reset state (in case phase is being run again)
        initialize();

        // Write assembly to output file
        try {
            final FileWriter writer = new FileWriter(fileName);

            writer.write("! Compiler phase 12 by Trevor Schiff.\n");
            writer.write("\n");

            writer.write(".global start\n");
            writer.write("start:\n");
            writer.write("\n");

            for (CodeFragment frag : codegen.Phase.getCodeFragments()) {
                writer.write(frag.toString());
                writer.write("\n");
            }

            writer.close();
        } catch (final IOException e) {
            Logger.addError("Write error: Unable to create assembly file for writing: %s", fileName);
            return false;
        }

        return true;
    }
}
