/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import java.util.List;

import check.ClassSymbol;
import check.MethodSymbol;
import main.Util;

import tree.*;

/**
 * Program IR fragment.
 * Contains the IR itself as well as useful information about the fragment
 */
public final class IRFragment {
    // Parent class of fragment method
    public final ClassSymbol cls;
    // Method which is represented by the fragment
    public final MethodSymbol mtd;

    // IR fragment data
    public final Stm stm;
    // IR fragment data (linearized)
    public List<Stm> linear;

    // Whether the IR fragment is the main function (entrypoint)
    public boolean isEntry;

    // Fragment-local temp manager
    public final TempManager tempMgr;

    /**
     * Constructor
     * 
     * @param stm     IR tree root statment
     * @param cls     Class in which this fragment exists
     * @param mtd     Method from which this fragment was derived
     * @param isEntry Whether this fragment is the program's main function
     *                (entrypoint)
     * @param lblMgr  Manager for labels in fragment
     * @param tempMgr Manager for temps in fragment
     */
    public IRFragment(final Stm stm, final ClassSymbol cls,
            final MethodSymbol mtd, final boolean isEntry,
            final TempManager tempMgr) {
        this.stm = stm;
        this.cls = cls;
        this.mtd = mtd;
        this.linear = null;
        this.isEntry = isEntry;

        this.tempMgr = tempMgr;
    }

    /**
     * Alternative constructor.
     * Fills in as much information using the current compiler context.
     * 
     * @param stm     IR tree root statment
     * @param isEntry Whether this fragment is the program's main function
     *                (entrypoint)
     */
    public IRFragment(final Stm stm, final boolean isEntry) {
        this(stm,
                check.Phase.getSymbolTable().currentClass(),
                check.Phase.getSymbolTable().currentMethod(),
                isEntry,
                Phase.getCurrTempMgr());
    }

    /**
     * Get qualified name of fragment
     */
    public String getName() {
        return Util.concatNames(cls.name, mtd.name);
    }

    /**
     * Convert IR fragment to string form
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("! Procedure fragment for %s%n", getName()));

        /**
         * Tree-form IR
         */
        if (linear == null) {
            builder.append(stm.toString());
        }
        /**
         * Linear IR
         */
        else {
            for (int i = 0; i < linear.size(); i++) {
                final Stm node = linear.get(i);
                builder.append(String.format("%03d:   %s", i, node.toString()));
            }
        }

        builder.append(String.format("! End fragment for %s%n", getName()));
        return builder.toString();
    }
}
