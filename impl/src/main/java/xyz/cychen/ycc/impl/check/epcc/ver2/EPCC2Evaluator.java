package xyz.cychen.ycc.impl.check.epcc.ver2;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

import java.util.ArrayList;
import java.util.List;

public class EPCC2Evaluator extends Evaluator {
    public class AnchorUpdater {
        public boolean update(CCT cct, Binding binding) {
            if (cct instanceof AndCCT andCCT) {
                return visit(andCCT, binding);
            } else if (cct instanceof OrCCT orCCT) {
                return visit(orCCT, binding);
            } else if (cct instanceof NotCCT notCCT) {
                return visit(notCCT, binding);
            } else if (cct instanceof ImpliesCCT impliesCCT) {
                return visit(impliesCCT, binding);
            } else if (cct instanceof ExistentialCCT existsCCT) {
                return visit(existsCCT, binding);
            } else if (cct instanceof UniversalCCT forallCCT) {
                return visit(forallCCT, binding);
            } else if (cct instanceof BFuncCCT bFuncCCT) {
                return visit(bFuncCCT, binding);
            } else {
                System.err.println("Unknown CCT type!");
                System.exit(-1);
                return false;
            }
        }

        private Pair<CCT, CCT> binaryHelper(BinaryCCT cct) {
            var children = cct.getChildren();
            return Pair.with(children.get(0).getCCT(), children.get(1).getCCT());
        }

        public boolean visit(AndCCT cct, Binding binding) {
            var children = binaryHelper(cct);
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                boolean old = children.getValue0().getTV();
                boolean nsub = update(children.getValue0(), binding);
                if (nsub == old) {
                    return cct.getTV();
                }
                boolean tv = nsub && children.getValue1().getTV();
                cct.setTV(tv);
                return tv;
            }
            else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
                boolean old = children.getValue1().getTV();
                boolean nsub = update(children.getValue1(), binding);
                if (old == nsub) {
                    return cct.getTV();
                }
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

        public boolean visit(OrCCT cct, Binding binding) {
            var children = binaryHelper(cct);
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return (boolean) cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                boolean old = children.getValue0().getTV();
                boolean nsub = update(children.getValue0(), binding);
                if (old == nsub) {
                    return cct.getTV();
                }
                boolean tv = nsub || children.getValue1().getTV();
                cct.setTV(tv);
                return tv;
            }
            else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
                boolean old = children.getValue1().getTV();
                boolean nsub = update(children.getValue1(), binding);
                if (old == nsub) {
                    return cct.getTV();
                }
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

        public boolean visit(ImpliesCCT cct, Binding binding) {
            var children = binaryHelper(cct);
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                boolean old = children.getValue0().getTV();
                boolean nsub = update(children.getValue0(), binding);
                if (old == nsub) {
                    return cct.getTV();
                }
                boolean tv = (!nsub) || children.getValue1().getTV();
                cct.setTV(tv);
                return tv;
            }
            else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
                boolean old = children.getValue1().getTV();
                boolean nsub = update(children.getValue1(), binding);
                if (old == nsub) {
                    return cct.getTV();
                }
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

        public boolean visit(NotCCT cct, Binding binding) {
            var arrows = cct.getChildren();
            var child = arrows.get(0).getCCT();
            if (!isAffected(child)) {
                return cct.getTV();
            }

            boolean old = child.getTV();
            boolean tv = !update(child, binding);
            if (old == tv) {
                return cct.getTV();
            }
            cct.setTV(tv);
            return tv;
        }

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
                boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
                binding.unbind(variable);
                boolean currentTV = cct.getTV();
                boolean ntv = subtv && currentTV;
                cct.setTV(ntv);
                return ntv;
            }
            else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
                boolean init = true;
                for (var a: cct.getChildren()) {
                    init = init && a.getCCT().getTV();
                }
                cct.setTV(init);
                return init;
            }
            else if (!thisSet && affected) {
                boolean init = true;
                for (var a: cct.getChildren()) {
                    binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                    boolean subTV = update(a.getCCT(), binding);
                    init = init && subTV;
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
                boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
                binding.unbind(variable);
                boolean currentTV = cct.getTV();
                boolean ntv = subtv || currentTV;
                cct.setTV(ntv);
                return ntv;
            }
            else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
                boolean init = false;
                for (var a: cct.getChildren()) {
                    init = init || a.getCCT().getTV();
                }
                cct.setTV(init);
                return init;
            }
            else if (!thisSet && affected) {
                boolean init = false;
                for (var a: cct.getChildren()) {
                    binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                    boolean subTV = update(a.getCCT(), binding);
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

        public boolean visit(BFuncCCT cct, Binding binding) {
            return cct.getTV();
        }
    }

    protected Checker.Change currentChange = null;
    protected Evaluator totalEval;

    public EPCC2Evaluator(Evaluator totalEval) {
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

    int chrono;

    public void setChrono(int chrono) {
        this.chrono = chrono;
    }

    public boolean isAnchor(CCT cct, CCT parent) {
        if (chrono == 0) {
            throw new RuntimeException();
        }
        return cct.getChrono() != chrono - 1 && (parent == null || parent.getChrono() == chrono -1);
    }

    private AnchorUpdater updater = new AnchorUpdater();

    public AnchorUpdater getUpdater() {
        return updater;
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            boolean nsub;
            if (isAnchor(children.getValue0(), cct) && children.getValue0().getEConditions().hit(currentChange)) {
                nsub = children.getValue0().getTV();
                updater.update(children.getValue0(), binding);
            } else {
                nsub = evaluate(children.getValue0(), binding);
            }
            boolean tv = nsub && children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            boolean nsub;
            if (isAnchor(children.getValue1(), cct) && children.getValue1().getEConditions().hit(currentChange)) {
                nsub = children.getValue1().getTV();
                updater.update(children.getValue1(), binding);
            } else {
                nsub = evaluate(children.getValue1(), binding);
            }
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
            return (boolean) cct.getTV();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
//            boolean nsub = evaluate(children.getValue0(), binding);
            boolean nsub;
            if (isAnchor(children.getValue0(), cct) && children.getValue0().getEConditions().hit(currentChange)) {
                nsub = children.getValue0().getTV();
                updater.update(children.getValue0(), binding);
            } else {
                nsub = evaluate(children.getValue0(), binding);
            }
            boolean tv = nsub || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
//            boolean nsub = evaluate(children.getValue1(), binding);
            boolean nsub;
            if (isAnchor(children.getValue1(), cct) && children.getValue1().getEConditions().hit(currentChange)) {
                nsub = children.getValue1().getTV();
                updater.update(children.getValue1(), binding);
            } else {
                nsub = evaluate(children.getValue1(), binding);
            }
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
//            boolean nsub = evaluate(children.getValue0(), binding);
            boolean nsub;
            if (isAnchor(children.getValue0(), cct) && children.getValue0().getEConditions().hit(currentChange)) {
                nsub = children.getValue0().getTV();
                updater.update(children.getValue0(), binding);
            } else {
                nsub = evaluate(children.getValue0(), binding);
            }
            boolean tv = (!nsub) || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
//            boolean nsub = evaluate(children.getValue1(), binding);
            boolean nsub;
            if (isAnchor(children.getValue1(), cct) && children.getValue1().getEConditions().hit(currentChange)) {
                nsub = children.getValue1().getTV();
                updater.update(children.getValue1(), binding);
            } else {
                nsub = evaluate(children.getValue1(), binding);
            }
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

        boolean tv = !evaluate(child, binding);
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
            boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
            binding.unbind(variable);
            boolean currentTV = cct.getTV();
            boolean ntv = subtv && currentTV;
            cct.setTV(ntv);
            return ntv;
        } else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
            boolean init = true;
            for (var a: cct.getChildren()) {
                init = init && a.getCCT().getTV();
            }
            cct.setTV(init);
            return init;
        } else if (!thisSet && affected) {
            boolean init = true;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV;
                if (isAnchor(a.getCCT(), cct) && a.getCCT().getEConditions().hit(currentChange)) {
                    subTV = a.getCCT().getTV();
                    updater.update(a.getCCT(), binding);
                } else {
                    subTV = evaluate(a.getCCT(), binding);
                }
                init = init && subTV;
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
    public boolean visit(ExistentialCCT cct, Binding binding) {
        ContextSet universe = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Context ctx = currentChange.getContext();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean thisSet = currentChange.getTargetSet().equals(universe.getId());
        boolean affected = (boolean) cct.getFormula().getChildren()[0].isAffected();
        if (!thisSet && !affected) {
            return cct.getTV();
        } else if (thisSet && currentChange instanceof Checker.AddChange && !affected) {
            binding.bind(variable, ctx);
            boolean subtv = totalEval.evaluate(cct.getChild(ctx), binding);
            binding.unbind(variable);
            boolean currentTV = cct.getTV();
            boolean ntv = subtv || currentTV;
            cct.setTV(ntv);
            return ntv;
        } else if (thisSet && currentChange instanceof Checker.DelChange && !affected) {
            boolean init = false;
            for (var a: cct.getChildren()) {
                init = init || a.getCCT().getTV();
            }
            cct.setTV(init);
            return init;
        } else if (!thisSet && affected) {
            boolean init = false;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV;
                if (isAnchor(a.getCCT(), cct) && a.getCCT().getEConditions().hit(currentChange)) {
                    subTV = a.getCCT().getTV();
                    updater.update(a.getCCT(), binding);
                } else {
                    subTV = evaluate(a.getCCT(), binding);
                }
                init = init || subTV;
                binding.unbind(variable);
            }
            cct.setTV(init);
            return init;
        } else {
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
