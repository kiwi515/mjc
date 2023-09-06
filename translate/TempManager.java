/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import java.util.List;
import java.util.ArrayList;

import tree.*;
import main.Logger;
import main.Util;

/**
 * Function-local temp manager
 */
public final class TempManager {
    // Current temp unique ID
    private int m_numTemp = 0;
    // All temps owned by this manager
    private ArrayList<TEMP> m_children = new ArrayList<>();

    /**
     * Create unique IR temp with prefix(es)
     * 
     * @param args Prefix tokens
     */
    public TEMP create(final String... args) {
        if (m_numTemp > 9999) {
            Logger.addError("Common error: Ran out of unique temps (>9999)");
        }

        // Create unique identifier
        final int no = m_numTemp++;
        final String name = String.format("%s%04d",
                Util.concatNames(args), no);

        // Track all owned temps for future use
        final TEMP t = new TEMP(name);
        m_children.add(t);

        return t;
    }

    /**
     * Create unique IR temp with no prefix
     */
    public TEMP create() {
        return create("t");
    }

    /**
     * Access all temps owned by this manager
     */
    public List<TEMP> children() {
        return m_children;
    }

    /**
     * Check if temp is owned by this manager
     */
    public boolean isChild(final TEMP t) {
        final String targetName = t.temp.toString();

        for (final TEMP child : m_children) {
            final String childName = child.temp.toString();
            if (childName.equals(targetName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if temp is owned by this manager
     */
    public boolean isChild(final NameOfTemp t) {
        return isChild(new TEMP(t));
    }

    /**
     * Add child to this manager
     */
    public void addChild(final TEMP t) {
        m_children.add(t);
    }

    /**
     * Add child to this manager
     */
    public void addChild(final NameOfTemp t) {
        m_children.add(new TEMP(t));
    }

    /**
     * Clear all unique temps
     */
    public void reset() {
        m_numTemp = 0;
        m_children.clear();
    }
}
