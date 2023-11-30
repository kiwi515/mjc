/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import check.*;
import main.Arch;
import main.Logger;
import main.Util;

import syntax.*;
import tree.*;

/**
 * Visitor which builds an IR expression from an AST node
 */
public final class IRExpressionVisitor implements SyntaxTreeVisitor<Exp> {
    /**
     * Compiler intrinsic functions (not including print).
     * Map MiniJava "System" class methods -> C runtime functions
     */
    private static final HashMap<String, String> s_intrinsics = new HashMap<String, String>(
            Map.of("gc", "runtime_do_gc_cycle"));

    /**
     * Visit program
     */
    @Override
    public Exp visit(final Program n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit main class
     */
    @Override
    public Exp visit(final MainClass n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Exp visit(final SimpleClassDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Exp visit(final ExtendingClassDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Exp visit(final MethodDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit local declaration
     */
    @Override
    public Exp visit(final LocalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Exp visit(final FieldDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Exp visit(final FormalDecl n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit type
     */
    @Override
    public Exp visit(final IdentifierType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit int array
     */
    @Override
    public Exp visit(final IntArrayType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit boolean
     */
    @Override
    public Exp visit(final BooleanType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit integer
     */
    @Override
    public Exp visit(final IntegerType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit void
     */
    @Override
    public Exp visit(final VoidType n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit block
     */
    @Override
    public Exp visit(final Block n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit if statement
     */
    @Override
    public Exp visit(final If n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit while loop
     */
    @Override
    public Exp visit(final While n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit print statement
     */
    @Override
    public Exp visit(final Print n) {
        // Print argument
        final Exp arg = n.e.accept(new IRExpressionVisitor());
        final Type type = n.e.accept(new SemanticsVisitor());

        // Integer print uses runtime_print_integer
        if (type == Type.THE_INTEGER_TYPE) {
            return new CALL(new NAME("runtime_print_integer"), arg);
        }
        // Boolean print uses runtime_print_boolean
        else if (type == Type.THE_BOOLEAN_TYPE) {
            return new CALL(new NAME("runtime_print_boolean"), arg);
        }

        Logger.addError("Translate error: Bad print arg type: %s", type.getName());
        return null;
    }

    /**
     * Visit assignment statement
     */
    @Override
    public Exp visit(final Assign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit array (subscript) assignment statement
     */
    @Override
    public Exp visit(final ArrayAssign n) {
        Logger.logVerboseLn("Not implemented!");
        return null;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Exp visit(final And n) {
        final LABEL t = Phase.getLabelMgr().create("if", "then");
        final LABEL tt = Phase.getLabelMgr().create("if", "then_inner");
        final LABEL f = Phase.getLabelMgr().create("if", "else");
        final LABEL join = Phase.getLabelMgr().create("if", "end");
        final TEMP tmp = Phase.getCurrTempMgr().create();

        final Exp lhs = n.e1.accept(this);
        final Exp rhs = n.e2.accept(this);

        /**
         * Check if LHS can optimized away
         */
        if (TranslateUtil.expIsConstZero(lhs)) {
            // Expression cannot be true
            return CONST.FALSE;
        }
        if (TranslateUtil.expIsConstOne(lhs)) {
            // LHS can be ignored
            return rhs;
        }

        /**
         * Check if RHS can optimized away
         */
        if (TranslateUtil.expIsConstZero(rhs)) {
            // Expression cannot be true
            return CONST.FALSE;
        }
        if (TranslateUtil.expIsConstOne(rhs)) {
            // LHS can be ignored
            return lhs;
        }

        /**
         * Cannot be optimized away, so perform the following at runtime:
         * tmp = (lhs && rhs) ? 1 : 0
         */
        final Stm cond = TranslateUtil.joinFragments(
                TranslateUtil.expAsCond(lhs, t, f),
                t,
                TranslateUtil.expAsCond(rhs, tt, f),
                tt,
                new MOVE(tmp, CONST.TRUE),
                new JUMP(join.label),
                f,
                new MOVE(tmp, CONST.FALSE),
                join);

        // Expression evaluates to 0/1
        return new RET(cond, tmp);
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Exp visit(final LessThan n) {
        final Exp lhs = n.e1.accept(this);
        final Exp rhs = n.e2.accept(this);

        // Can be computed at compile time if both expressions are constants
        if (TranslateUtil.expIsConst(lhs) && TranslateUtil.expIsConst(rhs)) {
            return ((CONST) lhs).value < ((CONST) rhs).value
                    ? CONST.TRUE
                    : CONST.FALSE;
        }

        return new RELOP(CJUMP.LT, lhs, rhs);
    }

    /**
     * Visit plus expression
     */
    @Override
    public Exp visit(final Plus n) {
        final Exp lhs = n.e1.accept(this);
        final Exp rhs = n.e2.accept(this);

        // Can be computed at compile time if both expressions are constants
        if (TranslateUtil.expIsConst(lhs) && TranslateUtil.expIsConst(rhs)) {
            return new CONST(
                    ((CONST) lhs).value + ((CONST) rhs).value);
        }

        // If LHS/RHS is zero, no addition is performed
        if (TranslateUtil.expIsConstZero(lhs)) {
            return rhs;
        }
        if (TranslateUtil.expIsConstZero(rhs)) {
            return lhs;
        }

        return new BINOP(BINOP.PLUS, lhs, rhs);
    }

    /**
     * Visit minus expression
     */
    @Override
    public Exp visit(final Minus n) {
        final Exp lhs = n.e1.accept(this);
        final Exp rhs = n.e2.accept(this);

        // Can be computed at compile time if both expressions are constants
        if (TranslateUtil.expIsConst(lhs) && TranslateUtil.expIsConst(rhs)) {
            return new CONST(
                    ((CONST) lhs).value - ((CONST) rhs).value);
        }

        // If RHS is zero, no subtraction is performed
        if (TranslateUtil.expIsConstZero(rhs)) {
            return lhs;
        }

        return new BINOP(BINOP.MINUS, lhs, rhs);
    }

    /**
     * Visit times expression
     */
    @Override
    public Exp visit(final Times n) {
        final Exp lhs = n.e1.accept(this);
        final Exp rhs = n.e2.accept(this);

        // Can be computed at compile time if both expressions are constants
        if (TranslateUtil.expIsConst(lhs) && TranslateUtil.expIsConst(rhs)) {
            return new CONST(
                    ((CONST) lhs).value * ((CONST) rhs).value);
        }

        // If LHS/RHS is one, the expression evaluates to the other side
        if (TranslateUtil.expIsConstOne(lhs)) {
            return rhs;
        }
        if (TranslateUtil.expIsConstOne(rhs)) {
            return lhs;
        }

        // If LHS/RHS is zero, the expression evaluates to zero
        if (TranslateUtil.expIsConstZero(lhs)
                || TranslateUtil.expIsConstZero(rhs)) {
            return new CONST(0);
        }

        return new BINOP(BINOP.MUL, lhs, rhs);
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Exp visit(final ArrayLookup n) {
        // Get array base address
        final Exp arrayAccess = n.expressionForArray.accept(this);
        // Array index
        final Exp index = n.indexInArray.accept(this);
        // Array offset
        final Exp offset = TranslateUtil.expIndexToOffset(index);

        return new MEM(new BINOP(BINOP.PLUS, arrayAccess, offset));
    }

    /**
     * Visit array length expression
     */
    @Override
    public Exp visit(final ArrayLength n) {
        // Get array base address
        final Exp arrayAccess = n.expressionForArray.accept(this);
        // Wrap access with MEM node
        return new MEM(arrayAccess);
    }

    /**
     * Visit function call expression
     */
    @Override
    public Exp visit(final Call n) {
        String cls = "DUMMY";
        final String method = n.i.s;

        /**
         * Catch intrinsic functions
         */
        if (n.e instanceof IdentifierExp) {
            final IdentifierExp ie = (IdentifierExp) n.e;

            if (ie.s == "System") {
                // Convert to C runtime function name
                final String runtimeFunc = s_intrinsics.get(method);

                // Validate that this is a real method
                if (runtimeFunc == null) {
                    Logger.addError("Translate error: Unknown intrinsic function: %s", method);
                    return new CALL(new NAME("DUMMY"));
                }

                // Call args
                final ArrayList<Exp> args = new ArrayList<>();
                for (final Expression e : n.el) {
                    args.add(e.accept(this));
                }

                return new CALL(new NAME(runtimeFunc), args);
            }
        }

        /**
         * Determine class name of method by inspecting the callee object
         */

        // Callee is this object
        if (n.e instanceof This) {
            final ClassSymbol cs = check.Phase.getSymbolTable().currentClass();
            cls = cs.name;
        }
        // Callee is newly allocated object
        else if (n.e instanceof NewObject) {
            final NewObject callee = (NewObject) n.e;
            cls = callee.i.s;
        }
        // Callee is a nested call's return value
        else if (n.e instanceof Call) {
            final Type nestedType = n.e.accept(new SemanticsVisitor());
            cls = ((IdentifierType) nestedType).nameOfType;
        }
        // Callee is some variable
        else if (n.e instanceof IdentifierExp) {
            final IdentifierExp ie = (IdentifierExp) n.e;
            VarSymbol vs = check.Phase.getSymbolTable().getVar(ie.s);
            if (vs != null) {
                cls = vs.type.getName();
            }
            // Couldn't find field in the current scope
            else {
                // Check the current class fields
                final ClassSymbol thisClass = check.Phase.getSymbolTable().currentClass();
                // Is this a member access (with "this." omitted)?
                if (thisClass != null) {
                    // Check self members
                    vs = thisClass.getVar(ie.s);
                    cls = vs.type.getName();
                }
            }
        } else {
            Logger.addError("Translate error: Unable to find class name of callee: %s", n.e);
        }

        /**
         * Now we have the callee object's type, and can access the method.
         * 
         * For the purpose of the method symbol, we need to know
         * exactly which class owns the method.
         */

        final ClassSymbol cs = check.Phase.getSymbolTable().getClass(cls);
        final MethodSymbol ms = cs.getMethod(method);

        // Class which owns this method (fallback on callee type name)
        if (ms.parent != null) {
            cls = ms.parent.name;
        }

        /**
         * Build IR node
         */

        final String qualifiedName = Util.concatNames(cls, method);
        final ArrayList<Exp> args = new ArrayList<>();

        // Callee object
        args.add(n.e.accept(this));

        // Call args
        for (final Expression e : n.el) {
            args.add(e.accept(this));
        }

        return new CALL(new NAME(qualifiedName), args);
    }

    /**
     * Visit integer literal
     */
    @Override
    public Exp visit(final IntegerLiteral n) {
        return new CONST(n.i);
    }

    /**
     * Visit boolean True
     */
    @Override
    public Exp visit(final True n) {
        return CONST.TRUE;
    }

    /**
     * Visit boolean False
     */
    @Override
    public Exp visit(final False n) {
        return CONST.FALSE;
    }

    /**
     * Visit identifier expression
     */
    @Override
    public Exp visit(final IdentifierExp n) {
        // Convert to identifier to reuse code
        return new Identifier(n.lineNumber, n.columnNumber, n.s).accept(this);
    }

    /**
     * Visit this
     */
    @Override
    public Exp visit(final This n) {
        return Arch.get().getSelfAccess();
    }

    /**
     * Visit array allocation
     */
    @Override
    public Exp visit(final NewArray n) {
        return new CALL(
                new NAME("runtime_alloc_array"),
                new CONST(Arch.get().getWordSize()),
                n.e.accept(this));

    }

    /**
     * Visit object/record allocation
     */
    @Override
    public Exp visit(final NewObject n) {
        int clsSize = -1;
        final ClassSymbol cls = check.Phase.getSymbolTable().getClass(n.i.s);

        if (cls == null) {
            Logger.addError("Translate error: Could not resolve class %s", n.i.s);
        } else {
            clsSize = cls.byteSize();
        }

        // Not actually allocating anything, so emit null pointer
        if (clsSize == 0) {
            return new CONST(0);
        }

        return new CALL(new NAME("runtime_alloc_object"), new CONST(clsSize));
    }

    /**
     * Visit logical NOT
     */
    @Override
    public Exp visit(final Not n) {
        Exp value = n.e.accept(this);

        // Expand relop
        if (value instanceof RELOP) {
            value = ((RELOP) value).asExp();
        }

        // Can be computed at compile time
        if (TranslateUtil.expIsConst(value)) {
            final boolean b_value = ((CONST) value).value != 0;
            return new CONST(b_value ? 0 : 1);
        }

        return new BINOP(BINOP.XOR, value, new CONST(1));
    }

    /**
     * Visit identifier
     */
    @Override
    public Exp visit(final Identifier n) {
        final Exp access = TranslateUtil.accessScopeSymbol(n.s);
        if (access != null) {
            return (access instanceof BINOP)
                    ? new MEM(access)
                    : access;
        }

        // Hopefully this does not happen
        Logger.addError("Translate error: Failed to resolve identifer %s (scope=%s)", n.s,
                check.Phase.getSymbolTable().getScope());

        return new TEMP("BadExpIdentifier");
    }
}
