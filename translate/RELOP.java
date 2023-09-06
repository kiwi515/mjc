/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package translate;

import tree.*;

/**
 * CJUMP extends Stm, but we need to generate an Exp from the LessThan node.
 * 
 * RELOP contains everything in CJUMP that the LessThan node provides, allowing
 * for IRExpressionVisitor to return this type.
 */
public final class RELOP extends Exp {
    public final int oper;
    public final Exp lhs, rhs;

    public RELOP(final int oper, final Exp lhs, final Exp rhs) {
        this.oper = oper;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * CJUMP helper
     */
    public Stm asCond(final LABEL t, final LABEL f) {
        return new CJUMP(oper, lhs, rhs, t.label, f.label);
    }

    /**
     * CJUMP helper
     */
    public Exp asExp() {
        final LABEL t = Phase.getLabelMgr().create("if", "then");
        final LABEL f = Phase.getLabelMgr().create("if", "else");
        final LABEL join = Phase.getLabelMgr().create("if", "end");
        final TEMP tmp = Phase.getCurrTempMgr().create();

        /**
         * tmp = (lhs && rhs) ? 1 : 0
         */
        final Stm stm = TranslateUtil.joinFragments(
                asCond(t, f),
                t,
                new MOVE(tmp, CONST.TRUE),
                new JUMP(join.label),
                f,
                new MOVE(tmp, CONST.FALSE),
                join);

        // Expression evaluates to 0/1
        return new RET(stm, tmp);
    }
}
