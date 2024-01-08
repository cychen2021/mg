package xyz.cychen.ycc.impl.check.ecc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

import java.util.List;
import java.util.function.BiFunction;

public class ECCGenerator extends Generator {
    private Pair<Boolean, Boolean> binaryHelperTruthValue(BinaryCCT cct) {
        List<CCT.Arrow> arrows = cct.getChildren();
        return Pair.with(arrows.get(0).getCCT().getTV(),
                arrows.get(1).getCCT().getTV());
    }

    private Pair<Link, Link> binaryHelperLink(BinaryCCT cct, Binding binding) {
        List<CCT.Arrow> arrows = cct.getChildren();
        Link left = generate(arrows.get(0).getCCT(), binding);
        Link right = generate(arrows.get(1).getCCT(), binding);
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

    @Override
    public Link visit(AndCCT cct, Binding binding) {
        var sub = binaryHelperLink(cct, binding);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                cartesian, union, selectR, selectL);
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(OrCCT cct, Binding binding) {
        var sub = binaryHelperLink(cct, binding);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                union, cartesian, selectL, selectR);
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(ImpliesCCT cct, Binding binding) {
        var sub = binaryHelperLink(cct, binding);
        var tvs = binaryHelperTruthValue(cct);
        Link result = binaryHandler(tvs.getValue0(), tvs.getValue1(), sub.getValue0(), sub.getValue1(),
                selectR, selectInverseL, cartesianInverseL, unionInverseL);
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(NotCCT cct, Binding binding) {
        CCT sub = cct.getChildren().get(0).getCCT();
        Link subLink = generate(sub, binding);
        Link result = Link.flip(subLink);
        cct.setLK(result);
        return result;
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
            Link subLink = generate(sub, binding);
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
            Link subLink = generate(sub, binding);
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
