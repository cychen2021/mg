package xyz.cychen.ycc.impl.check.conc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Builder;
import xyz.cychen.ycc.framework.formula.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ConCBuilder extends Builder {
    ThreadPoolExecutor executor;

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public CCT build(Formula formula) {
        return build(formula, true);
    }

    public CCT build(Formula formula, boolean splittable) {
        if (formula instanceof AndFormula andFormula) {
            return visit(andFormula, splittable);
        } else if (formula instanceof OrFormula orFormula) {
            return visit(orFormula, splittable);
        } else if (formula instanceof ImpliesFormula impliesFormula) {
            return visit(impliesFormula, splittable);
        } else if (formula instanceof NotFormula notFormula) {
            return visit(notFormula, splittable);
        } else if (formula instanceof UniversalFormula universalFormula) {
            if (splittable) {
                return visit(universalFormula, splittable);
            } else {
                return visit(universalFormula);
            }
        } else if (formula instanceof ExistentialFormula existentialFormula) {
            if (splittable) {
                return visit(existentialFormula, splittable);
            } else {
                return visit(existentialFormula);
            }
        } else if (formula instanceof BFuncFormula bFuncFormula) {
            return visit(bFuncFormula);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Pair<CCT, CCT> binaryHelper(BinaryFormula formula, boolean splittable) {
        Formula left = formula.getChildren()[0];
        Formula right = formula.getChildren()[1];
        CCT lc = build(left, splittable);
        CCT rc = build(right, splittable);
        return Pair.with(lc, rc);
    }

    private CCT notHelper(NotFormula formula, boolean splittable) {
        Formula sub = formula.getChildren()[0];
        return build(sub, splittable);
    }

    private List<Pair<Context, CCT>> quantifiedHelper(QuantifiedFormula formula) {
        ContextSet universe = formula.getUniverse();
        List<Pair<Context, CCT>> result = new ArrayList<>(universe.size());
        for (Context c: universe) {
            CCT sub = build(formula.getChildren()[0], false);
            result.add(Pair.with(c, sub));
        }
        return result;
    }

    public AndCCT visit(AndFormula formula, boolean splittable) {
        var sub = binaryHelper(formula, splittable);
        return new AndCCT(formula, sub.getValue0(), sub.getValue1());
    }

    public OrCCT visit(OrFormula formula, boolean splittable) {
        var sub = binaryHelper(formula, splittable);
        return new OrCCT(formula, sub.getValue0(), sub.getValue1());
    }

    public ImpliesCCT visit(ImpliesFormula formula, boolean splittable) {
        var sub = binaryHelper(formula, splittable);
        return new ImpliesCCT(formula, sub.getValue0(), sub.getValue1());
    }

    public NotCCT visit(NotFormula formula, boolean splittable) {
        CCT sub = notHelper(formula, splittable);
        return new NotCCT(formula, sub);
    }

    @Override
    public AndCCT visit(AndFormula formula) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OrCCT visit(OrFormula formula) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImpliesCCT visit(ImpliesFormula formula) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NotCCT visit(NotFormula formula) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniversalCCT visit(UniversalFormula formula) {
        List<Pair<Context, CCT>> children = quantifiedHelper(formula);
        return new UniversalCCT(formula, children);
    }

    protected List<Pair<Context, CCT>> concurrent(QuantifiedFormula formula) {
        List<Pair<Context, Future<CCT>>> results = new ArrayList<>(formula.getUniverse().size());
        for (Context c: formula.getUniverse()) {
             results.add(Pair.with(c, executor.submit(() -> build(formula.getChildren()[0], false))));
        }
        return results.stream().map(f -> {
            try {
                return Pair.with(f.getValue0(), f.getValue1().get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).toList();
    }

    public UniversalCCT visit(UniversalFormula formula, boolean splittable) {
        assert splittable;
        List<Pair<Context, CCT>> children = concurrent(formula);
        return new UniversalCCT(formula, children);
    }

    public ExistentialCCT visit(ExistentialFormula formula, boolean splittable) {
        assert splittable;
        List<Pair<Context, CCT>> children = concurrent(formula);
        return new ExistentialCCT(formula, children);
    }

    @Override
    public ExistentialCCT visit(ExistentialFormula formula) {
        List<Pair<Context, CCT>> children = quantifiedHelper(formula);
        return new ExistentialCCT(formula, children);
    }

    @Override
    public BFuncCCT visit(BFuncFormula formula) {
        return new BFuncCCT(formula);
    }
}
