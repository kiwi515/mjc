/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import main.Logger;

import syntax.*;

/**
 * Visitor which builds a symbol table for the program
 */
public class SymbolTableVisitor implements SyntaxTreeVisitor<Void> {
    // Symbol table reference
    private final SymbolTable m_symbolTable;

    public SymbolTableVisitor() {
        m_symbolTable = Phase.getSymbolTable();
        assert m_symbolTable != null;
        m_symbolTable.resetScope();
    }

    /**
     * Visit program
     */
    @Override
    public Void visit(final Program n) {
        // Visit main class
        n.m.accept(this);

        // Visit class declarations
        for (ClassDecl cls : n.cl) {
            cls.accept(this);
        }

        return null;
    }

    /**
     * Visit main class
     */
    @Override
    public Void visit(final MainClass n) {
        // Add class to symbol table
        if (m_symbolTable.putClass(new ClassSymbol(n.nameOfMainClass.s, null)) != null) {
            Logger.addError(n.lineNumber, n.columnNumber, "Main class %s re-declared", n.nameOfMainClass.s);
        }

        // Enter class scope
        m_symbolTable.enterScope(n.nameOfMainClass.s);
        // Add main function
        m_symbolTable.putMethod(new MethodSymbol("main", Type.THE_VOID_TYPE));
        // Exit scope
        m_symbolTable.exitScope();

        return null;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Void visit(final SimpleClassDecl n) {
        // Add class to symbol table
        if (m_symbolTable.putClass(new ClassSymbol(n.i.s, null)) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Class %s re-declared", n.i.s);
        }

        // Enter class scope
        m_symbolTable.enterScope(n.i.s);

        // Visit fields
        for (FieldDecl fld : n.fields) {
            fld.accept(this);
        }

        // Add methods
        for (MethodDecl mtd : n.methods) {
            mtd.accept(this);
        }

        // Exit scope
        m_symbolTable.exitScope();

        return null;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Void visit(final ExtendingClassDecl n) {
        // Get information of base class
        // (NOTE: This can be null in the case where the derived class is defined before
        // the base class. Because of this, we try this again during the semantics pass)
        final ClassSymbol base = m_symbolTable.getClass(n.j.s);

        // Add class to symbol table
        if (m_symbolTable.putClass(new ClassSymbol(n.i.s, base)) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Class %s re-declared", n.i.s);
        }

        // Enter class scope
        m_symbolTable.enterScope(n.i.s);

        // Visit fields
        for (FieldDecl fld : n.fields) {
            fld.accept(this);
        }

        // Add methods
        for (MethodDecl mtd : n.methods) {
            mtd.accept(this);
        }

        // Exit scope
        m_symbolTable.exitScope();

        return null;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Void visit(final MethodDecl n) {
        // Create method info
        final MethodSymbol ms = new MethodSymbol(n.i.s, n.t);

        // Enter method scope
        m_symbolTable.enterScope(n.i.s);

        // Visit formals
        for (FormalDecl fr : n.formals) {
            fr.accept(this);

            // Also, link the formals to this MethodSymbol
            final VarSymbol vs = new VarSymbol(fr.i.s, fr.t);
            ms.formals.add(vs);
        }

        // Visit method locals
        for (LocalDecl lo : n.locals) {
            lo.accept(this);

            // Also, link the locals to this MethodSymbol
            final VarSymbol vs = new VarSymbol(lo.i.s, lo.t);
            ms.locals.add(vs);
        }

        // Exit method scope
        m_symbolTable.exitScope();

        // Add method to symbol table
        if (m_symbolTable.putMethod(ms) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Class method %s re-declared", n.i.s);
        }

        return null;
    }

    /**
     * Visit local declaration
     */
    @Override
    public Void visit(final LocalDecl n) {
        final VarSymbol vs = new VarSymbol(n.i.s, n.t);

        if (m_symbolTable.putVar(vs) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Function local %s re-declared", n.i.s);
        }

        return null;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Void visit(final FieldDecl n) {
        final VarSymbol vs = new VarSymbol(n.i.s, n.t);

        if (m_symbolTable.putClassVar(vs) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Class field %s re-declared", n.i.s);
        }

        return null;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Void visit(final FormalDecl n) {
        if (m_symbolTable.putVar(new VarSymbol(n.i.s, n.t)) != null) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Function formal parameter %s re-declared", n.i.s);
        }

        return null;
    }

    /**
     * Visit type
     */
    @Override
    public Void visit(final IdentifierType n) {
        return null;
    }

    /**
     * Visit int array
     */
    @Override
    public Void visit(final IntArrayType n) {
        return null;
    }

    /**
     * Visit boolean
     */
    @Override
    public Void visit(final BooleanType n) {
        return null;
    }

    /**
     * Visit integer
     */
    @Override
    public Void visit(final IntegerType n) {
        return null;
    }

    /**
     * Visit void
     */
    @Override
    public Void visit(final VoidType n) {
        return null;
    }

    /**
     * Visit block
     */
    @Override
    public Void visit(final Block n) {
        return null;
    }

    /**
     * Visit if statement
     */
    @Override
    public Void visit(final If n) {
        return null;
    }

    /**
     * Visit while loop
     */
    @Override
    public Void visit(final While n) {
        return null;
    }

    /**
     * Visit print statement
     */
    @Override
    public Void visit(final Print n) {
        return null;
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Void visit(final Assign n) {
        return null;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Void visit(final ArrayAssign n) {
        return null;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Void visit(final And n) {
        return null;
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Void visit(final LessThan n) {
        return null;
    }

    /**
     * Visit plus expression
     */
    @Override
    public Void visit(final Plus n) {
        return null;
    }

    /**
     * Visit minus expression
     */
    @Override
    public Void visit(final Minus n) {
        return null;
    }

    /**
     * Visit times expression
     */
    @Override
    public Void visit(final Times n) {
        return null;
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Void visit(final ArrayLookup n) {
        return null;
    }

    /**
     * Visit array length expression
     */
    @Override
    public Void visit(final ArrayLength n) {
        return null;
    }

    /**
     * Visit function call expression
     */
    @Override
    public Void visit(final Call n) {
        return null;
    }

    /**
     * Visit integer literal
     */
    @Override
    public Void visit(final IntegerLiteral n) {
        return null;
    }

    /**
     * Visit boolean True
     */
    @Override
    public Void visit(final True n) {
        return null;
    }

    /**
     * Visit boolean False
     */
    @Override
    public Void visit(final False n) {
        return null;
    }

    /**
     * Visit identifier expression
     */
    @Override
    public Void visit(final IdentifierExp n) {
        return null;
    }

    /**
     * Visit this
     */
    @Override
    public Void visit(final This n) {
        return null;
    }

    /**
     * Visit array allocation
     */
    @Override
    public Void visit(final NewArray n) {
        return null;
    }

    /**
     * Visit object/record allocation
     */
    @Override
    public Void visit(final NewObject n) {
        return null;
    }

    /**
     * Visit logical NOT
     */
    @Override
    public Void visit(final Not n) {
        return null;
    }

    /**
     * Visit identifier
     */
    @Override
    public Void visit(final Identifier n) {
        return null;
    }
}
