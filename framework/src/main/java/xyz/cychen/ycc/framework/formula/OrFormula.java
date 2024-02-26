package xyz.cychen.ycc.framework.formula;

import java.util.Arrays;

public class OrFormula extends BinaryFormula {
    public OrFormula(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public void analyzeEConditions() {
        left.analyzeEConditions();
        right.analyzeEConditions();
        left.negConditions.linkTo(right.posConditions);
        right.negConditions.linkTo(left.posConditions);
        posConditions = left.posConditions.union(right.posConditions);
        negConditions = left.negConditions.union(right.negConditions);
    }

    @Override
    public int hashCode() {
        int l = left.hashCode();
        int r = right.hashCode();
        int[] tuple;
        if (l <= r) {
            tuple = new int[]{FormulaType.OR.ordinal(), l, r};
        }
        else {
            tuple = new int[]{FormulaType.OR.ordinal(), r, l};
        }
        return Arrays.hashCode(tuple);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrFormula orFormula = (OrFormula) obj;
        return ( orFormula.left.equals(left) &&  orFormula.right.equals(right))
                || ( orFormula.left.equals(right) &&  orFormula.right.equals(left));
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new OrFormula(left.clone(), right.clone());
    }
}
