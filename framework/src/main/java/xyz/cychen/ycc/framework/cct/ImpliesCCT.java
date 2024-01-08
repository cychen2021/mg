package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.ImpliesFormula;

public class ImpliesCCT extends BinaryCCT{
    public ImpliesCCT(ImpliesFormula formula, CCT left, CCT right) {
        super(formula, left, right);
    }

    @Override
    public String toStringRepr(boolean tagSCCT) {
        var sb = new StringBuilder();
        sb.append("(");
        sb.append("\"");
        if (tagSCCT && this.isSCCT()) {
            sb.append("SCCT ");
        }
        if (this.tv) {
            sb.append("T ");
        } else {
            sb.append("F ");
        }
        sb.append("implies");
        sb.append("\"");
        sb.append(" ");
        sb.append(left.toStringRepr(tagSCCT));
        sb.append(" ");
        sb.append(right.toStringRepr(tagSCCT));
        sb.append(")");
        return sb.toString();
    }
}
