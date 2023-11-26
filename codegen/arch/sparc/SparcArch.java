/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen.arch.sparc;

import java.util.List;
import java.util.ArrayList;

import translate.IRFragment;
import codegen.CodeFragment;
import main.Arch;
import tree.*;

/**
 * SPARC architecture definition
 */
public final class SparcArch extends Arch {
    // Size of a register group
    private final int sc_numRegister = 8;

    /**
     * Get word size on the SPARC architecture
     */
    @Override
    public int getWordSize() {
        return 4;
    }

    /**
     * Get the stack frame size on the SPARC architecture
     * 
     * @param locals Number of locals in the function
     */
    @Override
    public int getStackFrameSize(int locals) {
        // Number of words required in the frame
        int words = locals + 16 + 1 + 6;

        // Frame byte size
        int bytes = getWordSize() * words;
        // Double-word align
        bytes = (bytes + 7) & (-8);

        return bytes;
    }

    /**
     * Check whether the given temp is a register (rather than a real temp)
     */
    @Override
    public boolean tempIsRegister(final TEMP t) {
        final String name = t.temp.toString();

        // Registers are in the format %(i|o|l|g)[0-7]{1}
        if (name.length() != 3) {
            return false;
        }

        if (name.charAt(0) != '%') {
            return false;
        }

        if (name.charAt(1) != 'i'
                && name.charAt(1) != 'o'
                && name.charAt(1) != 'l'
                && name.charAt(1) != 'g') {
            return false;
        }

        final int regNo = '0' - name.charAt(2);
        if (regNo > 7) {
            return false;
        }

        return true;
    }

    /**
     * Access the this pointer/self object through IR
     */
    @Override
    public TEMP getSelfAccess() {
        return new TEMP("%i0");
    }

    /**
     * Access the register in which values are returned (as the callee)
     */
    @Override
    public TEMP getReturnAccessAsCallee() {
        return new TEMP("%i0");
    }

    /**
     * Access the register in which values are returned (as the caller)
     */
    @Override
    public TEMP getReturnAccessAsCaller() {
        return new TEMP("%o0");
    }

    /**
     * Access the stack pointer through IR
     */
    @Override
    public TEMP getStackAccess() {
        return new TEMP("%sp");
    }

    /**
     * Access the frame pointer through IR
     */
    @Override
    public TEMP getFrameAccess() {
        return new TEMP("%fp");
    }

    /**
     * Access stack variable through IR
     */
    @Override
    public Exp getLocalAccess(final int i) {
        // Frame pointer offset (add 1 to skip over padding)
        final int offset = (i + 1) * getWordSize();
        return new BINOP(BINOP.MINUS, getFrameAccess(), new CONST(offset));
    }

    /**
     * Access a formal parameter through IR
     */
    @Override
    public Exp getFormalAccessAsCallee(final int i) {
        assert i < sc_numRegister : "Invalid register";

        return new TEMP(
                String.format("%%i%d", i));
    }

    /**
     * Access a formal parameter through IR
     */
    @Override
    public Exp getFormalAccessAsCaller(final int i) {
        assert i < sc_numRegister : "Invalid register";

        return new TEMP(
                String.format("%%o%d", i));
    }

    /**
     * Access a local register through IR
     */
    @Override
    public Exp getLocalRegisterAccess(final int i) {
        assert i < sc_numRegister : "Invalid register";

        return new TEMP(
                String.format("%%l%d", i));
    }

    /**
     * Access all local registers
     */
    @Override
    public List<Exp> getAllLocalRegisters() {
        final ArrayList<Exp> regs = new ArrayList<>();

        for (int i = 0; i < sc_numRegister; i++) {
            regs.add(getLocalRegisterAccess(i));
        }

        return regs;
    }

    /**
     * Perform SPARC instruction selection
     */
    @Override
    public void insnSelect(final IRFragment frag) {
        SparcSelect.munch(frag);
    }

    /**
     * Perform optimizations on a code fragment
     */
    @Override
    public boolean optimize(final CodeFragment frag) {
        return SparcOptimizer.optimize(frag);
    }
}
