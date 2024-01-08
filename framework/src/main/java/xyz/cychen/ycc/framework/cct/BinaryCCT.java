package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.formula.BinaryFormula;

import java.util.ArrayList;
import java.util.List;

public abstract class BinaryCCT extends CCT {
    protected CCT left;
    protected CCT right;

    public BinaryCCT(BinaryFormula formula, CCT left, CCT right) {
        super(formula, false);
        this.left = left;
        this.right = right;
    }

    @Override
    public List<Arrow> getChildren() {
        List<Arrow> result = new ArrayList<>(2);
        result.add(0, NormalArrow.of(left));
        result.add(1, NormalArrow.of(right));
        return result;
    }
}
