/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package regalloc;

import java.util.ArrayList;
import java.util.List;

import main.Arch;

import tree.*;

/**
 * Pool of available registers doing allocation
 */
public final class Pool {
    /**
     * Register in the pool
     */
    static final class Register {
        // Register temp/name
        String name;
        // Whether the register is free
        boolean free = true;

        public Register(final String name) {
            this.name = name;
        }
    };

    // All local registers
    private final List<Register> m_regs = new ArrayList<>();

    public Pool() {
        final List<Exp> regs = Arch.get().getAllLocalRegisters();

        for (final Exp e : regs) {
            assert e instanceof TEMP;
            final TEMP t = (TEMP) e;

            m_regs.add(new Register(t.temp.toString()));
        }
    }

    /**
     * Acquire local register from the pool
     */
    public String acquire() {
        // Find first available register
        for (final Register reg : m_regs) {
            if (reg.free) {
                reg.free = false;
                return reg.name;
            }
        }

        // No more registers... :(
        return null;
    }

    /**
     * Free register to the pool
     * 
     * @param name Register name
     * @return Whether the register was able to be freed
     */
    public boolean release(final String name) {
        for (final Register reg : m_regs) {
            if (reg.name.equals(name) && reg.free == false) {
                reg.free = true;
                return true;
            }
        }

        return false;
    }
}
