package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.ImpliesFormula;

public class ImpliesCCT extends BinaryCCT{
    public ImpliesCCT(ImpliesFormula formula, CCT left, CCT right) {
        super(formula, left, right);
    }
}
