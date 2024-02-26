package xyz.cychen.ycc.impl.check.exyz;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;

import java.util.List;

public class EXYZEvaluator extends Evaluator {
    private final ECCEvaluator inner = new ECCEvaluator();

    private Checker.Change change;
    private CCT oldCCT = null;

    private CCT ptr = null;

    public void setChange(Checker.Change change) {
        this.change = change;
    }

    public void setOldCCT(CCT oldCCT) {
        this.oldCCT = oldCCT;
        this.ptr = oldCCT;
    }

    public boolean realEvaluate(CCT cct, Binding binding) {
        if (oldCCT == null) {
            return inner.evaluate(cct, binding);
        }
        if (!ptr.isSCCT()) {
            return checkAnchor(ptr, cct, binding);
        }
        return evaluate(cct, binding);
    }

    private boolean checkAnchor(CCT anchor, CCT cct, Binding binding) {
        if (anchor.getEConditions().hit(change)) {
            cct.setTV(anchor.getTV());
            return anchor.getTV();
        } else {
            return inner.evaluate(cct, binding);
        }
    }

    private Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct, Binding binding) {
        List<CCT.Arrow> arrows = cct.getChildren();
//        boolean left = evaluate(arrows.get(0).getCCT(), binding);
//        boolean right = evaluate(arrows.get(1).getCCT(), binding);
        boolean left, right;
        CCT currentPos = ptr;
        CCT oldLeftC = ptr.getChildren().get(0).getCCT();
        if (oldLeftC.isSCCT()) {
            ptr = oldLeftC;
            left = evaluate(arrows.get(0).getCCT(), binding);
            ptr = currentPos;
        } else {
            left = checkAnchor(oldLeftC, arrows.get(0).getCCT(), binding);
        }
        CCT oldRightC = ptr.getChildren().get(1).getCCT();
        if (oldRightC.isSCCT()) {
            ptr = oldRightC;
            right = evaluate(arrows.get(1).getCCT(), binding);
            ptr = currentPos;
        } else {
            right = checkAnchor(oldRightC, arrows.get(1).getCCT(), binding);
        }
        return Pair.with(left, right);
    }

    private boolean notHelper(NotCCT cct, Binding binding) {
        var current = ptr;
        ptr = ptr.getChildren().get(0).getCCT();
        var r = evaluate(cct.getChildren().get(0).getCCT(), binding);
        ptr = current;
        return r;
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
        CCT current = ptr;
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT old = ((UniversalCCT) ptr).getChild(qArrow.getContext());
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue;
            if (old != null) {
                if (old.isSCCT()) {
                    ptr = old;
                    truthValue = evaluate(arrow.getCCT(), binding);
                    ptr = current;
                } else {
                    truthValue = checkAnchor(old, arrow.getCCT(), binding);
                }
            } else {
                 truthValue = inner.evaluate(arrow.getCCT(), binding);
            }
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
        CCT current = ptr;
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT old = ((ExistentialCCT) ptr).getChild(qArrow.getContext());
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue;
            if (old != null) {
                if (old.isSCCT()) {
                    ptr = old;
                    truthValue = evaluate(arrow.getCCT(), binding);
                    ptr = current;
                } else {
                    truthValue = checkAnchor(old, arrow.getCCT(), binding);
                }
            } else {
                truthValue = inner.evaluate(arrow.getCCT(), binding);
            }
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
