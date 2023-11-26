/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import java.util.ArrayList;

import check.ClassSymbol;
import check.CheckUtil;
import check.MethodSymbol;
import check.SemanticsVisitor;
import check.VarSymbol;
import main.Arch;
import main.Util;

import tree.*;
import syntax.*;

/**
 * Translate phase utilities
 */
public final class TranslateUtil {
    /**
     * Convert expression to conditional
     */
    public static Stm expAsCond(final Exp exp, final LABEL t, final LABEL f) {
        if (exp instanceof RELOP) {
            return ((RELOP) exp).asCond(t, f);
        }

        return new CJUMP(CJUMP.NE, exp, new CONST(0), t.label, f.label);
    }

    /**
     * Determine whether an identifer holds an object reference
     * 
     * @param ident Identifier
     */
    public static boolean identIsObjRef(final Identifier ident) {
        final Type t = ident.accept(new SemanticsVisitor());
        return CheckUtil.typeIsHeapAlloced(t);
    }

    /**
     * Access method variable (can be formal or local)
     * 
     * @param sym  Method symbol
     * @param name Variable name
     * @return IR expression to access variable
     */
    public static Exp accessMethodVar(final MethodSymbol sym, final String name) {
        // Try local variables
        for (int i = 0; i < sym.locals.size(); i++) {
            final VarSymbol var = sym.locals.get(i);

            if (var.name.equals(name)) {
                return Arch.get().getLocalAccess(i);
            }
        }

        // Try formal variables
        for (int i = 0; i < sym.formals.size(); i++) {
            final VarSymbol var = sym.formals.get(i);

            if (var.name.equals(name)) {
                // Add 1 to skip over implicit "this" pointer
                return Arch.get().getFormalAccessAsCallee(i + 1);
            }
        }

        return null;
    }

    /**
     * Access *current* method variable (can be formal or local)
     * 
     * @param name Variable name
     * @return IR expression to access variable
     */
    public static Exp accessCurrentMethodVar(final String name) {
        final MethodSymbol sym = check.Phase.getSymbolTable().currentMethod();
        return sym != null ? accessMethodVar(sym, name) : null;
    }

    /**
     * Access class field
     * 
     * @param sym  Class symbol
     * @param name Field name
     * @return IR expression to access variable
     */
    public static Exp accessClassField(final ClassSymbol sym, final String name) {
        return new BINOP(BINOP.PLUS, Arch.get().getSelfAccess(),
                new CONST(sym.getVarOffset(name)));
    }

    /**
     * Access *current* class field
     * 
     * @param name Field name
     * @return IR expression to access variable
     */
    public static Exp accessCurrentClassField(final String name) {
        final ClassSymbol sym = check.Phase.getSymbolTable().currentClass();
        return sym != null ? accessClassField(sym, name) : null;
    }

    /**
     * Access some symbol in the current scope, whether it be a class field or
     * method argument/local
     * 
     * @param name Symbol name
     * @return IR expression to access symbol
     */
    public static Exp accessScopeSymbol(final String name) {
        // Try method var
        Exp access = accessCurrentMethodVar(name);
        if (access != null) {
            return access;
        }

        // Try class field
        return accessCurrentClassField(name);
    }

    /**
     * Wrap array index expression to convert it to an immediate offset
     * 
     * @param index Array index expression
     * @return Array offset expression
     */
    public static Exp expIndexToOffset(final Exp index) {
        /**
         * Constant (compile-time) index, we can optimize!
         */
        if (expIsConst(index)) {
            final int index_i = ((CONST) index).value;
            return new CONST((index_i + 1) * Arch.get().getWordSize());
        }

        /**
         * Index not known at compile time, must be calculated at runtime
         */
        // Apply +1 to offset to skip over length field
        final Exp adjIndex = new BINOP(BINOP.PLUS, index, new CONST(1));
        // Multiply index by machine word size
        return new BINOP(BINOP.MUL, adjIndex, new CONST(Arch.get().getWordSize()));
    }

    /**
     * Check whether an expression is a constant
     */
    public static boolean expIsConst(final Exp exp) {
        return (exp instanceof CONST);
    }

    /**
     * Check whether an expression is the constant 0
     */
    public static boolean expIsConstZero(final Exp exp) {
        if (!expIsConst(exp)) {
            return false;
        }

        return ((CONST) exp).value == 0;
    }

    /**
     * Check whether an expression is the constant 1
     */
    public static boolean expIsConstOne(final Exp exp) {
        if (!expIsConst(exp)) {
            return false;
        }

        return ((CONST) exp).value == 1;
    }

    /**
     * "Dress" the IR fragment (statement) by adding the prologue/epilogue details
     * (expected by the linearizer)
     * 
     * @param stm    Body fragment
     * @param cls    Class name
     * @param method Method name
     * @return Full fragment
     */
    public static Stm dressFragment(final Stm stm, final String cls, final String method) {
        final LABEL prologueLabel = new LABEL(Util.concatNames(cls, method, "prologueEnd"));
        final JUMP epilogueJump = new JUMP(Util.concatNames(cls, method, "epilogueBegin"));

        return joinFragments(prologueLabel, stm, epilogueJump);
    }

    /**
     * Join two IR fragments.
     * If either is null, the other becomes the result
     */
    @SuppressWarnings("deprecation")
    public static Stm joinFragment(final Stm s1, final Stm s2) {
        if (s1 == null) {
            return s2;
        }

        if (s2 == null) {
            return s1;
        }

        return SEQ.fromList(s1, s2);
    }

    /**
     * Join multiple IR fragments.
     * Null fragments are discarded.
     */
    @SuppressWarnings("deprecation")
    public static Stm joinFragments(final Stm... frags) {
        switch (frags.length) {
            case 0:
                return null;
            case 1:
                return frags[0];
            case 2:
                return joinFragment(frags[0], frags[1]);
            default:
                final ArrayList<Stm> nonNull = new ArrayList<>();
                for (final Stm s : frags) {
                    if (s != null) {
                        nonNull.add(s);
                    }
                }

                final Stm[] asArray = nonNull.toArray(new Stm[nonNull.size()]);
                return SEQ.fromList(asArray);
        }
    }
}
