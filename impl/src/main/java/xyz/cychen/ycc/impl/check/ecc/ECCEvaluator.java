package xyz.cychen.ycc.impl.check.ecc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;

import java.util.List;

public class ECCEvaluator extends Evaluator {
    private Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct, Binding binding) {
        List<CCT.Arrow> arrows = cct.getChildren();
        boolean left = evaluate(arrows.get(0).getCCT(), binding);
        boolean right = evaluate(arrows.get(1).getCCT(), binding);
        return Pair.with(left, right);
    }

    private boolean notHelper(NotCCT cct, Binding binding) {
        return evaluate(cct.getChildren().get(0).getCCT(), binding);
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        var sub = binaryHelper(cct, binding);
        boolean result = sub.getValue0() && sub.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(OrCCT cct, Binding binding) {
        var sub = binaryHelper(cct, binding);
        boolean result = sub.getValue0() || sub.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        var sub = binaryHelper(cct, binding);
        boolean result = (!sub.getValue0()) || sub.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(NotCCT cct, Binding binding) {
        boolean sub = notHelper(cct, binding);
        boolean result = !sub;
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(UniversalCCT cct, Binding binding) {
        boolean result = true;
        UniversalFormula f = (UniversalFormula) cct.getFormula();
        List<CCT.Arrow> arrows = cct.getChildren();
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue = evaluate(arrow.getCCT(), binding);
            binding.unbind(f.getVariable());
            result = result && truthValue;
        }
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(ExistentialCCT cct, Binding binding) {
        boolean result = false;
        ExistentialFormula f = (ExistentialFormula) cct.getFormula();
        List<CCT.Arrow> arrows = cct.getChildren();
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue = evaluate(arrow.getCCT(), binding);
            binding.unbind(f.getVariable());
            result = result || truthValue;
        }
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(BFuncCCT cct, Binding binding) {
        cct.bind(binding);
        boolean result = cct.evaluate();
        cct.setTV(result);
        return result;
    }
}
