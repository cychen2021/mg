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
}
