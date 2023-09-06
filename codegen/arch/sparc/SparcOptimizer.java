/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen.arch.sparc;

import java.util.ArrayList;
import java.util.List;

import codegen.CodeFragment;
import main.Logger;
import main.Util;
import optimize.Lifetime;

import assem.*;
import tree.*;

/**
 * SPARC back-end code optimizations
 */
public final class SparcOptimizer {
    /**
     * Peform optimizations on a code fragment
     */
    public static boolean optimize(final CodeFragment frag) {
        // Whether any optimizations have occurred
        boolean opt = false;
        // Number of instructions deleted in the last optimization
        int numDelete = 0;

        for (int i = 0; i < frag.code.size(); i++) {
            // Reset optimization info
            numDelete = 0;

            // Current instruction
            final Instruction insn = frag.code.get(i);

            // Ignore labels/comments
            if (insn instanceof LabelInstruction || insn instanceof Comment) {
                continue;
            }

            /**
             * Try instruction specific optimizations
             */
            switch (opcode(insn)) {
                case "add":
                    numDelete = tryOptAdd(frag, i);
                    break;
                case "ba":
                    numDelete = tryOptBa(frag, i);
                    break;
                case "call":
                    numDelete = tryOptCall(frag, i);
                    break;
                case "mov":
                    numDelete = tryOptMov(frag, i);
                    break;
                case "set":
                    numDelete = tryOptSet(frag, i);
                    break;
                case "sub":
                    numDelete = tryOptSub(frag, i);
                    break;
            }

            // Fix context after potentially changing code
            if (numDelete >= 0) {
                // Fix instruction index
                i -= numDelete;
                // Update temp lifetimes with new code
                updateLifetimes(frag);
            }

            /**
             * Try generic optimizations
             */
            numDelete = tryOptCleanUpMerge(frag, i);

            // Fix context after potentially changing code
            if (numDelete >= 0) {
                // Fix instruction index
                i -= numDelete;
                // Update temp lifetimes with new code
                updateLifetimes(frag);
            }

            // Mark whether any optimizations were successful
            opt = opt | numDelete >= 0;
        }

        return opt;
    }

    /**
     * Attempt to perform optimizations on an ADD instruction
     * 
     * 1. Simplify operation (tryOptAddSimplify)
     * 2. Eliminate operation temp (tryOptAddMerge)
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptAdd(final CodeFragment frag, final int index) {
        int numDelete = 0;

        // Optimization 1: Try to simplify operation (is one side CONST 0?)
        numDelete = tryOptAddSimplify(frag, index);
        if (numDelete >= 0) {
            return numDelete;
        }

        // Optimization 2: Try to merge operation with next instruction (ld/st)
        numDelete = tryOptAddMerge(frag, index);
        if (numDelete >= 0) {
            return numDelete;
        }

        // No optimizations performed
        return -1;
    }

    /**
     * Given an ADD instruction, convert it to a MOV instruction if it is deemed
     * unnecessary
     * (adding zero to anything).
     * 
     * Examples:
     * add %fp, 0, %i0   =====>   mov %fp, %i0
     * add 0, %fp, %i0   =====>   mov %fp, %i0
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptAddSimplify(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        // Information on source operands
        final NameOfTemp src = insn.use().get(0);
        final NameOfTemp src2 = insn.use().get(1);

        // Operand which is equal to zero (if applicable)
        NameOfTemp zeroAdd = null;
        // Operand which is not equal to zero (must at least be one,
        // otherwise IR optimizer would propogate the constant)
        NameOfTemp nonzeroAdd = null;

        // If either side is zero, the instruction can be substituted with a mov.
        // (Future optimization passes may optimize out this new mov instruction)
        if (src.toString().equals("0")) {
            zeroAdd = src;
            nonzeroAdd = src2;
        } else if (src2.toString().equals("0")) {
            zeroAdd = src2;
            nonzeroAdd = src;
        }

        // Nothing to optimize
        if (zeroAdd == null) {
            return -1;
        }

        Logger.logVerboseLn("Removing unnecessary ADD (0+x or x+0) in favor of MOV: %s",
                insn.format(frag.map));

        // Addition evaluates to other source
        frag.code.set(index,
                new MoveInstruction(
                        "mov `s0, `d0",
                        // Fix comment
                        String.format("%s -> %s", nonzeroAdd, dst),
                        dst,
                        nonzeroAdd));

        // No instructions are deleted
        return 0;
    }

    /**
     * Given an ADD instruction, if its destination temp is deemed unnecessary,
     * delete the temp and merge this instruction with the next instruction.
     * 
     * This optimization applies specifically to IR MEM nodes, or LD/ST instructions.
     * 
     * It's for things like frame pointer accesses, or other accesses where a constant
     * immediate value is applied to offset a register.
     * 
     * Examples:
     * add %fp, 4, %l0   =====>   (removed)
     * st %l1, [%l0]     =====>   st %l1, [%fp+4]
     * 
     * add %fp, 4, %l0   =====>   (removed)
     * ld [%l0], %l1     =====>   ld [%fp+4], %l1
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptAddMerge(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);
        // Information on source operands
        final NameOfTemp src = insn.use().get(0);
        final NameOfTemp src2 = insn.use().get(1);

        // Merge operands (for comment string)
        final String merged = String.format("%s + %s", src, src2);

        // Temp is not thrown away immediately
        if (dstLife == null || dstLife.endIndex != index + 1) {
            return -1;
        }

        // Temp is thrown away in a load instruction...
        if (opcode(dstLife.endInsn).equals("ld")
                // ...and it is the source of the load
                && dstLife.endInsn.uses(dst)) {

            // Update load instruction
            frag.code.set(dstLife.endIndex,
                    new OperationInstruction(
                            "ld [`s0+`s1], `d0",
                            // Fix comment
                            dstLife.endInsn.comment.replace(
                                    dst.toString(),
                                    merged),
                            dstLife.endInsn.def(),
                            Util.makeList(src, src2)));

            // Delete add instruction
            frag.code.remove(index);
            // One instruction deleted
            return 1;
        }
        // Temp is thrown away in a store instruction...
        else if (opcode(dstLife.endInsn).equals("st")
                // ...and it is the destination of the store
                && dstLife.endInsn.defines(dst)) {

            // Update load instruction
            frag.code.set(dstLife.endIndex,
                    new OperationInstruction(
                            "st `s0, [`d0+`d1]",
                            // Fix comment
                            dstLife.endInsn.comment.replace(
                                    dst.toString(),
                                    merged),
                            Util.makeList(src, src2),
                            dstLife.endInsn.use()));

            // Delete add instruction
            frag.code.remove(index);
            // One instruction deleted
            return 1;
        }

        // No instructions are deleted
        return 0;
    }

    /**
     * Given a BA instruction, remove it if it is deeemed unnecessary (it's target
     * immediately follows the instruction).
     * 
     * When emitting IR nodes for a conditional jump (see RELOP.asExp()), two
     * branches are emitted: the conditional branch, which jumps to the "if-then"
     * part; and the unconditional branch, which jumps to the "if-else" part.
     * 
     * In most cases, the unconditional branch is not needed. When creating basic
     * blocks, the "if-else" part is usually placed right after the comparison
     * instructions, thus allowing for execution to fallthrough.
     * 
     * However, it is not a safe assumption to make that the basic blocks will
     * always be ordered this way. Thus, we emit two branches during IR translation,
     * and only after instruction selection do we attempt to remove it.
     * 
     * Example:
     * 
     * Class$Procedure:     ====>   Class$Procedure:
     *     cmp %i1, 1       ====>       cmp %i1, 1
     *     bl if$then0000   ====>       bl if$then0000
     *     nop              ====>       nop
     *     ba if$else0001   ====>       (removed)
     *     nop              ====>       (removed)
     * 
     * if$else0001:         ====>   if$else0001:
     *     nop              ====>       nop
     * 
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptBa(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);
        // Jump destination
        final NameOfLabel target = insn.jumps().get(0);

        // Skip over jump instruction
        int j = index + 1;
        // Look for label immediately after
        while (j < frag.code.size() - 1
                && !(frag.code.get(j) instanceof LabelInstruction)
                || opcode(frag.code.get(j)).equals("nop")) {
            j++;
        }

        // Instruction following branch is not a label
        if (!(frag.code.get(j) instanceof LabelInstruction)) {
            return -1;
        }

        // Interpret instruction as label
        final LabelInstruction label = (LabelInstruction) frag.code.get(j);

        // Next label is not the branch target
        if (!label.label.equals(target)) {
            return -1;
        }

        Logger.logVerboseLn("Removing unnecessary branch (label %s immediately follows): %s",
                label, insn.format(frag.map));

        // Delete all instructions between unconditional branch (inclusive)
        // and target label (exclusive)
        final int numDelete = j - index;

        while (index < j) {
            frag.code.remove(index);
            j--;
        }

        return numDelete;
    }

    /**
     * Given a CALL instruction, do not waste a temp preserving the return value if
     * it is not necessary to do so.
     * 
     * Very often, the result from a function is used "in-place" in some expression,
     * rather than saving it to a local variable.
     * 
     * In these cases, we waste a temp/register saving %o0 to somewhere else.
     * If we can tell that this is unnecessary, we remove the extra MOV by merging
     * it (having the next instruction access %o0 directly).
     * 
     * Example:
     * call Fac$ComputeFac   =====>   call Fac$ComputeFac
     * mov %o0, %l2          =====>   (removed)
     * smul %l1, %l2, %l2    =====>   smul %l1, %o0, %l2
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptCall(final CodeFragment frag, final int index) {
        // Skip over call instruction
        int j = index + 1;

        // Look for "mov %o0, XX" after function call (should be first non-nop
        // instruction)
        while (j < frag.code.size() - 1
                && opcode(frag.code.get(j)).equals("nop")) {
            j++;
        }

        // First non-nop instruction after function call
        final Instruction afterCallInsn = frag.code.get(j);

        // Instruction after call is not a MOV (%o0 is not being preserved)
        if (!opcode(afterCallInsn).equals("mov")) {
            return -1;
        }

        // Information on the temp created for the mov
        final NameOfTemp dst = afterCallInsn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);

        // Temp does not immediately get thrown away.
        // We cannot safely remove it without much further analysis.
        if (dstLife == null || dstLife.endIndex != j + 1) {
            return -1;
        }

        // Will always be %o0, but I shouldn't hardcode that.
        final NameOfTemp src = afterCallInsn.use().get(0);

        optMergeThisToNextInsn(frag, j, src, dst);

        // One instruction deleted
        return 1;
    }

    /**
     * Given a MOV instruction, remove it if it is a no-op (destination == source).
     * 
     * Example:
     * mov %i0, %i0   =====>   (removed)
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptMov(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        // Information on source operand
        final NameOfTemp src = insn.use().get(0);

        // Perfectly okay MOV, nothing to optimize
        if (!dst.equals(src)) {
            return -1;
        }

        Logger.logVerboseLn("Removing MOV instruction (no-op): %s",
                insn.format(frag.map));

        frag.code.remove(index);

        // One instruction deleted
        return 1;
    }

    /**
     * Given a SET instruction, merge it with the next instruction if possible.
     * 
     * SET often is used to hold an immediate value, so we merge it as an *actual*
     * immediate value when possible.
     * 
     * Examples:
     * set 1, %l0           =====>    (removed)
     * add %i0, %l0, %o0    =====>    add %i0, 1, %o0
     * 
     * set 1, %l0           =====>    (removed)
     * sub %i0, %l0, %o0    =====>    sub %i0, 1, %o0
     * 
     * set 1, %l0           =====>    (removed)
     * cmp %i0, %l0         =====>    cmp %i0, 1
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptSet(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on source operand
        final NameOfTemp src = insn.use().get(0);
        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);

        // Can't merge with next instruction, lifetime is too long
        if (dstLife == null || dstLife.endIndex != index + 1) {
            return -1;
        }

        // Next instruction doesn't support immediate values.
        // Can't meaningfully merge this SET instruction.
        if (!opcode(dstLife.endInsn).equals("add")
                && !opcode(dstLife.endInsn).equals("sub")
                && !opcode(dstLife.endInsn).equals("cmp")) {
            return -1;
        }

        // The next instruction does not use the result from this SET instruction
        if (!dstLife.endInsn.uses(dst)) {
            return -1;
        }

        Logger.logVerboseLn("Replacing redundant SET with immediate (next insn throwaway): %s",
                insn.format(frag.map));

        optMergeThisToNextInsn(frag, index, src, dst);

        // One instruction deleted
        return 1;
    }

    /**
     * Attempt to perform optimizations on an SUB instruction
     * 
     * 1. Simplify operation (tryOptSubSimplify)
     * 2. Eliminate operation temp (tryOptSubMerge)
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptSub(final CodeFragment frag, final int index) {
        int numDelete = 0;

        // Optimization 1: Try to simplify operation (is the RHS CONST 0?)
        numDelete = tryOptSubSimplify(frag, index);
        if (numDelete >= 0) {
            return numDelete;
        }

        // Optimization 2: Try to merge operation with next instruction (ld/st)
        numDelete = tryOptSubMerge(frag, index);
        if (numDelete >= 0) {
            return numDelete;
        }

        // No optimizations performed
        return -1;
    }

    /**
     * Given a SUB instruction, convert it to a MOV instruction if it is deemed
     * unnecessary
     * (subtracting zero from anything).
     * 
     * Example:
     * sub %fp, 0, %i0   =====>   mov %fp, %i0
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptSubSimplify(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        // Information on source operands
        final NameOfTemp src = insn.use().get(0);
        final NameOfTemp src2 = insn.use().get(1);

        // If the RHS is zero, the instruction can be substituted with a mov.
        // (Future optimization passes may optimize out this new mov instruction)
        if (!src2.toString().equals("0")) {
            // Nothing to optimize
            return -1;
        }

        Logger.logVerboseLn("Removing unnecessary SUB (x-0) in favor of MOV: %s",
                insn.format(frag.map));

        // Addition evaluates to other source
        frag.code.set(index,
                new MoveInstruction(
                        "mov `s0, `d0",
                        // Fix comment
                        String.format("%s -> %s", src, dst),
                        dst,
                        src));

        // This optimization does not delete instructions
        return 0;
    }

    /**
     * Given an SUB instruction, if its destination temp is deemed unnecessary,
     * delete the temp and merge this instruction with the next instruction.
     * 
     * This optimization applies specifically to IR MEM nodes, or LD/ST instructions.
     * 
     * It's for things like frame pointer accesses, or other accesses where a constant
     * immediate value is applied to offset a register.
     * 
     * Examples:
     * sub %fp, 4, %l0   =====>   (removed)
     * st %l1, [%l0]     =====>   st %l1, [%fp-4]
     * 
     * sub %fp, 4, %l0   =====>   (removed)
     * ld [%l0], %l1     =====>   ld [%fp-4], %l1
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptSubMerge(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);
        // Information on source operands
        final NameOfTemp src = insn.use().get(0);
        final NameOfTemp src2 = insn.use().get(1);

        // Merge operands (for comment string)
        final String merged = String.format("%s - %s", src, src2);

        // Temp is not thrown away immediately
        if (dstLife == null || dstLife.endIndex != index + 1) {
            return -1;
        }

        // Temp is thrown away in a load instruction...
        if (opcode(dstLife.endInsn).equals("ld")
                // ...and it is the source of the load
                && dstLife.endInsn.uses(dst)) {

            // Update load instruction
            frag.code.set(dstLife.endIndex,
                    new OperationInstruction(
                            "ld [`s0-`s1], `d0",
                            // Fix comment
                            dstLife.endInsn.comment.replace(
                                    dst.toString(),
                                    merged),
                            dstLife.endInsn.def(),
                            Util.makeList(src, src2)));

            // Delete sub instruction
            frag.code.remove(index);
            // One instruction deleted
            return 1;
        }
        // Temp is thrown away in a store instruction...
        else if (opcode(dstLife.endInsn).equals("st")
                // ...and it is the destination of the store
                && dstLife.endInsn.defines(dst)) {

            // Update load instruction
            frag.code.set(dstLife.endIndex,
                    new OperationInstruction(
                            "st `s0, [`d0-`d1]",
                            // Fix comment
                            dstLife.endInsn.comment.replace(
                                    dst.toString(),
                                    merged),
                            Util.makeList(src, src2),
                            dstLife.endInsn.use()));

            // Delete sub instruction
            frag.code.remove(index);
            // One instruction deleted
            return 1;
        }

        // No instructions are deleted
        return 0;
    }

    /**
     * Given *ANY* instruction, remove the succeeding MOV instruction if it is
     * deemed unnecessary.
     * 
     * Example:
     * set 1, %l0     =====>   set 1, %l1
     * mov %l0, %l1   =====>   (removed)
     * 
     * @param frag  Code fragment
     * @param index Instruction index (where to perform optimization)
     * @return Number of instructions deleted (to adjust index in caller)
     */
    private static int tryOptCleanUpMerge(final CodeFragment frag, final int index) {
        // Instruction to optimize
        final Instruction insn = frag.code.get(index);

        // Cannot optimize anything
        if (insn.use() == null || insn.def() == null ||
                insn.use().isEmpty() || insn.def().isEmpty()) {
            return -1;
        }

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);

        // Can't merge with next instruction, lifetime is too long
        if (dstLife == null || dstLife.endIndex != index + 1) {
            return -1;
        }

        // Next instruction is not a MOV
        if (!opcode(dstLife.endInsn).equals("mov")) {
            return -1;
        }

        // Next instruction does not use the result from this instruction.
        // Cannot meaningfully merge the two.
        if (!dstLife.endInsn.uses(dst)) {
            return -1;
        }

        Logger.logVerboseLn("Removing redundant move (throwaway) after instruction: %s",
                insn.format(frag.map));

        optMergeNextToThisInsn(frag, index);

        // One instruction deleted
        return 1;
    }

    /**
     * Remove unnecessary temp by merging two instructions.
     * 
     * Specifically, merge the next instruction with the current instruction ("this"
     * instruction).
     * 
     * Do so by replacing the destination of the current instruction with the
     * destination of the next instruction, and then deleting the next instruction.
     * 
     * @param frag  Code fragment containing the instructions
     * @param index Current instruction index
     */
    private static void optMergeNextToThisInsn(final CodeFragment frag,
            final int index) {
        assert index < frag.code.size() : "Bad index";
        final Instruction insn = frag.code.get(index);

        // Information on destination operand
        final NameOfTemp dst = insn.def().get(0);
        final Lifetime dstLife = frag.lifetimes.get(dst);

        // Update this instruction
        frag.code.set(index,
                new OperationInstruction(
                        insn.assem,
                        // Fix comment
                        insn.comment.replace(
                                insn.def().get(0).toString(),
                                dstLife.endInsn.def().get(0).toString()),
                        dstLife.endInsn.def(),
                        insn.use(),
                        insn.jumps()));

        // Remove old instruction
        frag.code.remove(dstLife.endIndex);
    }

    /**
     * Remove unnecessary temp by merging two instructions.
     * 
     * Specifically, merge the current instruction ("this" instruction) with the
     * next instruction.
     * 
     * Do so by replacing instances of the temp "remove" with instances of the temp
     * "keep", and then deleting the current instruction.
     * 
     * @param frag   Code fragment containing the instructions
     * @param index  Current instruction index
     * @param keep   Temp to keep
     * @param remove Temp to remove (by replacing)
     */
    private static void optMergeThisToNextInsn(final CodeFragment frag,
            final int index, final NameOfTemp keep, final NameOfTemp remove) {
        assert index < frag.code.size() : "Bad index";
        assert index < frag.code.size() - 1 : "No next instruction";
        final Instruction nextInsn = frag.code.get(index + 1);

        // Build source args
        assert nextInsn.use() != null : "No sources?";
        final List<NameOfTemp> s = new ArrayList<>();
        for (final NameOfTemp t : nextInsn.use()) {
            // Replace temp in next instruction
            if (t.equals(remove)) {
                s.add(keep);
            } else {
                s.add(t);
            }
        }

        // Update next instruction
        frag.code.set(index + 1,
                new OperationInstruction(
                        nextInsn.assem,
                        // Fix comment
                        nextInsn.comment.replace(
                                remove.toString(),
                                keep.toString()),
                        nextInsn.def(),
                        s,
                        nextInsn.jumps()));

        // Delete this instruction
        frag.code.remove(index);
    }

    /**
     * Extract opcode from an instruction object
     */
    private static String opcode(final Instruction insn) {
        if (insn instanceof LabelInstruction || insn instanceof Comment) {
            return "";
        }

        return insn.format().split("\\s+")[0];
    }

    /**
     * Re-analyze lifetimes of temps in a code fragment.
     * This is necessary after instructions are modified through optimizations
     */
    private static void updateLifetimes(final CodeFragment frag) {
        frag.lifetimes = Lifetime.analyze(frag);
    }
}
