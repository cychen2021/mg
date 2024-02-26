package xyz.cychen.ycc.impl.check.epcc.ver4;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

public class EPCC4Evaluator extends Evaluator {
    protected Checker.Change currentChange = null;
    protected Evaluator totalEval;

    int chrono;

    public void setChrono(int chrono) {
        this.chrono = chrono;
    }

    public boolean isAnchor(CCT cct, CCT parent) {
        if (chrono == 0) {
            throw new RuntimeException();
        }

        return cct.getChrono() != chrono - 1 && (parent == null || parent.getChrono() == chrono - 1);
    }

    private CCT parent = null;

    public void reset() {
        parent = null;
    }

    public EPCC4Evaluator(Evaluator totalEval) {
        this.totalEval = totalEval;
    }

    public void setChange(Checker.Change change) {
        this.currentChange = change;
    }

    private boolean isAffected(CCT cct) {
        return cct.getFormula().isAffected();
    }

    private Pair<CCT, CCT> binaryHelper(BinaryCCT cct) {
        var children = cct.getChildren();
        return Pair.with(children.get(0).getCCT(), children.get(1).getCCT());
    }

    public boolean totalEvaluate(CCT cct, Binding binding) {
        return totalEval.evaluate(cct, binding);
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue0(), binding);
            parent = old;
            boolean tv = nsub && children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue1(), binding);
            parent = old;
            boolean tv = nsub && children.getValue0().getTV();
            cct.setTV(tv);
            return tv;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(OrCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue0(), binding);
            parent = old;
            boolean tv = nsub || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue1(), binding);
            parent = old;
            boolean tv = nsub || children.getValue0().getTV();
            cct.setTV(tv);
            return tv;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue0(), binding);
            parent = old;
            boolean tv = (!nsub) || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            CCT old = parent;
            parent = cct;
            boolean nsub = evaluate(children.getValue1(), binding);
            parent = old;
            boolean tv = (!children.getValue0().getTV()) || nsub;
            cct.setTV(tv);
            return tv;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(NotCCT cct, Binding binding) {
        var arrows = cct.getChildren();
        var child = arrows.get(0).getCCT();
        if (!isAffected(child)) {
            return cct.getTV();
        }

        CCT old = parent;
        parent = cct;
        boolean tv = !evaluate(child, binding);
        parent = old;
        cct.setTV(tv);
        return tv;
    }

    @Override
    public boolean visit(UniversalCCT cct, Binding binding) {
        ContextSet universe = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Context ctx = currentChange.getContext();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean thisSet = currentChange.getTargetSet().equals(universe.getId());
        boolean affected = (boolean) cct.getFormula().getChildren()[0].isAffected();
        if (!thisSet && !affected) {
            return cct.getTV();
        }
        else if (thisSet && currentChange instanceof Checker.AddChange && !affected) {
            binding.bind(variable, ctx);
            CCT old = parent;
            parent = cct;
            boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
            parent = old;
            binding.unbind(variable);
            boolean currentTV = cct.getTV();
            if (isAnchor(cct, parent) && cct.getEConditions().hit(currentChange)) {
                return currentTV;
            } else {
                boolean ntv = subtv && currentTV;
                cct.setTV(ntv);
                return ntv;
            }
        }
        else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
            if (isAnchor(cct, parent) && cct.getEConditions().hit(currentChange)) {
                return cct.getTV();
            } else {
                boolean init = true;
                for (var a : cct.getChildren()) {
                    init = init && a.getCCT().getTV();
                }
                cct.setTV(init);
                return init;
            }
        }
        else if (!thisSet && affected) {
            boolean init = true;
            CCT old = parent;
            parent = cct;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV = evaluate(a.getCCT(), binding);
                init = init && subTV;
                binding.unbind(variable);
            }
            parent = old;
            cct.setTV(init);
            return init;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(ExistentialCCT cct, Binding binding) {
        ContextSet universe = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Context ctx = currentChange.getContext();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean thisSet = currentChange.getTargetSet().equals(universe.getId());
        boolean affected = (boolean) cct.getFormula().getChildren()[0].isAffected();
        if (!thisSet && !affected) {
            return cct.getTV();
        }
        else if (thisSet && currentChange instanceof Checker.AddChange && !affected) {
            binding.bind(variable, ctx);
            CCT old = parent;
            parent = cct;
            boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
            parent = old;
            binding.unbind(variable);
            boolean currentTV = cct.getTV();
            if (isAnchor(cct, parent) && cct.getEConditions().hit(currentChange)) {
                return currentTV;
            } else {
                boolean ntv = subtv || currentTV;
                cct.setTV(ntv);
                return ntv;
            }
        }
        else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
            if (isAnchor(cct, parent) && cct.getEConditions().hit(currentChange)) {
                return cct.getTV();
            } else {
                boolean init = false;
                for (var a : cct.getChildren()) {
                    init = init || a.getCCT().getTV();
                }
                cct.setTV(init);
                return init;
            }
        }
        else if (!thisSet && affected) {
            boolean init = false;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV = evaluate(a.getCCT(), binding);
                init = init || subTV;
                binding.unbind(variable);
            }
            cct.setTV(init);
            return init;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(BFuncCCT cct, Binding binding) {
        return cct.getTV();
    }
}
