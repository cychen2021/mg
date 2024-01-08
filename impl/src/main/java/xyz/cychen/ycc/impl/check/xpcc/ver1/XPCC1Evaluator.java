package xyz.cychen.ycc.impl.check.xpcc.ver1;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

public class XPCC1Evaluator extends Evaluator {
//    private boolean isAffected(CCT cct) {
//        return !cct.containsProperty(CCT.PROP_TV) || (boolean) cct.getFormula().getProperty(Formula.PROP_AF);
//    }
//
    protected Checker.Change currentChange = null;

    public void setChange(Checker.Change change) {
        this.currentChange = change;
    }

    @Override
    public boolean evaluate(CCT cct, Binding binding) {
        if (!cct.isAffected() && !cct.tvIsNull()) {
            return cct.getTV();
        }

        return super.evaluate(cct, binding);
    }

    protected Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct, Binding binding) {
        CCT left = cct.getChildren().get(0).getCCT();
        CCT right = cct.getChildren().get(1).getCCT();
        boolean ltv = evaluate(left, binding);
        boolean rtv = evaluate(right, binding);
        return Pair.with(ltv, rtv);
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        var tmp = binaryHelper(cct, binding);
        var result = tmp.getValue0() && tmp.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(OrCCT cct, Binding binding) {
        var tmp = binaryHelper(cct, binding);
        var result = tmp.getValue0() || tmp.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        var tmp = binaryHelper(cct, binding);
        var result = (!tmp.getValue0()) || tmp.getValue1();
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(NotCCT cct, Binding binding) {
        CCT sub = cct.getChildren().get(0).getCCT();
        var subr = evaluate(sub, binding);
        var result = !subr;
        cct.setTV(result);
        return result;
    }

    @Override
    public boolean visit(UniversalCCT cct, Binding binding) {
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        ContextSet universe = f.getUniverse();
        Variable v = f.getVariable();
        if (currentChange.getTargetSet().equals(universe.getId()) && currentChange instanceof Checker.AddChange) {
            binding.bind(v, currentChange.getContext());
            boolean result = evaluate(cct.getChild(currentChange.getContext()), binding);
            binding.unbind(v);
            boolean old = cct.getTV();
            result = result && old;
            cct.setTV(result);
            return result;
        }
        else {
            boolean result = true;
            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                binding.bind(v, qa.getContext());
                boolean sub = evaluate(qa.getCCT(), binding);
                binding.unbind(v);
                result = result && sub;
            }
            cct.setTV(result);
            return result;
        }
    }

    @Override
    public boolean visit(ExistentialCCT cct, Binding binding) {
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        ContextSet universe = f.getUniverse();
        Variable v = f.getVariable();
        if (currentChange.getTargetSet().equals(universe.getId()) && currentChange instanceof Checker.AddChange) {
            binding.bind(v, currentChange.getContext());
            boolean result = evaluate(cct.getChild(currentChange.getContext()), binding);
            binding.unbind(v);
            boolean old = cct.getTV();
            result = result || old;
            cct.setTV(result);
            return result;
        }
        else {
            boolean result = false;
            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                binding.bind(v, qa.getContext());
                boolean sub = evaluate(qa.getCCT(), binding);
                binding.unbind(v);
                result = result || sub;
            }
            cct.setTV(result);
            return result;
        }
    }

    @Override
    public boolean visit(BFuncCCT cct, Binding binding) {
        BFuncFormula f = (BFuncFormula) cct.getFormula();
        f.bind(binding);
        boolean r = f.evaluate();
        f.unbind();
        cct.setTV(r);
        return r;
    }
}
