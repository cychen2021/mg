package xyz.cychen.ycc.impl.check.conc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.*;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConCEvaluator extends Evaluator {
    protected ThreadPoolExecutor executor;

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }


    @Override
    public boolean evaluate(CCT cct, Binding binding) {
        return evaluate(cct, binding, true);
    }

    public boolean evaluate(CCT cct, Binding binding, boolean splittable) {
        if (cct instanceof AndCCT andCCT) {
            return visit(andCCT, binding, splittable);
        } else if (cct instanceof OrCCT orCCT) {
            return visit(orCCT, binding, splittable);
        } else if (cct instanceof ImpliesCCT impliesCCT) {
            return visit(impliesCCT, binding, splittable);
        } else if (cct instanceof NotCCT notCCT) {
            return visit(notCCT, binding, splittable);
        } else if (cct instanceof UniversalCCT univCCT) {
            if (splittable) {
                return visit(univCCT, binding, splittable);
            } else {
                return visit(univCCT, binding);
            }
        } else if (cct instanceof ExistentialCCT exisCCT) {
            if (splittable) {
                return visit(exisCCT, binding, splittable);
            } else {
                return visit(exisCCT, binding);
            }
        } else if (cct instanceof BFuncCCT bfuncCCT) {
            return visit(bfuncCCT, binding);
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected List<Boolean> concurrent(QuantifiedFormula formula, List<CCT.Arrow> arrows, Binding binding) {
        ArrayList<Future<Boolean>> result = new ArrayList<>(arrows.size());
        for (var a: arrows) {
            CCT.QuantifiedArrow arrow = (CCT.QuantifiedArrow) a;
            Binding subbinding = binding.clone();
            subbinding.bind(formula.getVariable(), arrow.getContext());
            Future<Boolean> future = executor.submit(() -> evaluate(arrow.getCCT(), subbinding, false));
            result.add(future);
        }

        var bresult = result.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        });
        return bresult.toList();
    }

    public boolean visit(UniversalCCT cct, Binding binding, boolean splittable) {
        assert splittable;
        List<Boolean> result = concurrent((QuantifiedFormula) cct.getFormula(), cct.getChildren(), binding);
        boolean r = result.stream().reduce(true, (a, b) -> a && b);
        cct.setTV(r);
        return r;
    }

    public boolean visit(ExistentialCCT cct, Binding binding, boolean splittable) {
        assert splittable;
        List<Boolean> result = concurrent((QuantifiedFormula) cct.getFormula(), cct.getChildren(), binding);
        boolean r = result.stream().reduce(false, (a, b) -> a || b);
        cct.setTV(r);
        return r;
    }

    private Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct, Binding binding, boolean splittable) {
        List<CCT.Arrow> arrows = cct.getChildren();
        boolean left = evaluate(arrows.get(0).getCCT(), binding, splittable);
        boolean right = evaluate(arrows.get(1).getCCT(), binding, splittable);
        return Pair.with(left, right);
    }

    private boolean notHelper(NotCCT cct, Binding binding, boolean splittable) {
        return evaluate(cct.getChildren().get(0).getCCT(), binding, splittable);
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean visit(OrCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean visit(NotCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    public boolean visit(AndCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelper(cct, binding, splittable);
        boolean result = sub.getValue0() && sub.getValue1();
        cct.setTV(result);
        return result;
    }

    public boolean visit(OrCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelper(cct, binding, splittable);
        boolean result = sub.getValue0() || sub.getValue1();
        cct.setTV(result);
        return result;
    }

    public boolean visit(ImpliesCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelper(cct, binding, splittable);
        boolean result = (!sub.getValue0()) || sub.getValue1();
        cct.setTV(result);
        return result;
    }

    public boolean visit(NotCCT cct, Binding binding, boolean splittable) {
        boolean sub = notHelper(cct, binding, splittable);
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
            boolean truthValue = evaluate(arrow.getCCT(), binding, false);
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
            boolean truthValue = evaluate(arrow.getCCT(), binding, false);
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
