/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen;

import java.util.ArrayList;

import main.Arch;
import main.Logger;
import translate.IRFragment;

/**
 * Wrapper for "codegen phase" of compiler
 */
public final class Phase {
    // Generated assembly instructions
    private static final ArrayList<CodeFragment> s_fragments = new ArrayList<>();

    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("CodeGen");
        s_fragments.clear();
    }

    /**
     * Perform codegen phase of compiler.
     * Generate assembly code from IR
     * 
     * @return Success
     */
    public static boolean execute() {
        // Reset state (in case phase is being run again)
        initialize();

        // Perform code generation using "maximal munch" algorithm
        for (final IRFragment frag : translate.Phase.getFragments()) {
            Arch.get().insnSelect(frag);
        }

        // Print assembly code before register allocation
        for (final CodeFragment frag : s_fragments) {
            Logger.logVerboseLn("%s", frag);
        }
        Logger.logVerboseLn("");

        // Error during instruction selection
        if (Logger.isError()) {
            return false;
        }

        return true;
    }

    /**
     * Access program code fragments
     */
    public static ArrayList<CodeFragment> getCodeFragments() {
        return s_fragments;
    }
}