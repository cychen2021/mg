package xyz.cychen.ycc.impl.check.conc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.impl.check.ecc.ECCGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiFunction;

public class ConCGenerator extends Generator {
    protected ThreadPoolExecutor executor;

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Link generate(CCT cct, Binding binding) {
        return generate(cct, binding, true);
    }

    public Link generate(CCT cct, Binding binding, boolean splittable) {
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
        } else if (cct instanceof ExistentialCCT existCCT) {
            if (splittable) {
                return visit(existCCT, binding, splittable);
            } else {
                return visit(existCCT, binding);
            }
        } else if (cct instanceof BFuncCCT bfuncCCT) {
            return visit(bfuncCCT, binding);
        } else {
            throw new IllegalArgumentException();
        }
    }


    protected List<Link> concurrent(QuantifiedFormula formula, List<CCT.Arrow> arrows, Binding binding) {
        List<Future<Link>> results = new ArrayList<>(arrows.size());
        for (var a : arrows) {
            CCT.QuantifiedArrow arrow = (CCT.QuantifiedArrow) a;
            Binding subbinding = binding.clone();
            subbinding.bind(formula.getVariable(), arrow.getContext());
            results.add(executor.submit(() -> generate(arrow.getCCT(), subbinding, false)));
        }
        List<Link> lresult = results.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        }).toList();
        return lresult;
    }

    private Pair<Boolean, Boolean> binaryHelperTruthValue(BinaryCCT cct) {
        List<CCT.Arrow> arrows = cct.getChildren();
        return Pair.with(arrows.get(0).getCCT().getTV(),
                arrows.get(1).getCCT().getTV());
    }

    private Pair<Link, Link> binaryHelperLink(BinaryCCT cct, Binding binding, boolean splittable) {
        List<CCT.Arrow> arrows = cct.getChildren();
        Link left = generate(arrows.get(0).getCCT(), binding, splittable);
        Link right = generate(arrows.get(1).getCCT(), binding, splittable);
        return Pair.with(left, right);
    }

    private Link binaryHandler(boolean tvl, boolean tvr, Link lkl, Link lkr,
                               BiFunction<Link, Link, Link> tt, BiFunction<Link, Link, Link> ff,
                               BiFunction<Link, Link, Link> tf, BiFunction<Link, Link, Link> ft) {
        if (tvl && tvr) {
            return tt.apply(lkl, lkr);
        }
        else if (tvl && !tvr) {
            return tf.apply(lkl, lkr);
        }
        else if (!tvl && tvr) {
            return ft.apply(lkl, lkr);
        }
        else {
            return ff.apply(lkl, lkr);
        }
    }

    private BiFunction<Link, Link, Link> cartesian = (Link lkl, Link lkr) -> Link.cartesian(lkl, lkr);
    private BiFunction<Link, Link, Link> union = (Link lkl, Link lkr) -> Link.union(lkl, lkr);
    private BiFunction<Link, Link, Link> selectL = (Link lkl, Link lkr) -> Link.copy(lkl);
    private BiFunction<Link, Link, Link> selectR = (Link lkl, Link lkr) -> Link.copy(lkr);
    private BiFunction<Link, Link, Link> cartesianInverseL = (Link lkl, Link lkr) ->
            cartesian.apply(Link.flip(lkl), lkr);
    private BiFunction<Link, Link, Link> unionInverseL = (Link lkl, Link lkr) -> union.apply(Link.flip(lkl), lkr);
    private BiFunction<Link, Link, Link> selectInverseL = (Link lkl, Link lkr) -> Link.flip(lkl);

    public Link visit(AndCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelperLink(cct, binding, splittable);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                cartesian, union, selectR, selectL);
        cct.setLK(result);
        return result;
    }

    public Link visit(OrCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelperLink(cct, binding, splittable);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                union, cartesian, selectL, selectR);
        cct.setLK(result);
        return result;
    }

    public Link visit(ImpliesCCT cct, Binding binding, boolean splittable) {
        var sub = binaryHelperLink(cct, binding, splittable);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                selectR, selectInverseL, cartesianInverseL, unionInverseL);
        cct.setLK(result);
        return result;
    }

    public Link visit(NotCCT cct, Binding binding, boolean splittable) {
        CCT sub = cct.getChildren().get(0).getCCT();
        Link subLink = generate(sub, binding, splittable);
        Link result = Link.flip(subLink);
        cct.setLK(result);
        return result;
    }

    public Link visit(UniversalCCT cct, Binding binding, boolean splittable) {
        assert splittable;
        var children = cct.getChildren();
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        List<Link> subresults = concurrent(f, children, binding);
        Link result = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
        for (int i=0; i<children.size(); i++) {
            if (!children.get(i).getCCT().getTV()) {
                result.unionWith(
                        Link.cartesian(
                                Link.of(Link.Type.VIO, f.getVariable(), ((CCT.QuantifiedArrow) children.get(i)).getContext()),
                                subresults.get(i)
                        )
                );
            }
        }
        cct.setLK(result);
        return result;
    }

    public Link visit(ExistentialCCT cct, Binding binding, boolean splittable) {
        assert splittable;
        var children = cct.getChildren();
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        List<Link> subresults = concurrent(f, children, binding);
        Link result = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
        for (int i=0; i<children.size(); i++) {
            if (children.get(i).getCCT().getTV()) {
                result.unionWith(
                        Link.cartesian(
                                Link.of(Link.Type.SAT, f.getVariable(), ((CCT.QuantifiedArrow) children.get(i)).getContext()),
                                subresults.get(i)
                        )
                );
            }
        }
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(AndCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link visit(OrCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link visit(ImpliesCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link visit(NotCCT cct, Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link visit(UniversalCCT cct, Binding binding) {
        Variable v = ((QuantifiedFormula) cct.getFormula()).getVariable();
        List<CCT.Arrow> arrows = cct.getChildren();
        Link result = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT sub = qArrow.getCCT();
            binding.bind(v, qArrow.getContext());
            Link subLink = generate(sub, binding, false);
            binding.unbind(v);
            if (!sub.getTV()) {
                result.unionWith(Link.cartesian(Link.of(Link.Type.VIO, v, qArrow.getContext()), subLink));
            }
        }
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(ExistentialCCT cct, Binding binding) {
        Variable v = ((QuantifiedFormula) cct.getFormula()).getVariable();
        List<CCT.Arrow> arrows = cct.getChildren();
        Link result = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
        for (var arrow: arrows) {
            CCT.QuantifiedArrow qArrow = (CCT.QuantifiedArrow) arrow;
            CCT sub = qArrow.getCCT();
            binding.bind(v, qArrow.getContext());
            Link subLink = generate(sub, binding, false);
            binding.unbind(v);
            if (sub.getTV()) {
                result.unionWith(Link.cartesian(Link.of(Link.Type.SAT, v, qArrow.getContext()), subLink));
            }
        }
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(BFuncCCT cct, Binding binding) {
        cct.bind(binding);
        Link result = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
        cct.setLK(result);
        return result;
    }
}
