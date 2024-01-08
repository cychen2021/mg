package xyz.cychen.ycc.impl.check.ecc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Builder;
import xyz.cychen.ycc.framework.formula.*;

import java.util.ArrayList;
import java.util.List;

public class ECCBuilder extends Builder {
    private Pair<CCT, CCT> binaryHelper(BinaryFormula formula) {
        Formula left = formula.getChildren()[0];
        Formula right = formula.getChildren()[1];
        CCT lc = build(left);
        CCT rc = build(right);
        return Pair.with(lc, rc);
    }

    private CCT notHelper(NotFormula formula) {
        Formula sub = formula.getChildren()[0];
        return build(sub);
    }

    private List<Pair<Context, CCT>> quantifiedHelper(QuantifiedFormula formula) {
        ContextSet universe = formula.getUniverse();
        List<Pair<Context, CCT>> result = new ArrayList<>(universe.size());
        for (Context c: universe) {
            CCT sub = build(formula.getChildren()[0]);
            result.add(Pair.with(c, sub));
        }
        return result;
    }

    @Override
    public AndCCT visit(AndFormula formula) {
        var sub = binaryHelper(formula);
        return new AndCCT(formula, sub.getValue0(), sub.getValue1());
    }

    @Override
    public OrCCT visit(OrFormula formula) {
        var sub = binaryHelper(formula);
        return new OrCCT(formula, sub.getValue0(), sub.getValue1());
    }

    @Override
    public ImpliesCCT visit(ImpliesFormula formula) {
        var sub = binaryHelper(formula);
        return new ImpliesCCT(formula, sub.getValue0(), sub.getValue1());
    }

    @Override
    public NotCCT visit(NotFormula formula) {
        CCT sub = notHelper(formula);
        return new NotCCT(formula, sub);
    }

    @Override
    public UniversalCCT visit(UniversalFormula formula) {
        List<Pair<Context, CCT>> children = quantifiedHelper(formula);
        return new UniversalCCT(formula, children);
    }

    @Override
    public ExistentialCCT visit(ExistentialFormula formula) {
        List<Pair<Context, CCT>> children = quantifiedHelper(formula);
        return new ExistentialCCT(formula, children);
    }

    @Override
    public BFuncCCT visit(BFuncFormula formula) {
        return new BFuncCCT(formula);
    }
}
