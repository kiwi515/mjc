/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package optimize;

import main.Logger;
import main.Arch;
import codegen.CodeFragment;

import java.util.HashMap;
import java.util.Map.Entry;

import tree.*;

/**
 * Wrapper for "optimization phase" of compiler
 */
public final class Phase {
    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("Optimize");
    }

    /**
     * Perform optimization phase of compiler.
     * Remove unnecessary instructions from the final assembly code.
     * 
     * @return Success
     */
    public static boolean execute() {
        // Reset state (in case phase is being run again)
        initialize();

        // Print assembly code before register allocation
        for (final CodeFragment frag : codegen.Phase.getCodeFragments()) {
            Logger.logVerboseLn("==========Begin optimization for fragment %s==========",
                    frag.getName());

            // Get temp lifetimes
            final HashMap<NameOfTemp, Lifetime> lifetimes = Lifetime.analyze(frag);
            // Link this to code fragment for use later (see optimization phase)
            frag.lifetimes = lifetimes;

            // Dump lifetime info
            for (Entry<NameOfTemp, Lifetime> pair : lifetimes.entrySet()) {
                final String name = pair.getKey().toString();
                final Lifetime life = pair.getValue();

                Logger.logVerboseLn("Lifetime %s:", name);

                if (life.isValid()) {
                    Logger.logVerboseLn("Begin on instruction (no:%04d) %s ",
                            life.startIndex, life.startInsn.format());
                    Logger.logVerboseLn("End on instruction   (no:%04d) %s",
                            life.endIndex, life.endInsn.format());
                } else {
                    Logger.logVerboseLn("N/A");
                }

                Logger.logVerboseLn("");
            }

            // Keep performing optimization passes until nothing can be changed
            while (Arch.get().optimize(frag)) {
                ;
            }

            Logger.logVerboseLn("");
        }

        return true;
    }
}