package xyz.cychen.ycc.impl.check.econc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;
import xyz.cychen.ycc.impl.check.conc.ConCEvaluator;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class EConCEvaluator extends Evaluator {
    protected ThreadPoolExecutor executor;

    private CCT oldCCT = null;
    private ThreadLocal<CCT> ptr = new ThreadLocal<>();
    private Checker.Change change;

    public void setChange(Checker.Change change) {
        this.change = change;
    }

    public void setOldCCT(CCT oldCCT) {
        this.oldCCT = oldCCT;
        this.ptr.set(oldCCT);
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        innerCon.setExecutor(executor);
    }

    private ConCEvaluator innerCon = new ConCEvaluator();
    private ECCEvaluator innerEcc = new ECCEvaluator();

    private boolean checkAnchor(CCT anchor, CCT cct, Binding binding, boolean splittable) {
        if (anchor.getEConditions().hit(change)) {
            cct.setTV(anchor.getTV());
            return anchor.getTV();
        } else {
            if (splittable) {
                return innerCon.evaluate(cct, binding);
            } else {
                return innerEcc.evaluate(cct, binding);
            }
        }
    }
    public boolean realEvaluate(CCT cct, Binding binding) {
        if (oldCCT == null) {
            return innerCon.evaluate(cct, binding);
        }
        if (!ptr.get().isSCCT()) {
            return checkAnchor(ptr.get(), cct, binding, true);
        }
        return evaluate(cct, binding);
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
        CCT current = ptr.get();
        for (var a: arrows) {
            CCT.QuantifiedArrow arrow = (CCT.QuantifiedArrow) a;
            Binding subbinding = binding.clone();
            subbinding.bind(formula.getVariable(), arrow.getContext());
            Future<Boolean> future = executor.submit(() -> {
                CCT old = ((QuantifiedCCT) current).getChild(arrow.getContext());
                if (old != null) {
                    if (!old.isSCCT()) {
                        return checkAnchor(old, arrow.getCCT(), subbinding, false);
                    } else {
                        ptr.set(old);
                        return evaluate(arrow.getCCT(), subbinding, false);
                    }
                } else {
                    return innerEcc.evaluate(arrow.getCCT(), subbinding);
                }
            });
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
//        boolean left = evaluate(arrows.get(0).getCCT(), binding, splittable);
//        boolean right = evaluate(arrows.get(1).getCCT(), binding, splittable);
        boolean left, right;
        CCT currentPos = ptr.get();
        CCT oldLeftC = ptr.get().getChildren().get(0).getCCT();
        if (oldLeftC.isSCCT()) {
            ptr.set(oldLeftC);
            left = evaluate(arrows.get(0).getCCT(), binding);
            ptr.set(currentPos);
        } else {
            left = checkAnchor(oldLeftC, arrows.get(0).getCCT(), binding, splittable);
        }
        CCT oldRightC = ptr.get().getChildren().get(1).getCCT();
        if (oldRightC.isSCCT()) {
            ptr.set(oldRightC);
            right = evaluate(arrows.get(1).getCCT(), binding);
            ptr.set(currentPos);
        } else {
            right = checkAnchor(oldRightC, arrows.get(1).getCCT(), binding, splittable);
        }
        return Pair.with(left, right);
    }

    private boolean notHelper(NotCCT cct, Binding binding, boolean splittable) {
        CCT currentPos = ptr.get();
        ptr.set(ptr.get().getChildren().get(0).getCCT());
        boolean r = evaluate(cct.getChildren().get(0).getCCT(), binding, splittable);
        ptr.set(currentPos);
        return r;
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
        CCT current = ptr.get();
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT old = ((UniversalCCT) ptr.get()).getChild(qArrow.getContext());
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue;
            if (old != null) {
                if (old.isSCCT()) {
                    ptr.set(old);
                    truthValue = evaluate(arrow.getCCT(), binding, false);
                    ptr.set(current);
                } else {
                    truthValue = checkAnchor(old, arrow.getCCT(), binding, false);
                }
            } else {
                truthValue = innerEcc.evaluate(arrow.getCCT(), binding);
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
        CCT current = ptr.get();
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT old = ((ExistentialCCT) ptr.get()).getChild(qArrow.getContext());
            binding.bind(f.getVariable(), qArrow.getContext());
            boolean truthValue;
            if (old != null) {
                if (old.isSCCT()) {
                    ptr.set(old);
                    truthValue = evaluate(arrow.getCCT(), binding, false);
                    ptr.set(current);
                } else {
                    truthValue = checkAnchor(old, arrow.getCCT(), binding, false);
                }
            } else {
                truthValue = innerEcc.evaluate(arrow.getCCT(), binding);
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
