package xyz.cychen.ycc.framework.formula;

import java.util.Arrays;

public class NotFormula extends Formula {
    protected Formula sub;

    public NotFormula(Formula sub) {
        this.sub = sub;
    }

    @Override
    public Formula[] getChildren() {
        return new Formula[]{sub};
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(
                new int[]{FormulaType.NOT.ordinal(), sub.hashCode()}
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotFormula notFormula = (NotFormula) obj;
        return notFormula.sub.equals(sub);
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new NotFormula(sub.clone());
    }
}
