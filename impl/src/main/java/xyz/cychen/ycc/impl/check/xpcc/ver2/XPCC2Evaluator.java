package xyz.cychen.ycc.impl.check.xpcc.ver2;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;

import java.util.List;

public class XPCC2Evaluator extends Evaluator {

    public class InnerECCEvaluator extends Evaluator {
        private Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct, Binding binding) {
//            long start = System.nanoTime();
            cct.prepareOutdated();
//            overheadTime += System.nanoTime() - start;

            List<CCT.Arrow> arrows = cct.getChildren();
            boolean left = evaluate(arrows.get(0).getCCT(), binding);
            boolean right = evaluate(arrows.get(1).getCCT(), binding);
            return Pair.with(left, right);
        }

        private boolean notHelper(NotCCT cct, Binding binding) {
//            long start = System.nanoTime();
            cct.prepareOutdated();
//            overheadTime += System.nanoTime() - start;

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
//            long start = System.nanoTime();
            cct.prepareOutdated();
//            overheadTime += System.nanoTime() - start;

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
//            long start = System.nanoTime();
            cct.prepareOutdated();
//            overheadTime += System.nanoTime() - start;

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
//            long start = System.nanoTime();
            cct.prepareOutdated();
//            overheadTime += System.nanoTime() - start;

            BFuncFormula f = (BFuncFormula) cct.getFormula();
            f.bind(binding);
            boolean result = f.evaluate();
            f.unbind();
            cct.setTV(result);
            return result;
        }
    }

    protected Checker.Change currentChange = null;
    protected Evaluator totalEval = new InnerECCEvaluator();


    protected long overheadTime = 0;

    public long getOverheadTime() {
        return overheadTime;
    }

    public void  clearOverhead() {
        overheadTime = 0;
    }

//    public XPCC2Evaluator(Evaluator totalEval) {
//        this.totalEval = totalEval;
//    }

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

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }

        if (!cct.isAffected()) {
            return cct.getTV();
        }
        if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue0(), binding);
            boolean tv = nsub && children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue1(), binding);
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

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return (boolean) cct.getTV();
        }

        if (!cct.isAffected()) {
            return cct.getTV();
        }
        if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue0(), binding);
            boolean tv = nsub || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue1(), binding);
            boolean tv = nsub || children.getValue0().getTV();
            cct.setTV(tv);
            return tv;
        }
        else {
//        cct.setIncrementalCount(incrementalTotal);
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelper(cct);

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            return cct.getTV();
        }


        if (!cct.isAffected()) {
            return cct.getTV();
        }

        if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue0(), binding);
            boolean tv = (!nsub) || children.getValue1().getTV();
            cct.setTV(tv);
            return tv;
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            boolean nsub = evaluate(children.getValue1(), binding);
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

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!isAffected(child)) {
            return cct.getTV();
        }

        if (!cct.isAffected()) {
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
        boolean affected =  cct.getFormula().getChildren()[0].isAffected();

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!thisSet && !affected) {
            return cct.getTV();
        }

        if (!cct.isAffected()) {
            return cct.getTV();
        }
        if (thisSet && currentChange instanceof Checker.AddChange && !affected) {
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
                boolean subTV = evaluate(a.getCCT(), binding);
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

//        long start = System.nanoTime();
        cct.prepareOutdated();
//        overheadTime += System.nanoTime() - start;

        if (!thisSet && !affected) {
            return cct.getTV();
        }

        if (thisSet && currentChange instanceof Checker.AddChange && !affected) {
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
        cct.prepareOutdated();
        return cct.getTV();
    }
}
