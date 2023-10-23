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
 * Visitor which builds an IR statement from an AST node
 */
public final class IRStatementVisitor implements SyntaxTreeVisitor<Stm> {
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
        Logger.logVerboseLn("Not implemented!");
        return null;
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
     * Visit void
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
        // Generate block IR
        Stm frag = null;
        for (final Statement s : n.sl) {
            frag = TranslateUtil.joinFragment(frag, s.accept(this));
        }

        // Empty block
        if (frag == null) {
            // Make no-op
            return new EVAL(new CONST(0));
        }

        return frag;
    }

    /**
     * Visit if statement
     */
    @Override
    @SuppressWarnings("deprecation")
    public Stm visit(final If n) {
        /**
         * Expression data
         */

        // Evaluate expression
        Exp lhs = n.e.accept(new IRExpressionVisitor());
        // Default condition is non-zero check (boolean)
        Exp rhs = new CONST(0);
        int oper = CJUMP.NE;

        // Relop specified data
        if (lhs instanceof RELOP) {
            final RELOP rel = (RELOP) lhs;
            lhs = rel.lhs;
            rhs = rel.rhs;
            oper = rel.oper;
        }

        /**
         * Statement data
         */
        final Stm thenStm = n.s1.accept(this);
        final Stm elseStm = n.s2.accept(this);

        /**
         * IR data
         */

        // Construct labels
        final LABEL t = Phase.getLabelMgr().create("if", "then");
        final LABEL f = Phase.getLabelMgr().create("if", "else");
        final LABEL join = Phase.getLabelMgr().create("if", "end");

        // Construct IR
        return SEQ.fromList(
                new CJUMP(oper, lhs, rhs, t.label, f.label),
                t,
                thenStm,
                new JUMP(join.label),
                f,
                elseStm,
                join);
    }

    /**
     * Visit while loop
     */
    @Override
    public Stm visit(final While n) {
        final LABEL cond = Phase.getLabelMgr().create("while", "cond");
        final LABEL body = Phase.getLabelMgr().create("while", "body");
        final LABEL end = Phase.getLabelMgr().create("while", "end");

        final Exp exp = n.e.accept(new IRExpressionVisitor());
        final Stm stm = n.s.accept(this);

        return TranslateUtil.joinFragments(
                // Test condition
                cond,
                TranslateUtil.expAsCond(exp, body, end),
                // Loop body
                body,
                stm,
                // Jump back to condition
                new JUMP(cond.label),
                // Loop end
                end);
    }

    /**
     * Visit print statement
     */
    @Override
    public Stm visit(final Print n) {
        // Print is an expression
        final Exp asExp = n.accept(new IRExpressionVisitor());
        // Now we make it a statement!
        return new EVAL(asExp);
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Stm visit(final Assign n) {
        Exp lhs = n.i.accept(new IRExpressionVisitor());
        Exp rhs = n.e.accept(new IRExpressionVisitor());

        if (rhs instanceof RELOP) {
            final RELOP rel = (RELOP) rhs;
            rhs = rel.asExp();
        }

        // Assignment IR
        Stm assign = new MOVE(lhs, rhs);

        // Check for pointer assignment, and NOT null pointer
        if (TranslateUtil.identIsObjRef(n.i) && !TranslateUtil.expIsConstZero(rhs)) {
            assign = TranslateUtil.joinFragments(
                    // Decrement outgoing
                    new EVAL(new CALL(
                            new NAME("runtime_ref_dec"),
                            lhs)),
                    // Do assignment
                    assign,
                    // Increment incoming
                    new EVAL(new CALL(
                            new NAME("runtime_ref_inc"),
                            lhs)));
        }

        return assign;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Stm visit(final ArrayAssign n) {
        // Get array base address
        final Exp lhs = n.nameOfArray.accept(new IRExpressionVisitor());
        // Array index
        final Exp index = n.indexInArray.accept(new IRExpressionVisitor());
        // Array offset
        final Exp offset = TranslateUtil.expIndexToOffset(index);
        // New value
        final Exp rhs = n.e.accept(new IRExpressionVisitor());

        // Expression to access array subscript
        final Exp subAccess = new MEM(new BINOP(BINOP.PLUS, lhs, offset));
        return new MOVE(subAccess, rhs);
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
