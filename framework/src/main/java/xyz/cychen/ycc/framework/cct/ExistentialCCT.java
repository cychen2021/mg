package xyz.cychen.ycc.framework.cct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;

import java.util.List;

public class ExistentialCCT extends QuantifiedCCT{
    public ExistentialCCT(ExistentialFormula formula, List<Pair<Context, CCT>> children) {
        super(formula, false, children);
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
        sb.append("exists ");
        var f = (ExistentialFormula) this.formula;
        var varName = f.getVariable().variable();
        sb.append(varName);
        sb.append(" ");
        sb.append(f.getUniverse().getId());
        sb.append("\"");
        for (var kv : children.entrySet()) {
            var k = kv.getKey();
            var v = kv.getValue();
            sb.append(" \"");
            sb.append(varName);
            sb.append("=");
            sb.append(k.getId());
            sb.append("\"@");
            sb.append(v.toStringRepr(tagSCCT));
        }
        sb.append(")");
        return sb.toString();
    }
}
