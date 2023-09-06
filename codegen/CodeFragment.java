/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package codegen;

import java.util.List;
import java.util.Map;

import check.ClassSymbol;
import check.MethodSymbol;
import translate.IRFragment;
import translate.TempManager;
import main.Util;
import optimize.Lifetime;
import assem.*;
import tree.NameOfTemp;

/**
 * Architecture-independant program code (assembly) fragment.
 * Contains the ASM itself as well as useful information about the fragment
 */
public abstract class CodeFragment {
    // Parent class of fragment method
    public final ClassSymbol cls;
    // Method which is represented by the fragment
    public final MethodSymbol mtd;
    // Code fragment data
    public List<Instruction> code;
    // Map of temporaries
    public Map<NameOfTemp, String> map;
    // Lifetimes of temporaries
    public Map<NameOfTemp, Lifetime> lifetimes;

    // Whether this function is the main function (entrypoint)
    public boolean isEntry;

    // Fragment-local temp manager
    public final TempManager tempMgr;

    // Generate arch-specific prologue
    protected abstract List<Instruction> prologue();

    // Generate arch-specific epilogue instructions
    protected abstract List<Instruction> epilogue();

    /**
     * Constructor
     * 
     * @param code Function instructions
     * @param ir   IR fragment from which these instructions were selected
     */
    public CodeFragment(final List<Instruction> code, final IRFragment ir) {
        this.code = code;
        this.cls = ir.cls;
        this.mtd = ir.mtd;
        this.map = null;
        this.lifetimes = null;
        this.isEntry = ir.isEntry;

        this.tempMgr = ir.tempMgr;

        // Create prologue/epilogue instructions
        this.code = Util.concatList(prologue(), this.code);
        this.code = Util.concatList(this.code, epilogue());
    }

    /**
     * Get qualified name of fragment
     */
    public String getName() {
        return Util.concatNames(cls.name, mtd.name);
    }

    /**
     * Get label for function prologue
     */
    protected LabelInstruction getPrologueLabel() {
        final String name = getName();
        return CodeGenUtil.labelInsn(name);
    }

    /**
     * Get label for function epilogue
     */
    protected LabelInstruction getEpilogueLabel() {
        final String name = Util.concatNames(getName(), "epilogueBegin");
        return CodeGenUtil.labelInsn(name);
    }

    /**
     * Convert code fragment to string form
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("! Procedure fragment for %s%n", getName()));

        for (final Instruction insn : code) {
            // Indent code
            if (!(insn instanceof LabelInstruction)) {
                builder.append("    ");
            }

            if (map != null) {
                builder.append(insn.format(map));
            } else {
                builder.append(insn.format());
            }
            builder.append("\n");
        }

        builder.append(String.format("! End fragment for %s%n", getName()));
        return builder.toString();
    }
}
