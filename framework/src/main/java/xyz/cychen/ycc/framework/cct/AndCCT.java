package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.AndFormula;

public class AndCCT extends BinaryCCT{
    public AndCCT(AndFormula formula, CCT left, CCT right) {
        super(formula, left, right);
    }
}
