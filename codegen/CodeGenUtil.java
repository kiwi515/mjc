/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen;

import java.util.List;
import java.util.ArrayList;

import assem.*;
import tree.*;

/**
 * Codegen phase utilities
 */
public final class CodeGenUtil {
    /**
     * Generate an OperationInstruction of the specified format
     * 
     * @param format  Instruction format (i.e. "SSD" for {src, src, dst})
     * @param insn    Instruction (Backtick format string)
     * @param comment Comment (Java format string)
     * @param args    Instruction operands
     */
    public static OperationInstruction operInsn(final String format,
            final String insn, final String comment, final Object... args) {
        assert format.length() == args.length : "Format does not match arguments";

        final ArrayList<NameOfTemp> srcs = new ArrayList<>();
        final ArrayList<NameOfTemp> dsts = new ArrayList<>();
        final ArrayList<NameOfLabel> jumps = new ArrayList<>();

        // Pack arguments
        for (int i = 0; i < format.length(); i++) {
            final char type = Character.toUpperCase(format.charAt(i));

            switch (type) {
                case 'S':
                    assert (args[i] instanceof NameOfTemp) : "Bad argument specified";
                    srcs.add((NameOfTemp) args[i]);
                    break;
                case 'D':
                    assert (args[i] instanceof NameOfTemp) : "Bad argument specified";
                    dsts.add((NameOfTemp) args[i]);
                    break;
                case 'J':
                    assert (args[i] instanceof NameOfLabel) : "Bad argument specified";
                    jumps.add((NameOfLabel) args[i]);
                    break;
                default:
                    assert false : "Unsupported instruction format";
                    break;
            }
        }

        // Build instruction object
        final String f_comment = String.format(comment, args);
        return new OperationInstruction(insn, f_comment, dsts, srcs, jumps);
    }

    /**
     * Generate OperationInstruction for a branch instruction
     * 
     * @param insn Instruction (Backtick format string)
     * @param dsts Labels to branch to
     */
    public static OperationInstruction branchInsn(final String insn,
            final List<NameOfLabel> dsts) {

        return new OperationInstruction(insn,
                null,
                null,
                dsts);
    }

    /**
     * Generate OperationInstruction for a branch instruction
     * 
     * @param insn Instruction (Backtick format string)
     * @param dst  Label to branch to
     */
    public static OperationInstruction branchInsn(final String insn,
            final NameOfLabel dst) {
        ArrayList<NameOfLabel> jumps = new ArrayList<>();
        jumps.add(dst);

        return branchInsn(insn, jumps);
    }

    /**
     * Generate OperationInstruction for a branch instruction
     * 
     * @param insn Instruction (Backtick format string)
     * @param dst  Label to branch to
     */
    public static OperationInstruction branchInsn(final String insn,
            final NameOfTemp dst) {
        return branchInsn(insn, new NameOfLabel(dst.toString()));
    }

    /**
     * Create LabelInstruction by name
     */
    public static LabelInstruction labelInsn(final String name) {
        return new LabelInstruction(
                new NameOfLabel(name));
    }

    /**
     * Create LabelInstruction by name
     */
    public static LabelInstruction labelInsn(final String fmt, final Object... args) {
        return labelInsn(String.format(fmt, args));
    }
}
