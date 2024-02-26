package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;

import java.util.Arrays;

public class UniversalFormula extends QuantifiedFormula{
    public UniversalFormula(Variable variable, ContextSet universe, Formula sub) {
        super(variable, universe, sub);
    }

    @Override
    public void analyzeEConditions() {
        sub.analyzeEConditions();
        posConditions = sub.posConditions;
        negConditions = sub.negConditions;
        posConditions.add(false, universe.getId());
        negConditions.add(true, universe.getId());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(
            new int[]{
                    FormulaType.UNIVERSAL.ordinal(),
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
        UniversalFormula universalFormula = (UniversalFormula) obj;
        return this.variable.equals(universalFormula.variable)
                && this.universe.equals(universalFormula.universe)
                && this.sub.equals(universalFormula.sub);
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new UniversalFormula(this.variable.clone(), this.universe, this.sub.clone());
    }
}
