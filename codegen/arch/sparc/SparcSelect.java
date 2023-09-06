/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen.arch.sparc;

import java.util.ArrayList;
import java.util.List;

import translate.*;
import codegen.*;
import main.Logger;
import main.Util;
import main.Arch;

import assem.*;
import tree.*;

/**
 * Instruction selection for the SPARC architecture
 */
public final class SparcSelect {
    // Selected assembly instructions
    private static ArrayList<Instruction> s_insns = null;
    // Temp manager of currently processing fragment
    private static TempManager s_tempMgr = null;

    /**
     * Perform instruction selection on an IR fragment.
     */
    public static void munch(final IRFragment frag) {
        // Clear existing code
        s_insns = new ArrayList<>();

        // Link temp manager so new temps can be created
        s_tempMgr = frag.tempMgr;

        // Generate new code
        for (final Stm s : frag.linear) {
            munchStm(s);
        }

        /**
         * The linearization phase can add new temps (hoist/call).
         * 
         * These need to be associated with the fragment (to be visible to the register
         * allocator), so here we add them to the fragment's temp manager.
         * 
         * NOTE: Yes, this should be done at the point of linearization.
         * However, it is easier to do this when referencing the assembly instructions
         */
        for (final Instruction insn : s_insns) {
            for (final NameOfTemp t : Util.concatList(insn.def(), insn.use())) {
                final String name = t.toString();

                // Not the kind of temp we are looking for.
                // It is either a register, or some literal
                if (!name.startsWith("t")
                        && !name.startsWith("call")
                        && !name.startsWith("hoist")) {
                    continue;
                }

                // This temp is not new
                if (s_tempMgr.isChild(t)) {
                    continue;
                }

                // Add to manager
                s_tempMgr.addChild(t);
            }
        }

        // Package into fragment
        final CodeFragment codeFrag = new SparcFragment(s_insns, frag);
        codegen.Phase.getCodeFragments().add(codeFrag);
    }

    /**
     * "Munch"/tile IR statement
     */
    private static void munchStm(final Stm stm) {
        if (stm instanceof CJUMP) {
            munchStmCJUMP((CJUMP) stm);
        } else if (stm instanceof EVAL) {
            munchStmEVAL((EVAL) stm);
        } else if (stm instanceof JUMP) {
            munchStmJUMP((JUMP) stm);
        } else if (stm instanceof LABEL) {
            munchStmLABEL((LABEL) stm);
        } else if (stm instanceof MOVE) {
            munchStmMOVE((MOVE) stm);
        } else if (stm instanceof SEQ) {
            munchStmSEQ((SEQ) stm);
        } else {
            Logger.addError("Codegen error: Unknown Stm type in IR: %s", stm);
        }
    }

    /**
     * "Munch"/tile IR expression
     */
    private static NameOfTemp munchExp(final Exp exp, final Object parent) {
        if (exp instanceof BINOP) {
            return munchExpBINOP((BINOP) exp, parent);
        } else if (exp instanceof CALL) {
            return munchExpCALL((CALL) exp, parent);
        } else if (exp instanceof CONST) {
            return munchExpCONST((CONST) exp, parent);
        } else if (exp instanceof ESEQ) {
            Logger.addError("Codegen error: ESEQ node in IR: %s", exp);
        } else if (exp instanceof MEM) {
            return munchExpMEM((MEM) exp, parent);
        } else if (exp instanceof NAME) {
            return munchExpNAME((NAME) exp, parent);
        } else if (exp instanceof RET) {
            return munchExpRET((RET) exp, parent);
        } else if (exp instanceof TEMP) {
            return munchExpTEMP((TEMP) exp, parent);
        } else if (exp instanceof RELOP) {
            Logger.addError("Codegen error: RELOP node in IR: %s", exp);
        } else {
            Logger.addError("Codegen error: Unknown Exp type in IR: %s", exp);
        }

        return new NameOfTemp("BadExp");
    }

    /**
     * "Munch"/tile CJUMP statement
     */
    private static void munchStmCJUMP(final CJUMP stm) {
        /**
         * Emit comparison
         */
        emit(CodeGenUtil.operInsn("SS",
                "cmp `s0, `s1",
                "compare (%s, %s)",
                munchExp(stm.left, stm),
                munchExp(stm.right, stm)));

        /**
         * Emit conditional branch
         */
        switch (stm.relop) {
            case CJUMP.EQ:
                emit(CodeGenUtil.branchInsn("be `j0",
                        stm.iftrue));
                break;
            case CJUMP.NE:
                emit(CodeGenUtil.branchInsn("bne `j0",
                        stm.iftrue));
                break;
            case CJUMP.LT:
                emit(CodeGenUtil.branchInsn("bl `j0",
                        stm.iftrue));
                break;
            case CJUMP.GT:
                emit(CodeGenUtil.branchInsn("bg `j0",
                        stm.iftrue));
                break;
            case CJUMP.LE:
                emit(CodeGenUtil.branchInsn("ble `j0",
                        stm.iftrue));
                break;
            case CJUMP.GE:
                emit(CodeGenUtil.branchInsn("bge `j0",
                        stm.iftrue));
                break;
            default:
                Logger.addError("Codegen error: Bad CJUMP relop type: %d", stm.relop);
                return;
        }

        // Delay slot
        emitNop();

        // Fallthrough branch
        emit(CodeGenUtil.branchInsn("ba `j0", stm.iffalse));
        // Delay slot
        emitNop();
    }

    /**
     * "Munch"/tile EVAL statement
     */
    private static void munchStmEVAL(final EVAL stm) {
        munchExp(stm.exp, stm);
    }

    /**
     * "Munch"/tile JUMP statement
     */
    private static void munchStmJUMP(final JUMP stm) {
        emit(CodeGenUtil.branchInsn("ba `j0", stm.targets));
        // Delay slot
        emitNop();
    }

    /**
     * "Munch"/tile LABEL statement
     */
    private static void munchStmLABEL(final LABEL stm) {
        emit(new LabelInstruction(stm.label));
    }

    /**
     * "Munch"/tile MOVE statement
     */
    private static void munchStmMOVE(final MOVE stm) {
        NameOfTemp lhs;
        NameOfTemp rhs;

        // Move source
        lhs = munchExp(stm.src, stm);
        rhs = munchExp(stm.dst, stm);

        /**
         * Move to temp
         */
        if (stm.dst instanceof TEMP) {
            emit(CodeGenUtil.operInsn("SD",
                    "mov `s0, `d0",
                    "%s -> %s",
                    lhs,
                    rhs));
        }
        /**
         * Move to memory
         */
        else if (stm.dst instanceof MEM) {
            emit(CodeGenUtil.operInsn("SD",
                    "st `s0, [`d0]",
                    "%s -> %s",
                    lhs,
                    rhs));
        } else {
            Logger.addError("Codegen error: Bad MOVE stm in IR: %s", stm);
        }
    }

    /**
     * "Munch"/tile SEQ statement
     */
    private static void munchStmSEQ(final SEQ stm) {
        munchStm(stm.left);
        munchStm(stm.right);
    }

    /**
     * "Munch"/tile BINOP expression
     */
    private static NameOfTemp munchExpBINOP(final BINOP exp, final Object parent) {
        // Get temp for expression result
        final TEMP t = s_tempMgr.create();

        switch (exp.binop) {
            case BINOP.PLUS:
                emit(CodeGenUtil.operInsn("SSD",
                        "add `s0, `s1, `d0",
                        "(%s + %s) -> %s",
                        munchExp(exp.left, exp),
                        munchExp(exp.right, exp),
                        t.temp));
                break;
            case BINOP.MINUS:
                emit(CodeGenUtil.operInsn("SSD",
                        "sub `s0, `s1, `d0",
                        "(%s - %s) -> %s",
                        munchExp(exp.left, exp),
                        munchExp(exp.right, exp),
                        t.temp));
                break;
            case BINOP.MUL:
                emit(CodeGenUtil.operInsn("SSD",
                        "smul `s0, `s1, `d0",
                        "(%s * %s) -> %s",
                        munchExp(exp.left, exp),
                        munchExp(exp.right, exp),
                        t.temp));
                break;
            case BINOP.XOR:
                emit(CodeGenUtil.operInsn("SSD",
                        "xor `s0, `s1, `d0",
                        "(%s ^ %s) -> %s",
                        munchExp(exp.left, exp),
                        munchExp(exp.right, exp),
                        t.temp));
                break;
            default:
                Logger.addError("Codegen error: Bad BINOP operator in IR: %s", exp);
                break;
        }

        return t.temp;
    }

    /**
     * "Munch"/tile CALL expression
     */
    private static NameOfTemp munchExpCALL(final CALL exp, final Object parent) {
        // Get temp for expression result
        final TEMP t = s_tempMgr.create();

        // Load function arguments
        final List<Exp> args = exp.subcomponents();
        for (int i = 1; i < args.size(); i++) {
            emit(CodeGenUtil.operInsn("SD",
                    "mov `s0, `d0",
                    "%s -> %s",
                    munchExp(args.get(i), exp),
                    ((TEMP) Arch.get().getFormalAccessAsCaller(i - 1)).temp));
        }

        // Call function
        emit(CodeGenUtil.branchInsn("call `j0", munchExp(exp.func, exp)));
        // Delay slot
        emitNop();

        // Get result (if it isn't being discarded)
        if (parent != null && !(parent instanceof EVAL)) {
            emit(CodeGenUtil.operInsn("SD",
                    "mov `s0, `d0",
                    "%s -> %s",
                    ((TEMP) Arch.get().getReturnAccessAsCaller()).temp,
                    t.temp));
        }

        return t.temp;
    }

    /**
     * "Munch"/tile CONST expression
     */
    private static NameOfTemp munchExpCONST(final CONST exp, final Object parent) {
        // Load to temporary register
        final TEMP t = s_tempMgr.create();
        // Constant literal value (unsigned)
        final long value = Integer.toUnsignedLong(exp.value);

        emit(CodeGenUtil.operInsn("SD",
                "set `s0, `d0",
                "%s -> %s",
                new NameOfTemp(Long.toString(value)),
                t.temp));

        return t.temp;
    }

    /**
     * "Munch"/tile MEM expression
     */
    private static NameOfTemp munchExpMEM(final MEM exp, final Object parent) {
        if (exp.exp instanceof TEMP || exp.exp instanceof BINOP || exp.exp instanceof MEM) {
            // Get data address
            final NameOfTemp addr = munchExp(exp.exp, exp);

            // Figure out if we need to emit an extra load instruction
            // (because this MEM is on the LHS of another node)
            boolean load = false;
            if (parent != null) {
                // LHS of a MOVE statement
                load |= (parent instanceof MOVE && ((MOVE) parent).src == exp);
                // *ANY* side of a BINOP statement
                load |= (parent instanceof BINOP);
                // *ANY* operand of a comparison
                load |= (parent instanceof CJUMP);
                // *ANY* argument of a function call
                load |= (parent instanceof CALL);
                // Nested MEM nodes
                load |= (parent instanceof MEM);
            }

            if (load) {
                // Load data into temp
                final TEMP t = s_tempMgr.create();

                emit(CodeGenUtil.operInsn("SD",
                        "ld [`s0], `d0",
                        "*(%s) -> %s",
                        addr,
                        t.temp));

                return t.temp;
            }

            return addr;
        }

        Logger.addError("Codegen error: Bad MEM child in IR: %s", exp.exp);
        return new NameOfTemp("BadExpMem");
    }

    /**
     * "Munch"/tile NAME expression
     */
    private static NameOfTemp munchExpNAME(final NAME exp, final Object parent) {
        return new NameOfTemp(exp.label.toString());
    }

    /**
     * "Munch"/tile RET expression
     */
    private static NameOfTemp munchExpRET(final RET exp, final Object parent) {
        munchStm(exp.stm);
        return munchExp(exp.exp, exp);
    }

    /**
     * "Munch"/tile BINOP expression
     */
    private static NameOfTemp munchExpTEMP(final TEMP exp, final Object parent) {
        return exp.temp;
    }

    /**
     * Emit instruction
     * (Exists to improve readability)
     */
    private static void emit(final Instruction insn) {
        s_insns.add(insn);
    }

    /**
     * Emit no-op instruction
     * (Used to waste delay slot)
     */
    private static void emitNop() {
        emit(new OperationInstruction("nop", "(do nothing in delay slot)"));
    }
}
