package xyz.cychen.ycc.framework.formula;

import java.util.Arrays;

public class AndFormula extends BinaryFormula{
    public AndFormula(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public void analyzeEConditions() {
        left.analyzeEConditions();
        right.analyzeEConditions();
        left.posConditions.linkTo(right.negConditions);
        right.posConditions.linkTo(left.negConditions);
        posConditions = left.posConditions.union(right.posConditions);
        negConditions = left.negConditions.union(right.negConditions);
    }

    @Override
    public int hashCode() {
        int l = left.hashCode();
        int r = right.hashCode();
        int[] tuple;
        if (l <= r) {
            tuple = new int[]{FormulaType.AND.ordinal(), l, r};
        }
        else {
            tuple = new int[]{FormulaType.AND.ordinal(), r, l};
        }
        return Arrays.hashCode(tuple);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AndFormula andFormula = (AndFormula) obj;
        return (andFormula.left.equals(left) && andFormula.right.equals(right))
                || (andFormula.left.equals(right) && andFormula.right.equals(left));
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new AndFormula(left.clone(), right.clone());
    }
}
