package xyz.cychen.ycc.impl.check.pcctotalgen;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.impl.check.occ.OCCGenerator;

import java.util.List;
import java.util.function.BiFunction;

public class OCCGeneratorForPCC extends OCCGenerator {
    @Override
    public Link generate(CCT cct, Binding binding) {
        Link r = super.generate(cct, binding);
//        setIncrementalLinkCount(cct, r.size());
        return r;
    }
}
