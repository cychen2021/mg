package xyz.cychen.ycc.framework.cct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.formula.UniversalFormula;

import java.util.List;

public class UniversalCCT extends QuantifiedCCT{
    public UniversalCCT(UniversalFormula formula, List<Pair<Context, CCT>> children) {
        super(formula, true, children);
    }
}
