/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

import java.util.List;

import codegen.CodeFragment;
import codegen.arch.sparc.SparcArch;
import translate.IRFragment;

import tree.*;

/**
 * Interface for compiler targets.
 * Also is a singleton to manage its own lifetime
 */
public abstract class Arch {
    private static Arch s_instance = null;

    /**
     * Setup target architecture based on the compiler configuration
     */
    public static void initialize() {
        switch (Config.getArchType()) {
            case Sparc:
                s_instance = new SparcArch();
                break;
            default:
                Logger.logVerboseLn("Unimplemented architecture: %s",
                        Config.getArchType().name());
        }
    }

    /**
     * Access target architecture
     */
    public static Arch get() {
        assert s_instance != null : "Please call initialize";
        return s_instance;
    }

    /**
     * Get word size on the target architecture
     */
    abstract public int getWordSize();

    /**
     * Get the stack frame size on the target architecture given parameters
     * 
     * @param locals Number of locals in the function
     */
    abstract public int getStackFrameSize(int locals);

    /**
     * Check whether the given temp is a register (rather than a real temp)
     */
    abstract public boolean tempIsRegister(final TEMP t);

    /**
     * Access the this pointer/self object through IR
     */
    abstract public Exp getSelfAccess();

    /**
     * Access the register in which values are returned (as the callee)
     */
    abstract public Exp getReturnAccessAsCallee();

    /**
     * Access the register in which values are returned (as the caller)
     */
    abstract public Exp getReturnAccessAsCaller();

    /**
     * Access the stack pointer through IR
     */
    abstract public Exp getStackAccess();

    /**
     * Access the frame pointer through IR
     */
    abstract public Exp getFrameAccess();

    /**
     * Access stack variable through IR
     */
    abstract public Exp getLocalAccess(final int i);

    /**
     * Access a formal parameter through IR (as the callee)
     */
    abstract public Exp getFormalAccessAsCallee(final int i);

    /**
     * Access a formal parameter through IR (as the caller)
     */
    abstract public Exp getFormalAccessAsCaller(final int i);

    /**
     * Access a local register through IR
     */
    abstract public Exp getLocalRegisterAccess(final int i);

    /**
     * Access all local registers
     */
    abstract public List<Exp> getAllLocalRegisters();

    /**
     * Perform instruction selection on an IR fragment
     */
    abstract public void insnSelect(final IRFragment frag);

    /**
     * Perform optimizations on a code fragment
     */
    abstract public boolean optimize(final CodeFragment frag);
}
