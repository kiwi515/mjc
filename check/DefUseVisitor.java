/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import java.util.HashMap;

import main.Logger;

import syntax.*;

/**
 * Visitor which checks for usage of uninitialized variables
 */
public final class DefUseVisitor implements SyntaxTreeVisitor<Void> {
    private static final class DefUsePair {
        // Variable definition location
        int defLine = -1;
        int defCol = -1;
        // Variable use location
        int useLine;
        int useCol;
        // Whether this variable has been logged yet
        boolean isLogged;

        public boolean isValid() {
            // Not defined
            if (defLine == -1 || defCol == -1) {
                return false;
            }

            // Used before declaration line
            if (useLine < defLine) {
                return false;
            }

            // Declared and used on the same line
            if (useLine == defLine) {
                return defCol < useCol;
            }

            // Used after declaration line
            return true;
        }
    };

    private final HashMap<String, DefUsePair> s_defUseMap = new HashMap<>();

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
        return null;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Void visit(final SimpleClassDecl n) {
        // Visit methods
        for (final MethodDecl mtd : n.methods) {
            mtd.accept(this);
        }

        return null;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Void visit(final ExtendingClassDecl n) {
        // Visit methods
        for (final MethodDecl mtd : n.methods) {
            mtd.accept(this);
        }

        return null;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Void visit(final MethodDecl n) {
        // Clear the map, as locals only are visible in the method
        s_defUseMap.clear();

        // Visit method locals (variable defs)
        for (final LocalDecl lo : n.locals) {
            lo.accept(this);
        }

        // Visit method statements (possible variable uses)
        for (final Statement s : n.sl) {
            s.accept(this);
        }

        return null;
    }

    /**
     * Visit local declaration
     */
    @Override
    public Void visit(final LocalDecl n) {
        /**
         * Assume a duplicate is not already in the map.
         * (At this point in compilation we know there are no semantic errors)
         * We will fill in the def parts at the first assignment
         */
        final DefUsePair du = new DefUsePair();
        s_defUseMap.put(n.i.s, du);

        return null;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Void visit(final FieldDecl n) {
        return null;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Void visit(final FormalDecl n) {
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
        // Visit statements
        for (final Statement s : n.sl) {
            s.accept(this);
        }

        return null;
    }

    /**
     * Visit if statement
     */
    @Override
    public Void visit(final If n) {
        // Visit condition
        n.e.accept(this);

        // Visit if clause
        n.s1.accept(this);
        // Visit else clause
        n.s2.accept(this);

        return null;
    }

    /**
     * Visit while loop
     */
    @Override
    public Void visit(final While n) {
        // Visit condition
        n.e.accept(this);
        // Visit statement
        n.s.accept(this);

        return null;
    }

    /**
     * Visit print statement
     */
    @Override
    public Void visit(final Print n) {
        // Visit expression
        n.e.accept(this);

        return null;
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Void visit(final Assign n) {
        // Grammar hack for discarding return value
        if (n.i == null) {
            return null;
        }

        // This is a def for the LHS
        final DefUsePair du = s_defUseMap.get(n.i.s);

        // LHS is a local if it is in the map
        if (du != null) {
            du.defLine = n.lineNumber;
            du.defCol = n.columnNumber;
        }

        // Visit RHS
        n.e.accept(this);

        return null;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Void visit(final ArrayAssign n) {
        // Visit array identifier
        n.nameOfArray.accept(this);
        // Visit array index
        n.indexInArray.accept(this);
        // Visit rvalue
        n.e.accept(this);

        return null;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Void visit(final And n) {
        // Visit LHS
        n.e1.accept(this);
        // Visit RHS
        n.e2.accept(this);

        return null;
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Void visit(final LessThan n) {
        // Visit LHS
        n.e1.accept(this);
        // Visit RHS
        n.e2.accept(this);

        return null;
    }

    /**
     * Visit plus expression
     */
    @Override
    public Void visit(final Plus n) {
        // Visit LHS
        n.e1.accept(this);
        // Visit RHS
        n.e2.accept(this);

        return null;
    }

    /**
     * Visit minus expression
     */
    @Override
    public Void visit(final Minus n) {
        // Visit LHS
        n.e1.accept(this);
        // Visit RHS
        n.e2.accept(this);

        return null;
    }

    /**
     * Visit times expression
     */
    @Override
    public Void visit(final Times n) {
        // Visit LHS
        n.e1.accept(this);
        // Visit RHS
        n.e2.accept(this);

        return null;
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Void visit(final ArrayLookup n) {
        // Visit array
        n.expressionForArray.accept(this);
        // Visit array index
        n.indexInArray.accept(this);

        return null;
    }

    /**
     * Visit array length expression
     */
    @Override
    public Void visit(final ArrayLength n) {
        // Visit array
        n.expressionForArray.accept(this);

        return null;
    }

    /**
     * Visit function call expression
     */
    @Override
    public Void visit(final Call n) {
        // Visit callee
        n.e.accept(this);

        // Visit call args
        for (final Expression e : n.el) {
            e.accept(this);
        }

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
        // Convert to identifier to avoid repeating code
        final Identifier id = new Identifier(n.lineNumber, n.columnNumber, n.s);
        id.accept(this);

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
        // Visit array dimension expression
        n.e.accept(this);

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
        // Visit expression
        n.e.accept(this);

        return null;
    }

    /**
     * Visit identifier
     */
    @Override
    public Void visit(final Identifier n) {
        final DefUsePair du = s_defUseMap.get(n.s);

        if (du == null) {
            // Not a local variable (won't be in the map)
            return null;
        }

        // Set use info
        du.useLine = n.lineNumber;
        du.useCol = n.columnNumber;

        // Log error if def-use is invalid
        if (!du.isValid() && !du.isLogged) {
            Logger.addError(du.useLine, du.useCol, "Local %s is not initialized before point of use", n.s);
            // We only want to log this one time (as opposed to all future uses)
            du.isLogged = true;
        }

        return null;
    }
}
