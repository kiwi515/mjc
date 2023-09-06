/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package check;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Symbol table for enabling semantic checks
 */
public final class SymbolTable {
    // Current scope
    private LinkedList<String> m_scopeStack = new LinkedList<>();
    // Symbol table
    private HashMap<String, BaseSymbol> m_symbolMap = new HashMap<>();

    /**
     * Build a string representing the current scope.
     * Everything is concatenated with a period (".")
     */
    public String getScope() {
        return String.join(".", m_scopeStack);
    }

    /**
     * Query symbol table
     * 
     * @note Internal implementation
     * @param name      Symbol name
     * @param scope     Symbol scope
     * @param recursive Recursively search outer scopes
     */
    private BaseSymbol getSymbol(final String name, final String scope, final boolean recursive) {
        // Key is scope concatenated with name ("full" name)
        final boolean globalScope = scope == null || scope.isEmpty();
        final String fullName = globalScope ? name : String.join(".", scope, name);

        // Search current scope
        final BaseSymbol sym = m_symbolMap.get(fullName);
        if (sym != null) {
            // Found it!
            return sym;
        }

        // Doesn't exist in this scope. Do we search the outer scope?
        if (recursive && scope != null) {
            final int scopeIndex = scope.lastIndexOf(".");
            if (scopeIndex == -1) {
                // No more outer scope layers
                return null;
            }

            return getSymbol(name, scope.substring(0, scopeIndex), true);
        }

        // Nothing we can do
        return null;
    }

    /**
     * Query symbol table for variable
     * 
     * @param name Variable name
     */
    public VarSymbol getVar(final String name) {
        return getVarEx(name, getScope(), false);
    }

    /**
     * Query symbol table for variable
     * 
     * @param name      Variable name
     * @param scope     Variable scope
     * @param recursive Recursively search outer scopes
     */
    public VarSymbol getVarEx(final String name, final String scope, final boolean recursive) {
        final BaseSymbol sym = getSymbol(name, scope, recursive);
        if (sym == null) {
            return null;
        }

        assert sym instanceof VarSymbol;
        return (VarSymbol) sym;
    }

    /**
     * Query symbol table for method
     * 
     * @param name Method name
     */
    public MethodSymbol getMethod(final String name) {
        return getMethodEx(name, getScope(), false);
    }

    /**
     * Query symbol table for method
     * 
     * @param name      Method name
     * @param scope     Method scope
     * @param recursive Recursively search outer scopes
     */
    public MethodSymbol getMethodEx(final String name, final String scope, final boolean recursive) {
        final BaseSymbol sym = getSymbol(name, scope, recursive);
        if (sym == null) {
            return null;
        }

        assert sym instanceof MethodSymbol;
        return (MethodSymbol) sym;
    }

    /**
     * Query symbol table for class
     * 
     * @param name Class name
     */
    public ClassSymbol getClass(final String name) {
        return getClassEx(name, null, false);
    }

    /**
     * Query symbol table for class
     * 
     * @param name      Class name
     * @param scope     Class scope
     * @param recursive Recursively search outer scopes
     */
    public ClassSymbol getClassEx(final String name, final String scope, final boolean recursive) {
        final BaseSymbol sym = getSymbol(name, scope, recursive);
        if (sym == null) {
            return null;
        }

        assert sym instanceof ClassSymbol;
        return (ClassSymbol) sym;
    }

    /**
     * Add/update symbol
     * 
     * @param sym Symbol
     * @return Old symbol (if exists)
     */
    private BaseSymbol putSymbol(final BaseSymbol sym) {
        final String name = sym.name;
        final String scope = getScope();

        // Old value (NOT recursive to avoid updating values in the wrong scope)
        final BaseSymbol old = getSymbol(name, scope, false);

        // Keys in the hashmap are the scope + name
        final boolean globalScope = scope == null || scope.isEmpty();
        final String fullName = globalScope ? name : String.join(".", scope, name);
        m_symbolMap.put(fullName, sym);

        return old;
    }

    /**
     * Add/update variable symbol
     * 
     * @param vs Variable
     * @return Old value (if exists)
     */
    public VarSymbol putVar(final VarSymbol vs) {
        if (vs == null) {
            return null;
        }

        final BaseSymbol sym = putSymbol(vs);
        if (sym == null) {
            return null;
        }

        assert sym instanceof VarSymbol;
        return (VarSymbol) sym;
    }

    /**
     * Add/update variable symbol in the current class
     * 
     * @param vs Variable
     * @return Old value (if exists)
     */
    public VarSymbol putClassVar(final VarSymbol vs) {
        if (vs == null) {
            return null;
        }

        // Tie to parent class (if applicable)
        if (scopeIsClass()) {
            currentClass().fields.add(vs);
        }

        return putVar(vs);
    }

    /**
     * Add/update method symbol
     * 
     * @param ms Method
     * @return Old value (if exists)
     */
    public MethodSymbol putMethod(final MethodSymbol ms) {
        if (ms == null) {
            return null;
        }

        // Tie to parent class (if applicable)
        if (scopeIsClass()) {
            currentClass().methods.put(ms.name, ms);
            ms.parent = currentClass();
        }

        final BaseSymbol sym = putSymbol(ms);
        if (sym == null) {
            return null;
        }

        assert sym instanceof MethodSymbol;
        return (MethodSymbol) sym;
    }

    /**
     * Add/update class symbol
     * 
     * @param cs Class
     * @return Old value (if exists)
     */
    public ClassSymbol putClass(final ClassSymbol cs) {
        final BaseSymbol sym = putSymbol(cs);
        if (sym == null) {
            return null;
        }

        // MiniJava does not allow nested classes
        assert !scopeIsClass() : "Can't nest classes in MiniJava!";

        assert sym instanceof ClassSymbol;
        return (ClassSymbol) sym;
    }

    /**
     * Add/enter scope level
     */
    public void enterScope(final String scope) {
        m_scopeStack.addLast(scope);
    }

    /**
     * Remove/exit scope level
     */
    public String exitScope() {
        return m_scopeStack.removeLast();
    }

    /**
     * Reset/clear scope level
     */
    public void resetScope() {
        m_scopeStack.clear();
    }

    /**
     * Apply current scope to symbol
     * 
     * @param name Symbol name
     */
    public String applyScope(final String name) {
        return String.join(".", getScope(), name);
    }

    /**
     * Check whether the current scope is a class
     */
    public boolean scopeIsClass() {
        return currentClass() != null;
    }

    /**
     * Get current class from scope
     */
    public ClassSymbol currentClass() {
        // Current scope, will be local to the function
        String name = getScope();

        // Pop one level of scope to get the class name
        final int scopeIndex = name.lastIndexOf(".");
        if (scopeIndex != -1) {
            name = name.substring(0, scopeIndex);

            // Remove leading scope levels
            final int start = name.lastIndexOf(".");
            if (start != -1) {
                name = name.substring(start);
            }
        }

        return getClass(name);
    }

    /**
     * Get current method from scope
     */
    public MethodSymbol currentMethod() {
        if (!scopeIsClass()) {
            return null;
        }

        // Current scope, will be local to the function
        String method = getScope();

        // Isolate the highest level of scope to get the method name
        final int scopeIndex = method.lastIndexOf(".");
        if (scopeIndex == -1) {
            return null;
        }
        method = method.substring(scopeIndex + 1);

        // Search current class for method
        return currentClass().getMethod(method);
    }
}