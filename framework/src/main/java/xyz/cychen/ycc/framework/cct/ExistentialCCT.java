package xyz.cychen.ycc.framework.cct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;

import java.util.List;

public class ExistentialCCT extends QuantifiedCCT{
    public ExistentialCCT(ExistentialFormula formula, List<Pair<Context, CCT>> children) {
        super(formula, false, children);
    }
}
