package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.NotFormula;

import java.util.ArrayList;
import java.util.List;

public class NotCCT extends CCT {
    protected CCT sub;

    public NotCCT(NotFormula formula, CCT sub) {
        super(formula, false);
        this.sub = sub;
    }

    @Override
    public List<Arrow> getChildren() {
        List<Arrow> result = new ArrayList<>(1);
        result.add(0, NormalArrow.of(sub));
        return result;
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
        sb.append("not");
        sb.append("\"");
        sb.append(" ");
        sb.append(sub.toStringRepr(tagSCCT));
        sb.append(")");
        return sb.toString();
    }
}
