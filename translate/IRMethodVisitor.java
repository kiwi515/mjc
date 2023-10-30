/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import main.Arch;
import main.Logger;

import syntax.*;
import tree.*;

import java.util.LinkedList;

/**
 * Visitor which builds an IR fragment from a class method
 */
public final class IRMethodVisitor implements SyntaxTreeVisitor<Stm> {
    /**
     * Visit program
     */
    @Override
    public Stm visit(final Program n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit main class
     */
    @Override
    public Stm visit(final MainClass n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Stm visit(final SimpleClassDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Stm visit(final ExtendingClassDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Stm visit(final MethodDecl n) {
        Stm frag = null;

        // Class/method name for dressing the fragment
        final String cls = check.Phase.getSymbolTable().currentClass().name;
        final String method = n.i.s;

        // Enter method scope
        check.Phase.getSymbolTable().enterScope(n.i.s);

        // Increment object aliases' ref counts
        for (final FormalDecl f : n.formals) {
            // Ignore non-reference types
            if (!TranslateUtil.identIsObjRef(f.i)) {
                continue;
            }

            // Decrement ref count
            frag = TranslateUtil.joinFragments(
                    frag,
                    new EVAL(new CALL(
                            new NAME("runtime_ref_inc"),
                            f.i.accept(new IRExpressionVisitor()))));
        }

        // Method body
        for (final Statement s : n.sl) {
            frag = TranslateUtil.joinFragment(frag, s.accept(new IRStatementVisitor()));
        }

        // Method return
        final Stm ret = new MOVE(Arch.get().getReturnAccessAsCallee(),
                n.e.accept(new IRExpressionVisitor()));
        frag = TranslateUtil.joinFragment(frag, ret);

        // Cleanup local variables
        for (final LocalDecl l : n.locals) {
            // Ignore non-reference types
            if (!TranslateUtil.identIsObjRef(l.i)) {
                continue;
            }

            // Decrement ref count
            frag = TranslateUtil.joinFragments(
                    frag,
                    new EVAL(new CALL(
                            new NAME("runtime_ref_dec"),
                            l.i.accept(new IRExpressionVisitor()))));
        }

        // Decrement object aliases' ref counts
        for (final FormalDecl f : n.formals) {
            // Ignore non-reference types
            if (!TranslateUtil.identIsObjRef(f.i)) {
                continue;
            }

            // Decrement ref count
            frag = TranslateUtil.joinFragments(
                    frag,
                    new EVAL(new CALL(
                            new NAME("runtime_ref_dec"),
                            f.i.accept(new IRExpressionVisitor()))));
        }

        // Exit method scope
        check.Phase.getSymbolTable().exitScope();

        return TranslateUtil.dressFragment(frag, cls, method);
    }

    /**
     * Visit local declaration
     */
    @Override
    public Stm visit(final LocalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Stm visit(final FieldDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Stm visit(final FormalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit type
     */
    @Override
    public Stm visit(final IdentifierType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit int array
     */
    @Override
    public Stm visit(final IntArrayType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean
     */
    @Override
    public Stm visit(final BooleanType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit integer
     */
    @Override
    public Stm visit(final IntegerType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit Stm
     */
    @Override
    public Stm visit(final VoidType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit block
     */
    @Override
    public Stm visit(final Block n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit if statement
     */
    @Override
    public Stm visit(final If n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit while loop
     */
    @Override
    public Stm visit(final While n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit print statement
     */
    @Override
    public Stm visit(final Print n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Stm visit(final Assign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Stm visit(final ArrayAssign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Stm visit(final And n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Stm visit(final LessThan n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit plus expression
     */
    @Override
    public Stm visit(final Plus n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit minus expression
     */
    @Override
    public Stm visit(final Minus n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit times expression
     */
    @Override
    public Stm visit(final Times n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Stm visit(final ArrayLookup n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array length expression
     */
    @Override
    public Stm visit(final ArrayLength n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit function call expression
     */
    @Override
    public Stm visit(final Call n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit integer literal
     */
    @Override
    public Stm visit(final IntegerLiteral n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean True
     */
    @Override
    public Stm visit(final True n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean False
     */
    @Override
    public Stm visit(final False n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit identifier expression
     */
    @Override
    public Stm visit(final IdentifierExp n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit this
     */
    @Override
    public Stm visit(final This n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array allocation
     */
    @Override
    public Stm visit(final NewArray n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit object/record allocation
     */
    @Override
    public Stm visit(final NewObject n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit logical NOT
     */
    @Override
    public Stm visit(final Not n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit identifier
     */
    @Override
    public Stm visit(final Identifier n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }
}
