/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package regalloc;

import java.util.Map;
import java.util.HashMap;

import codegen.CodeFragment;
import main.Logger;
import optimize.Lifetime;
import tree.*;
import assem.*;

/**
 * Register allocator
 */
public final class Allocator {
    // Register pool
    private static Pool s_regPool = null;

    /**
     * Assign registers to temps in a code fragment
     * 
     * @return Map from temp to register
     */
    public static Map<NameOfTemp, String> assign(final CodeFragment frag) {
        // Initialize register pool
        s_regPool = new Pool();

        // All "alive" allocations (not yet freed)
        Map<NameOfTemp, String> alive = new HashMap<>();

        // Final register map
        Map<NameOfTemp, String> map = new HashMap<>() {
            @Override
            public String get(Object key) {
                assert key instanceof NameOfTemp : "WHY!!!";
                final NameOfTemp t = (NameOfTemp) key;

                // Temps missing from the map should keep their original names
                if (!containsKey(t)) {
                    return t.toString();
                }

                return super.get(t);
            }
        };

        // Step through code instructions and assign registers
        for (int i = 0; i < frag.code.size(); i++) {
            final Instruction insn = frag.code.get(i);

            /**
             * Free any registers that go out of use (from lifetimes)
             */
            for (final TEMP t : frag.tempMgr.children()) {
                // Temp is not being used
                if (!alive.containsKey(t.temp)) {
                    continue;
                }

                // Check lifetime
                final Lifetime life = frag.lifetimes.get(t.temp);
                assert life != null : "Missing lifetime?";

                // Is lifetime over?
                if (i > life.endIndex) {
                    final String reg = alive.get(t.temp);

                    // Free register
                    if (s_regPool.release(reg)) {
                        Logger.logVerboseLn("Freeing register %s because lifetime is over (end: %04d, insn: %04d)",
                                reg, life.endIndex, i);
                    }

                    // Remove from alive list
                    alive.remove(t.temp);
                }
            }

            /**
             * Assign new regs for definitions
             */
            // No definitions in this instruction
            if (insn.def() == null) {
                continue;
            }

            for (final NameOfTemp def : insn.def()) {
                // Not *actually* a temp (maybe some known register)
                if (!frag.tempMgr.isChild(def)) {
                    continue;
                }

                // Already assigned a register?
                if (map.containsKey(def)) {
                    continue;
                }

                // First-come, first-serve
                final String reg = s_regPool.acquire();
                Logger.logVerboseLn("Allocated register %s for temp %s", reg, def);

                if (reg != null) {
                    // Save allocation
                    assert !map.containsKey(def) : "Double-allocation???";
                    map.put(def, reg);

                    // Mark allocation as alive
                    alive.put(def, reg);
                } else {
                    Logger.logVerbose("Ran out of registers, and I cannot spill!");
                    return map;
                }
            }
        }

        return map;
    }
}
