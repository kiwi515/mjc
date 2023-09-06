/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import main.Logger;

import tree.*;

/**
 * Interface for all visitors which traverse an IR tree (one fragment)
 */
public abstract class IRTreeVisitor<T> {
    /**
     * Visitor dynamic dispatch for expression types.
     * We cannot modify support.jar to properly support the visitor pattern
     */
    public void visit(final Exp exp) {
        if (exp instanceof BINOP) {
            visit((BINOP) exp);
        } else if (exp instanceof CALL) {
            visit((CALL) exp);
        } else if (exp instanceof CONST) {
            visit((CONST) exp);
        } else if (exp instanceof ESEQ) {
            visit((ESEQ) exp);
        } else if (exp instanceof MEM) {
            visit((MEM) exp);
        } else if (exp instanceof NAME) {
            visit((NAME) exp);
        } else if (exp instanceof RET) {
            visit((RET) exp);
        } else if (exp instanceof TEMP) {
            visit((TEMP) exp);
        } else if (exp instanceof RELOP) {
            visit((RELOP) exp);
        } else {
            Logger.addError("Translate error: Unknown Exp type in IR: %s", exp);
        }
    }

    /**
     * Visitor dynamic dispatch for expression types.
     * We cannot modify support.jar to properly support the visitor pattern
     */
    public void visit(final Stm stm) {
        if (stm instanceof CJUMP) {
            visit((CJUMP) stm);
        } else if (stm instanceof EVAL) {
            visit((EVAL) stm);
        } else if (stm instanceof JUMP) {
            visit((JUMP) stm);
        } else if (stm instanceof LABEL) {
            visit((LABEL) stm);
        } else if (stm instanceof MOVE) {
            visit((MOVE) stm);
        } else if (stm instanceof SEQ) {
            visit((SEQ) stm);
        } else {
            Logger.addError("Translate error: Unknown Stm type in IR: %s", stm);
        }
    }

    // Binary operation
    abstract T visit(final BINOP n);

    // Method call
    abstract T visit(final CALL n);

    // Conditional jump
    abstract T visit(final CJUMP n);

    // Constant literal
    abstract T visit(final CONST n);

    // Expression sequence
    abstract T visit(final ESEQ n);

    // Evaluate expression
    abstract T visit(final EVAL n);

    // Unconditional jump
    abstract T visit(final JUMP n);

    // Label declaration
    abstract T visit(final LABEL n);

    // Memory reference
    abstract T visit(final MEM n);

    // Data move (could be memory, could be register)
    abstract T visit(final MOVE n);

    // Label reference
    abstract T visit(final NAME n);

    // "ESEQ" alias
    abstract T visit(final RET n);

    // Statement sequence
    abstract T visit(final SEQ n);

    // Machine temporary
    abstract T visit(final TEMP n);

    // Our custom node (see translate.RELOP)
    abstract T visit(final RELOP n);
}
