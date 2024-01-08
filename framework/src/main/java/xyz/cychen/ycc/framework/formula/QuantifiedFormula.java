package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;

public abstract class QuantifiedFormula extends Formula {
    protected final Variable variable;
    protected final ContextSet universe;
    protected final Formula sub;

    public QuantifiedFormula(Variable variable, ContextSet universe, Formula sub) {
        this.variable = variable;
        this.universe = universe;
        this.sub = sub;
    }

    @Override
    public Formula[] getChildren() {
        return new Formula[]{sub};
    }

    public ContextSet getUniverse() {
        return universe;
    }

    public Variable getVariable() {
        return variable;
    }
}
