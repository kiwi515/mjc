/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import main.Logger;

import syntax.*;
import tree.*;

/**
 * Visitor which builds a list of IR fragments from a program
 */
public final class IRProgramVisitor implements SyntaxTreeVisitor<Void> {
    /**
     * Visit program
     */
    @Override
    public Void visit(final Program n) {
        // Visit main class
        n.m.accept(this);

        // Visit class declarations
        for (final ClassDecl cls : n.cl) {
            cls.accept(this);
        }

        return null;
    }

    /**
     * Visit main class
     */
    @Override
    public Void visit(final MainClass n) {
        // Enter class + method scope
        check.Phase.getSymbolTable().enterScope(n.nameOfMainClass.s);
        check.Phase.getSymbolTable().enterScope("main");

        // Create new label/temp managers
        translate.Phase.resetForFunction();
        // Translate main function
        Stm frag = n.body.accept(new IRStatementVisitor());
        // Dump heap for debug at end of execution
        frag = TranslateUtil.joinFragments(
                frag,
                new EVAL(new CALL(new NAME("runtime_debug_dumpheap"))));

        // Dress fragment
        frag = TranslateUtil.dressFragment(frag, n.nameOfMainClass.s, "main");
        // Save fragment
        translate.Phase.getFragments().add(new IRFragment(frag, true));

        // Exit scopes
        check.Phase.getSymbolTable().exitScope();
        check.Phase.getSymbolTable().exitScope();

        return null;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Void visit(final SimpleClassDecl n) {
        // Enter class scope
        check.Phase.getSymbolTable().enterScope(n.i.s);

        // Visit methods
        for (final MethodDecl mtd : n.methods) {
            // Create new label/temp managers
            Phase.resetForFunction();
            // Translate function
            final Stm frag = mtd.accept(new IRMethodVisitor());

            check.Phase.getSymbolTable().enterScope(mtd.i.s);
            Phase.getFragments().add(new IRFragment(frag, false));
            check.Phase.getSymbolTable().exitScope();
        }

        // Exit scope
        check.Phase.getSymbolTable().exitScope();

        return null;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Void visit(final ExtendingClassDecl n) {
        // Enter class scope
        check.Phase.getSymbolTable().enterScope(n.i.s);

        // Visit methods
        for (final MethodDecl mtd : n.methods) {
            // Create new label/temp managers
            Phase.resetForFunction();
            // Translate function
            final Stm frag = mtd.accept(new IRMethodVisitor());

            check.Phase.getSymbolTable().enterScope(mtd.i.s);
            Phase.getFragments().add(new IRFragment(frag, false));
            check.Phase.getSymbolTable().exitScope();
        }

        // Exit scope
        check.Phase.getSymbolTable().exitScope();

        return null;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Void visit(final MethodDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit local declaration
     */
    @Override
    public Void visit(final LocalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Void visit(final FieldDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Void visit(final FormalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit type
     */
    @Override
    public Void visit(final IdentifierType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit int array
     */
    @Override
    public Void visit(final IntArrayType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean
     */
    @Override
    public Void visit(final BooleanType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit integer
     */
    @Override
    public Void visit(final IntegerType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit void
     */
    @Override
    public Void visit(final VoidType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit block
     */
    @Override
    public Void visit(final Block n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit if statement
     */
    @Override
    public Void visit(final If n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit while loop
     */
    @Override
    public Void visit(final While n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit print statement
     */
    @Override
    public Void visit(final Print n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Void visit(final Assign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Void visit(final ArrayAssign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Void visit(final And n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Void visit(final LessThan n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit plus expression
     */
    @Override
    public Void visit(final Plus n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit minus expression
     */
    @Override
    public Void visit(final Minus n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit times expression
     */
    @Override
    public Void visit(final Times n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Void visit(final ArrayLookup n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array length expression
     */
    @Override
    public Void visit(final ArrayLength n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit function call expression
     */
    @Override
    public Void visit(final Call n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit integer literal
     */
    @Override
    public Void visit(final IntegerLiteral n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean True
     */
    @Override
    public Void visit(final True n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean False
     */
    @Override
    public Void visit(final False n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit identifier expression
     */
    @Override
    public Void visit(final IdentifierExp n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit this
     */
    @Override
    public Void visit(final This n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array allocation
     */
    @Override
    public Void visit(final NewArray n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit object/record allocation
     */
    @Override
    public Void visit(final NewObject n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit logical NOT
     */
    @Override
    public Void visit(final Not n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit identifier
     */
    @Override
    public Void visit(final Identifier n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }
}
