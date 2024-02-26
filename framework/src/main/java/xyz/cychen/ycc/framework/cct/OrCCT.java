package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.OrFormula;

public class OrCCT extends BinaryCCT{
    public OrCCT(OrFormula formula, CCT left, CCT right) {
        super(formula, left, right);
    }
}
