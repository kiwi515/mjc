/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package regalloc;

import main.Logger;
import codegen.CodeFragment;

/**
 * Wrapper for "register allocation phase" of compiler
 */
public final class Phase {
    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("RegAlloc");
    }

    /**
     * Perform register allocation phase of compiler.
     * Allocate registers for temps throughout the code fragments
     * 
     * @return Success
     */
    public static boolean execute() {
        // Reset state (in case phase is being run again)
        initialize();

        // Allocate registers!!!
        for (final CodeFragment frag : codegen.Phase.getCodeFragments()) {
            Logger.logVerboseLn("==========Begin register allocation for fragment %s==========",
                    frag.getName());

            // Allocate registers
            frag.map = Allocator.assign(frag);

            Logger.logVerboseLn("");
        }

        return true;
    }
}