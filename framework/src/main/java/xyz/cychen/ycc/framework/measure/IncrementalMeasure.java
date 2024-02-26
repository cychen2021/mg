package xyz.cychen.ycc.framework.measure;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.*;

public class IncrementalMeasure extends Measure {
    @Override
    public Statistics measure(boolean constraint, CCT cct) {
        var r = super.measure(constraint, cct);
        clearIncrementalCount(cct);
        return r;
    }

    protected void clearIncrementalCount(CCT cct) {
        for (var a: cct.getChildren()) {
            clearIncrementalCount(a.getCCT());
        }
    }
}