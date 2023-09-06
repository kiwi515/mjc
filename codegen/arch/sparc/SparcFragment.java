/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen.arch.sparc;

import java.util.List;
import java.util.ArrayList;

import codegen.*;
import translate.IRFragment;

import assem.*;

/**
 * Code fragment for the SPARC architecture
 */
public final class SparcFragment extends CodeFragment {
    public SparcFragment(final List<Instruction> code, final IRFragment ir) {
        super(code, ir);
    }

    /**
     * Generate SPARC prologue
     */
    @Override
    protected List<Instruction> prologue() {
        final ArrayList<Instruction> code = new ArrayList<>();

        // Prologue label
        code.add(getPrologueLabel());

        if (isEntry) {
            code.add(new Comment("Main function does not have a stack frame."));
            code.add(new Comment("As a result, the prologue is empty."));
        } else {
            code.add(new OperationInstruction(String.format(".set LOCLS, %d", this.mtd.locals.size())));
            code.add(new OperationInstruction("save %sp, -4*(LOCLS+1+7+16)&-8, %sp"));
        }

        return code;
    }

    /**
     * Generate SPARC epilogue
     */
    @Override
    protected List<Instruction> epilogue() {
        final ArrayList<Instruction> code = new ArrayList<>();

        // Epilogue label
        code.add(getEpilogueLabel());

        // Main function must call exit to flush output
        if (isEntry) {
            code.add(new OperationInstruction("clr %o0"));
            code.add(new OperationInstruction("call exit"));
            // Waste delay slot
            code.add(new OperationInstruction("nop", "(do nothing in delay slot)"));
        }
        // Non-main functions must destroy their stack frame
        else {
            // Destroy stack frame and return
            code.add(new OperationInstruction("ret"));
            code.add(new OperationInstruction("restore", "(utilize delay slot)"));
        }

        return code;
    }
}
