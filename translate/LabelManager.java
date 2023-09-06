/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import tree.*;
import main.Logger;
import main.Util;

/**
 * Function-local label manager
 */
public final class LabelManager {
    // Current label unique ID
    private int m_numLabel = 0;

    /**
     * Create unique IR label with prefix(es)
     * 
     * @param args Prefix tokens
     */
    public LABEL create(final String... args) {
        if (m_numLabel > 9999) {
            Logger.addError("Common error: Ran out of unique labels (>9999)");
        }

        // Create unique identifier
        final int no = m_numLabel++;
        final String name = String.format("%s%04d",
                Util.concatNames(args), no);

        return new LABEL(name);
    }

    /**
     * Create unique IR label with no prefix
     */
    public LABEL create() {
        return create("lbl");
    }

    /**
     * Clear all unique labels
     */
    public void reset() {
        m_numLabel = 0;
    }
}
