package xyz.cychen.ycc.impl.check.xyz;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.ExistentialFormula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;

import java.util.List;

public class XYZGenerator extends Generator {
    private Pair<CCT, CCT> binaryHelperCCT(BinaryCCT cct) {
        var children = cct.getChildren();
        return Pair.with(children.get(0).getCCT(), children.get(1).getCCT());
    }

    private Pair<Boolean, Boolean> binaryHelperTruthValue(BinaryCCT cct) {
        List<CCT.Arrow> arrows = cct.getChildren();
        return Pair.with(arrows.get(0).getCCT().getTV(),
                arrows.get(1).getCCT().getTV());
    }

    private CCT notHelperCCT(NotCCT cct) {
        var children = cct.getChildren();
        return children.get(0).getCCT();
    }

    private boolean notHelperTruthValue(CCT cct) {
        return cct.getChildren().get(0).getCCT().getTV();
    }

    @Override
    public Link visit(AndCCT cct, Binding binding) {
        var children = binaryHelperCCT(cct);
        var tv = binaryHelperTruthValue(cct);
        if (tv.getValue0() && tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.cartesian(leftLink, rightLink);
            cct.setLK(result);
            return result;
        }
        else if (tv.getValue0() && !tv.getValue1()) {
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.copy(rightLink);
            cct.setLK(result);
            return result;
        }
        else if (!tv.getValue0() && tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link result = Link.copy(leftLink);
            cct.setLK(result);
            return result;
        }
        else {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.union(leftLink, rightLink);
            cct.setLK(result);
            return result;
        }
    }

    @Override
    public Link visit(OrCCT cct, Binding binding) {
        var children = binaryHelperCCT(cct);
        var tv = binaryHelperTruthValue(cct);
        if (tv.getValue0() && tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.union(leftLink, rightLink);
            cct.setLK(result);
            return result;
        }
        else if (tv.getValue0() && !tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link result = Link.copy(leftLink);
            cct.setLK(result);
            return result;
        }
        else if (!tv.getValue0() && tv.getValue1()) {
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.copy(rightLink);
            cct.setLK(result);
            return result;
        }
        else {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.cartesian(leftLink, rightLink);
            cct.setLK(result);
            return result;
        }
    }

    @Override
    public Link visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelperCCT(cct);
        var tv = binaryHelperTruthValue(cct);
        if (tv.getValue0() && !tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.cartesian(Link.flip(leftLink), rightLink);
            cct.setLK(result);
            return result;
        }
        else if (!tv.getValue0() && tv.getValue1()) {
            Link leftLink = generate(children.getValue0(), binding);
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.union(Link.flip(leftLink), rightLink);
            cct.setLK(result);
            return result;
        }
        else if (tv.getValue0() && tv.getValue1()) {
            Link rightLink = generate(children.getValue1(), binding);
            Link result = Link.copy(rightLink);
            cct.setLK(result);
            return result;
        }
        else {
            Link leftLink = generate(children.getValue0(), binding);
            Link result = Link.flip(leftLink);
            cct.setLK(result);
            return result;
        }
    }

    @Override
    public Link visit(NotCCT cct, Binding binding) {
        CCT sub = notHelperCCT(cct);
        Link link = generate(sub, binding);
        Link r = Link.flip(link);
        cct.setLK(r);
        return r;
    }

    @Override
    public Link visit(UniversalCCT cct, Binding binding) {
        boolean tv = cct.getTV();
        Link result = Link.of(tv ? Link.Type.SAT : Link.Type.VIO);
        for (var arrow: cct.getChildren()) {
            CCT.QuantifiedArrow child = (CCT.QuantifiedArrow) arrow;
            boolean subtv = child.getCCT().getTV();
            if (!subtv) {
                Variable v = ((UniversalFormula) cct.getFormula()).getVariable();
                binding.bind( v, ((CCT.QuantifiedArrow) arrow).getContext());
                Link subLink = generate(child.getCCT(), binding);
                binding.unbind(v);
                result.unionWith(Link.cartesian(Link.of(Link.Type.VIO, v, child.getContext()), subLink));
            }
        }
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(ExistentialCCT cct, Binding binding) {
        boolean tv = cct.getTV();
        Link result = Link.of(tv ? Link.Type.SAT : Link.Type.VIO);
        for (var arrow: cct.getChildren()) {
            CCT.QuantifiedArrow child = (CCT.QuantifiedArrow) arrow;
            boolean subtv = child.getCCT().getTV();
            if (subtv) {
                Variable v = ((ExistentialFormula) cct.getFormula()).getVariable();
                binding.bind( v, ((CCT.QuantifiedArrow) arrow).getContext());
                Link subLink = generate(child.getCCT(), binding);
                binding.unbind(v);
                result.unionWith(Link.cartesian(Link.of(Link.Type.SAT, v, child.getContext()), subLink));
            }
        }
        cct.setLK(result);
        return result;
    }

    @Override
    public Link visit(BFuncCCT cct, Binding binding) {
        boolean tv = cct.getTV();
        cct.bind(binding);
        Link result = Link.of(tv ? Link.Type.SAT : Link.Type.VIO);
        cct.setLK(result);
        return result;
    }
}
