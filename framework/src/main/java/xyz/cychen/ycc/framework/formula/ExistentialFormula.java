package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;

import java.util.Arrays;

public class ExistentialFormula extends QuantifiedFormula{
    public ExistentialFormula(Variable variable, ContextSet universe, Formula sub) {
        super(variable, universe, sub);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(
                new int[]{
                        FormulaType.EXISTENTIAL.ordinal(),
                        variable.hashCode(),
                        universe.hashCode(),
                        sub.hashCode()
                }
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExistentialFormula existentialFormula = (ExistentialFormula) obj;
        return this.variable.equals(existentialFormula.variable)
                && this.universe.equals(existentialFormula.universe)
                && this.sub.equals(existentialFormula.sub);
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new ExistentialFormula(this.variable.clone(), this.universe, this.sub.clone());
    }
}
