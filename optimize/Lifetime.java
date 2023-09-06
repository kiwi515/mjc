/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package optimize;

import assem.*;
import tree.*;
import codegen.CodeFragment;
import main.Util;

import java.util.HashMap;

/**
 * Representation of a temp's lifetime (in its parent code fragment)
 * 
 * This notion of a "lifetime" is naive, as it doesn't account for things like
 * basic blocks.
 * 
 * A temp's lifetime simply has a start and end point, regardless of how the
 * blocks are ordered.
 */
public final class Lifetime {
    // Lifetime-starting instruction
    public Instruction startInsn = null;
    // Index of lifetime-starting instruction
    public int startIndex = -1;

    // Lifetime-ending instruction
    public Instruction endInsn = null;
    // Index of lifetime-ending instruction
    public int endIndex = -1;

    /**
     * Process temp reference
     * 
     * @param insn  Instruction that references temp
     * @param index Index of instruction into its fragment
     */
    public void ref(final Instruction insn, final int index) {
        if (startInsn == null) {
            // Instruction begins lifetime
            startInsn = insn;
            startIndex = index;
        } else {
            // Instruction continues lifetime (could also be the end)
            endInsn = insn;
            endIndex = index;
        }
    }

    /**
     * Check whether the lifetime is valid
     */
    public boolean isValid() {
        if (startInsn == null || endInsn == null) {
            return false;
        }

        if (startIndex == -1 || endIndex == -1) {
            return false;
        }

        return startIndex <= endIndex;
    }

    /**
     * Determine lifetimes of all temps in a code fragment
     * 
     * @return Map from temp name to temp lifetime
     */
    public static HashMap<NameOfTemp, Lifetime> analyze(final CodeFragment frag) {
        // Initialize map
        HashMap<NameOfTemp, Lifetime> map = new HashMap<>();
        for (final TEMP t : frag.tempMgr.children()) {
            map.put(t.temp, new Lifetime());
        }

        // Compute temp lifetimes
        for (int i = 0; i < frag.code.size(); i++) {
            final Instruction insn = frag.code.get(i);

            // Temp references in instruction
            for (final NameOfTemp t : Util.concatList(insn.def(), insn.use())) {
                final Lifetime life = map.get(t);

                // It is okay if this is null.
                // Temps such as "%fp"/"%o0" are not meant to be mapped
                if (life != null) {
                    life.ref(insn, i);
                }
            }
        }

        return map;
    }
}
