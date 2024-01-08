package xyz.cychen.ycc.framework.formula;

import java.util.Arrays;

public class ImpliesFormula extends BinaryFormula{
    public ImpliesFormula(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public int hashCode() {
        int l = left.hashCode();
        int r = right.hashCode();
        return Arrays.hashCode(
                new int[]{FormulaType.IMPLIES.ordinal(), l, r}
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ImpliesFormula impliesFormula = (ImpliesFormula) obj;
        return impliesFormula.left.equals(left)
                && impliesFormula.right.equals(right);
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new ImpliesFormula(left.clone(), right.clone());
    }
}
