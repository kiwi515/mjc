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
 * Visitor which performs semantic checks on the program
 */
public final class SemanticsVisitor implements SyntaxTreeVisitor<Type> {
    // Symbol table reference
    private final SymbolTable m_symbolTable;

    public SemanticsVisitor() {
        // Symbol table required to perform type checking
        m_symbolTable = Phase.getSymbolTable();
        assert m_symbolTable != null;
    }

    /**
     * Visit program
     */
    @Override
    public Type visit(final Program n) {
        // Visit main class
        n.m.accept(this);

        // Visit class declarations
        for (ClassDecl cls : n.cl) {
            cls.accept(this);
        }

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit main class
     */
    @Override
    public Type visit(final MainClass n) {
        // Enter class scope
        m_symbolTable.enterScope(n.nameOfMainClass.s);
        // Enter method scope
        m_symbolTable.enterScope("main");

        // Visit statement
        n.body.accept(this);

        // Exit scopes
        m_symbolTable.exitScope();
        m_symbolTable.exitScope();

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit simple class declaration
     */
    @Override
    public Type visit(final SimpleClassDecl n) {
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

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit extending class declaration
     */
    @Override
    public Type visit(final ExtendingClassDecl n) {
        final ClassSymbol myCls = m_symbolTable.getClass(n.i.s);
        final ClassSymbol baseCls = m_symbolTable.getClass(n.j.s);
        assert myCls != null : "Couldn't find my class";
        assert baseCls != null : "Couldn't find base class";

        // Finish derived class symbol setup (if necessary)
        if (myCls.base == null) {
            myCls.base = baseCls;
        }

        // Circular dependency check
        if (baseCls.isDerivedFrom(myCls)) {
            Logger.addError(n.i.lineNumber, n.i.columnNumber, "Circular inheritance between classes %s and %s",
                    myCls.name, baseCls.name);
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

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit method declaration
     */
    @Override
    public Type visit(final MethodDecl n) {
        // Get info on method
        final MethodSymbol sym = m_symbolTable.getMethod(n.i.s);
        assert sym != null : "Couldn't find method";

        // Enter method scope
        m_symbolTable.enterScope(n.i.s);

        // Visit method body
        for (Statement st : n.sl) {
            st.accept(this);
        }

        // Method return value type should match the declaration
        final Type type = n.e.accept(this);
        // Up-casting is allowed (derived -> base)
        if (!CheckUtil.typeCanUpCast(type, sym.type, m_symbolTable)) {
            Logger.addError(n.e.lineNumber, n.e.columnNumber,
                    "Cannot convert return value from type %s to type %s", type, sym.type);
        }

        // Exit method scope
        m_symbolTable.exitScope();

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit local declaration
     */
    @Override
    public Type visit(final LocalDecl n) {
        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit field declaration
     */
    @Override
    public Type visit(final FieldDecl n) {
        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit formal declaration
     */
    @Override
    public Type visit(final FormalDecl n) {
        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit type
     */
    @Override
    public Type visit(final IdentifierType n) {
        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit int array
     */
    @Override
    public Type visit(final IntArrayType n) {
        return Type.THE_INT_ARRAY_TYPE;
    }

    /**
     * Visit boolean
     */
    @Override
    public Type visit(final BooleanType n) {
        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit integer
     */
    @Override
    public Type visit(final IntegerType n) {
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit void
     */
    @Override
    public Type visit(final VoidType n) {
        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit block
     */
    @Override
    public Type visit(final Block n) {
        // Visit statements
        for (Statement st : n.sl) {
            st.accept(this);
        }

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit if statement
     */
    @Override
    public Type visit(final If n) {
        // Condition must be boolean
        final Type condType = n.e.accept(this);
        if (condType != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.e.lineNumber, n.e.columnNumber, "If statement condition must be boolean, not %s",
                    condType);
        }

        // Visit clauses
        n.s1.accept(this);
        n.s2.accept(this);

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit while loop
     */
    @Override
    public Type visit(final While n) {
        // Loop condition must be boolean
        final Type condType = n.e.accept(this);
        if (condType != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.e.lineNumber, n.e.columnNumber, "While loop condition must be boolean, not %s", condType);
        }

        // Visit loop statements
        n.s.accept(this);

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit print statement
     */
    @Override
    public Type visit(final Print n) {
        // Expression should be integer/boolean
        final Type expType = n.e.accept(this);

        if (expType != Type.THE_INTEGER_TYPE && expType != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.lineNumber, n.columnNumber, "Cannot print value of type %s", expType);
        }

        return Type.THE_VOID_TYPE;
    }

    /**
     * Visit assignment expression
     */
    @Override
    public Type visit(final Assign n) {
        // Get LHS/RHS types
        final Type lhsType = n.i.accept(this);
        final Type rhsType = n.e.accept(this);

        // LHS type should match RHS type
        // Up-casting is allowed (derived -> base)
        if (!CheckUtil.typeCanUpCast(rhsType, lhsType, m_symbolTable)) {
            Logger.addError(n.lineNumber, n.columnNumber, "Cannot convert from type %s to type %s",
                    rhsType, lhsType);
        }

        // Assignment evaluates to RHS
        return rhsType;
    }

    /**
     * Visit array assignment expression
     */
    @Override
    public Type visit(final ArrayAssign n) {
        // Can only perform array index on an array
        if (n.nameOfArray.accept(this) != Type.THE_INT_ARRAY_TYPE) {
            Logger.addError(n.nameOfArray.lineNumber, n.nameOfArray.columnNumber,
                    "Cannot perform array index on non-array object: %s", n.nameOfArray);
        }

        // Can only index array using integer value
        if (n.indexInArray.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.indexInArray.lineNumber, n.indexInArray.columnNumber,
                    "Cannot index array by non-integer value: %s", n.indexInArray);
        }

        // New rvalue must be an integer value
        final Type type = n.e.accept(this);
        if (type != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e.lineNumber, n.e.columnNumber, "Cannot assign non-integral rvalue to integer array: %s",
                    n.e);
        }

        // Assignment evaluates to rvalue
        return type;
    }

    /**
     * Visit logical AND expression
     */
    @Override
    public Type visit(final And n) {
        // Left-hand-side of expression should be a boolean value
        if (n.e1.accept(this) != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.e1.lineNumber, n.e1.columnNumber, "Logical AND LHS is not boolean: %s", n.e1);
        }

        // Right-hand-side of expression should be a boolean value
        if (n.e2.accept(this) != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.e2.lineNumber, n.e2.columnNumber, "Logical AND RHS is not boolean: %s", n.e2);
        }

        // Expression result will be True/False
        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit less-than expression
     */
    @Override
    public Type visit(final LessThan n) {
        // Left-hand-side of expression should be an integer value
        if (n.e1.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e1.lineNumber, n.e1.columnNumber, "Less-than LHS is not integral: %s", n.e1);
        }

        // Right-hand-side of expression should be an integer value
        if (n.e2.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e2.lineNumber, n.e2.columnNumber, "Less-than RHS is not integral: %s", n.e2);
        }

        // Expression result will be True/False
        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit plus expression
     */
    @Override
    public Type visit(final Plus n) {
        // Left-hand-side of expression should be an integer value
        if (n.e1.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e1.lineNumber, n.e1.columnNumber, "Addition LHS is not integral: %s", n.e1);
        }

        // Right-hand-side of expression should be an integer value
        if (n.e2.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e2.lineNumber, n.e2.columnNumber, "Addition RHS is not integral: %s", n.e2);
        }

        // Expression result will be an integer
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit minus expression
     */
    @Override
    public Type visit(final Minus n) {
        // Left-hand-side of expression should be an integer value
        if (n.e1.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e1.lineNumber, n.e1.columnNumber, "Subraction LHS is not integral: %s", n.e1);
        }

        // Right-hand-side of expression should be an integer value
        if (n.e2.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e2.lineNumber, n.e2.columnNumber, "Subtraction RHS is not integral: %s", n.e2);
        }

        // Expression result will be an integer
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit times expression
     */
    @Override
    public Type visit(final Times n) {
        // Left-hand-side of expression should be an integer value
        if (n.e1.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e1.lineNumber, n.e1.columnNumber, "Multiplication LHS is not integral: %s", n.e1);
        }

        // Right-hand-side of expression should be an integer value
        if (n.e2.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e2.lineNumber, n.e2.columnNumber, "Multiplication RHS is not integral: %s", n.e2);
        }

        // Expression result will be an integer
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit array lookup expression
     */
    @Override
    public Type visit(final ArrayLookup n) {
        // Can only perform array lookup on an array
        if (n.expressionForArray.accept(this) != Type.THE_INT_ARRAY_TYPE) {
            Logger.addError(n.expressionForArray.lineNumber, n.expressionForArray.columnNumber,
                    "Cannot perform array lookup on non-array object: %s", n.expressionForArray);
        }

        // Can only index array using integer value
        if (n.indexInArray.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.indexInArray.lineNumber, n.indexInArray.columnNumber,
                    "Cannot index array by non-integer value: %s", n.indexInArray);
        }

        // Lookup result will be an integer
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit array length expression
     */
    @Override
    public Type visit(final ArrayLength n) {
        // Can only call length method on array
        if (n.expressionForArray.accept(this) != Type.THE_INT_ARRAY_TYPE) {
            Logger.addError(n.expressionForArray.lineNumber, n.expressionForArray.columnNumber,
                    "Cannot call length method on non-array object: %s", n.expressionForArray);
        }

        // Array length is an integer
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit method call expression
     */
    @Override
    public Type visit(final Call n) {
        final String funcName = n.i.s;
        ClassSymbol cls = null;
        String className = null;
        MethodSymbol sym = null;

        /**
         * Callee is this object
         */
        if (n.e instanceof This) {
            // Don't adjust the class name.
            // The method will be in the current scope
            cls = m_symbolTable.currentClass();
            sym = cls.getMethod(funcName);
        }
        /**
         * Callee is newly allocated object
         */
        else if (n.e instanceof NewObject) {
            // Change class name (scope) to class of object
            final NewObject callee = (NewObject) n.e;
            className = callee.i.s;

            // Search class for method
            cls = m_symbolTable.getClass(className);
            sym = cls.getMethod(funcName);
        }
        /**
         * Callee is a nested call's return value
         */
        else if (n.e instanceof Call) {
            // Get class name of return type
            final Type nestedType = n.e.accept(this);
            assert nestedType instanceof IdentifierType;
            className = ((IdentifierType) nestedType).nameOfType;

            // Search class for method
            cls = m_symbolTable.getClass(className);
            sym = cls.getMethod(funcName);
        }
        /**
         * Callee is some variable
         */
        else if (n.e instanceof IdentifierExp) {
            // Get object symbol
            final IdentifierExp ie = (IdentifierExp) n.e;
            VarSymbol vs = m_symbolTable.getVar(ie.s);

            // Couldn't find field in the current scope
            if (vs == null) {
                // Check the current class fields
                final ClassSymbol thisClass = m_symbolTable.currentClass();
                // Is this a member access (with "this." omitted)?
                if (thisClass != null) {
                    // Check self members
                    vs = thisClass.getVar(ie.s);
                }
            }

            // Did we find the callee's symbol?
            if (vs == null) {
                Logger.addError(n.lineNumber, n.columnNumber, "Attempt to call method %s from undeclared object %s",
                        funcName, ie.s);
                return Type.THE_VOID_TYPE;
            }

            // Make sure the callee is not a primitive type
            if (CheckUtil.typeIsPrim(vs.type)) {
                Logger.addError(n.lineNumber, n.columnNumber, "Attempt to call method %s from primitive type %s",
                        funcName, vs.type);
                return Type.THE_VOID_TYPE;
            }

            // Get method from class
            final String objClass = vs.type.getName();
            sym = m_symbolTable.getMethodEx(funcName, objClass, false);
        }
        /**
         * Don't know what the callee is, but we know it isn't an object
         */
        else {
            Logger.addError(n.lineNumber, n.columnNumber, "Attempt to call method %s from non-object", funcName);
            return Type.THE_VOID_TYPE;
        }

        // Pretty name for errors down the line
        final String prettyFunc = String.format("%s.%s", className != null ? className : "this", funcName);

        // Couldn't find the method symbol in any class
        if (sym == null) {
            Logger.addError(n.lineNumber, n.columnNumber, "Attempt to call undeclared method: %s", prettyFunc);
            return Type.THE_VOID_TYPE;
        }

        // Count arguments in call and declaration
        final int expectedArgc = sym.formals.size();
        final int gotArgc = n.el.size();

        // Check argument count
        if (expectedArgc != gotArgc) {
            Logger.addError(n.lineNumber, n.columnNumber, "Method %s expects %d arguments, got %d", prettyFunc,
                    expectedArgc, gotArgc);
        } else {
            // Check argument types
            for (int i = 0; i < expectedArgc; i++) {
                // Get type in call and declaration
                final Type expectedType = sym.getFormal(i).type;
                final Type gotType = n.el.get(i).accept(this);

                // Check that types are equal (or the argument can be upcasted to the formal's
                // type)
                if (!CheckUtil.typeCanUpCast(gotType, expectedType, m_symbolTable)) {
                    Logger.addError(n.lineNumber, n.columnNumber,
                            "Cannot convert argument %s (arg #%d) from type %s to type %s",
                            sym.getFormal(i).name, i + 1, gotType, expectedType);
                }
            }
        }

        // Evaluate to method return type
        return sym.type;
    }

    /**
     * Visit integer literal
     */
    @Override
    public Type visit(final IntegerLiteral n) {
        return Type.THE_INTEGER_TYPE;
    }

    /**
     * Visit boolean True
     */
    @Override
    public Type visit(final True n) {
        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit boolean False
     */
    @Override
    public Type visit(final False n) {
        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit identifier expression
     */
    @Override
    public Type visit(final IdentifierExp n) {
        // Convert to identifier to avoid repeating code
        final Identifier id = new Identifier(n.lineNumber, n.columnNumber, n.s);
        return id.accept(this);
    }

    /**
     * Visit this
     */
    @Override
    public Type visit(final This n) {
        final ClassSymbol thisClass = m_symbolTable.currentClass();
        return new IdentifierType(n.lineNumber, n.columnNumber, thisClass.name);
    }

    /**
     * Visit array allocation
     */
    @Override
    public Type visit(final NewArray n) {
        // Check array dimension type
        if (n.e.accept(this) != Type.THE_INTEGER_TYPE) {
            Logger.addError(n.e.lineNumber, n.e.columnNumber, "Array dimension specified using a non-integer value: %s",
                    n.e);
        }

        return Type.THE_INT_ARRAY_TYPE;
    }

    /**
     * Visit object/record allocation
     */
    @Override
    public Type visit(final NewObject n) {
        // Query symbol table
        final ClassSymbol sym = m_symbolTable.getClass(n.i.s);

        // Check that the identifier is valid
        if (sym == null) {
            Logger.addError(n.lineNumber, n.columnNumber, "Attempt to construct undeclared class %s", n.i.s);
            return Type.THE_VOID_TYPE;
        }

        // Return identifier type
        return new IdentifierType(n.lineNumber, n.columnNumber, sym.name);
    }

    /**
     * Visit logical NOT expression
     */
    @Override
    public Type visit(final Not n) {
        if (n.e.accept(this) != Type.THE_BOOLEAN_TYPE) {
            Logger.addError(n.lineNumber, n.columnNumber, "Attempt to use boolean NOT operator on non-boolean value");
        }

        return Type.THE_BOOLEAN_TYPE;
    }

    /**
     * Visit identifier
     */
    @Override
    public Type visit(final Identifier n) {
        // Search current scope (function locals/formals)
        VarSymbol sym = m_symbolTable.getVar(n.s);

        // Couldn't find member
        if (sym == null) {
            // Check the current class fields
            final ClassSymbol thisClass = m_symbolTable.currentClass();
            // Is this a member access (with "this." omitted)?
            if (thisClass != null) {
                // Check self members
                sym = thisClass.getVar(n.s);
            }
        }

        // Check that the identifier is valid
        if (sym == null) {
            Logger.addError(n.lineNumber, n.columnNumber, "Use of undeclared/invisible identifier %s", n.s);
            return Type.THE_VOID_TYPE;
        }

        // Return identifier type
        return sym.type;
    }
}
