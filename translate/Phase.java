/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import java.util.ArrayList;

import main.Logger;

import assem.*;
import tree.*;

/**
 * Wrapper for "translate phase" of compiler
 */
public final class Phase {
    // Program IR fragments
    private static final ArrayList<IRFragment> s_fragments = new ArrayList<>();

    // Currently processing method's label manager
    private static final LabelManager s_labelMgr = new LabelManager();
    // Currently processing method's temp manager
    private static TempManager s_currTempMgr;

    /**
     * Clear phase state, in case the phase is being ran again
     */
    private static void initialize() {
        Logger.registerPhase("Translate");
        check.Phase.getSymbolTable().resetScope();
        s_fragments.clear();

        resetForFunction();
    }

    /**
     * Perform translate phase of compiler.
     * Translate program AST into IR
     * 
     * @return Success
     */
    public static boolean execute() {
        // Reset state (in case phase is being run again)
        initialize();

        /**
         * Tree-form IR
         */

        // Generate program IR
        parse.Phase.getAstRoot().accept(new IRProgramVisitor());

        // Pretty-print IR (verbose output)
        for (final IRFragment frag : s_fragments) {
            Logger.logVerboseLn("%s", frag);
        }

        // Error during translation to IR
        if (Logger.isError()) {
            return false;
        }

        /**
         * Linear IR
         */

        // Canonicalize/linearize program IR
        for (final IRFragment frag : s_fragments) {
            frag.linear = canon.Main.transform(frag.stm);
        }

        // Pretty-print linearized IR (verbose output)
        for (final IRFragment frag : s_fragments) {
            Logger.logVerboseLn("%s", frag);
        }

        return true;
    }

    /**
     * Access program IR fragments
     */
    public static ArrayList<IRFragment> getFragments() {
        return s_fragments;
    }

    /**
     * Access current label manager
     */
    public static LabelManager getLabelMgr() {
        return s_labelMgr;
    }

    /**
     * Access current temp manager
     */
    public static TempManager getCurrTempMgr() {
        return s_currTempMgr;
    }

    /**
     * Reset state before translating a new function/fragment
     */
    public static void resetForFunction() {
        // Don't make new label manager too, all functions share the same set
        // to avoid name collision
        s_currTempMgr = new TempManager();
    }
}
