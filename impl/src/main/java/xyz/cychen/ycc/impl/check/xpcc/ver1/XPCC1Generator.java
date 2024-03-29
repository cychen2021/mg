package xyz.cychen.ycc.impl.check.xpcc.ver1;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

public class XPCC1Generator extends Generator {

    protected int chrono = -1;

    public void setChrono(int chrono) {
        this.chrono = chrono;
    }

    protected Checker.Change currentChange = null;

    public void setChange(Checker.Change change) {
        this.currentChange = change;
    }

    public boolean isStale(CCT cct) {
        return cct.getChrono() == -1 || cct.getChrono() < chrono - 1;
    }

    @Override
    public Link generate(CCT cct, Binding binding) {
        if (!isStale(cct) && !cct.isAffected() && cct.getLinkNoDefault() != null) {
//            cct.setIncrementalCount(0);
            return cct.getLK();
        }

        return super.generate(cct, binding);
    }

    protected Pair<CCT, CCT> binaryHelperC(CCT cct) {
        var left = cct.getChildren().get(0).getCCT();
        var right = cct.getChildren().get(1).getCCT();
        return Pair.with(left, right);
    }

    protected Pair<Boolean, Boolean> binaryHelperT(Pair<CCT, CCT> p) {
        return Pair.with(
                p.getValue0().getTV(),
                p.getValue1().getTV()
        );
    }

    @Override
    public Link visit(AndCCT cct, Binding binding) {
        var children = binaryHelperC(cct);
        var tv = binaryHelperT(children);
        Link r;
        if (tv.getValue0() && tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.cartesian(l1, l2);
        }
        else if (tv.getValue0() && !tv.getValue1()) {
            Link l2 = generate(children.getValue1(), binding);
            r = Link.copy(l2);
        }
        else if (!tv.getValue0() && tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            r = Link.copy(l1);
        }
        else {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.union(l1, l2);
        }

//        setIncrementalLinkCount(cct, r.size());

        cct.setLK(r);
        return r;
    }

    @Override
    public Link visit(OrCCT cct, Binding binding) {
        var children = binaryHelperC(cct);
        var tv = binaryHelperT(children);
        Link r;
        if (tv.getValue0() && tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.union(l1, l2);
        }
        else if (!tv.getValue0() && tv.getValue1()) {
            Link l2 = generate(children.getValue1(), binding);
            r = Link.copy(l2);
        }
        else if (tv.getValue0() && !tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            r = Link.copy(l1);
        }
        else {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.cartesian(l1, l2);
        }

//        setIncrementalLinkCount(cct, r.size());

        cct.setLK(r);
        return r;
    }

    @Override
    public Link visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelperC(cct);
        var tv = binaryHelperT(children);
        Link r;
        if (!tv.getValue0() && tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.union(Link.flip(l1), l2);
        }
        else if (tv.getValue0() && tv.getValue1()) {
            Link l2 = generate(children.getValue1(), binding);
            r = Link.copy(l2);
        }
        else if (!tv.getValue0() && !tv.getValue1()) {
            Link l1 = generate(children.getValue0(), binding);
            r = Link.flip(l1);
        }
        else {
            Link l1 = generate(children.getValue0(), binding);
            Link l2 = generate(children.getValue1(), binding);
            r = Link.cartesian(Link.flip(l1), l2);
        }

//        setIncrementalLinkCount(cct, r.size());

        cct.setLK(r);
        return r;
    }

    @Override
    public Link visit(NotCCT cct, Binding binding) {
        Link s = generate(cct.getChildren().get(0).getCCT(), binding);
        Link r = Link.flip(s);

//        setIncrementalLinkCount(cct, r.size());

        cct.setLK(r);
        return r;
    }

    @Override
    public Link visit(UniversalCCT cct, Binding binding) {
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        ContextSet universe = f.getUniverse();
        Variable v = f.getVariable();
        if (!isStale(cct) && currentChange.getTargetSet().equals(universe.getId()) &&
                currentChange instanceof Checker.AddChange) {
            Link l = null;
            int r = 0;
            if (!cct.getChild(currentChange.getContext()).getTV()) {
                binding.bind(v, currentChange.getContext());
                l = Link.cartesian(Link.of(Link.Type.VIO, v, currentChange.getContext()),
                        generate(cct.getChild(currentChange.getContext()), binding));
                r = l.size();
                binding.unbind(v);
            }
            Link old = cct.getLK();
            Link updated = Link.union(old, l);
//            setIncrementalLinkCount(cct, r);
            cct.setLK(updated);
            return updated;
        }
        else {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                CCT sub = qa.getCCT();
                if (!sub.getTV()) {
                    binding.bind(v, qa.getContext());
                    Link subL = generate(sub, binding);
                    binding.unbind(v);
                    Link tmp = Link.cartesian(Link.of(Link.Type.VIO, v, qa.getContext()), subL);
                    r.unionWith(tmp);

                    incre += tmp.size();
                }
            }

//            setIncrementalLinkCount(cct, incre);

            cct.setLK(r);
            return r;
        }
    }

    @Override
    public Link visit(ExistentialCCT cct, Binding binding) {
        QuantifiedFormula f = (QuantifiedFormula) cct.getFormula();
        ContextSet universe = f.getUniverse();
        Variable v = f.getVariable();
        if (!isStale(cct) && currentChange.getTargetSet().equals(universe.getId()) &&
                currentChange instanceof Checker.AddChange) {
            int r = 0;
            Link l = null;
            if (cct.getChild(currentChange.getContext()).getTV()) {
                binding.bind(v, currentChange.getContext());
                l = Link.cartesian(Link.of(Link.Type.SAT, v, currentChange.getContext()),
                        generate(cct.getChild(currentChange.getContext()), binding));
                binding.unbind(v);
                r = l.size();
            }
//            setIncrementalLinkCount(cct, r);
            Link old = cct.getLK();
            Link updated = Link.union(old, l);
            cct.setLK(updated);
            return updated;
        } else {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                CCT sub = qa.getCCT();
                if (sub.getTV()) {
                    binding.bind(v, qa.getContext());
                    Link subL = generate(sub, binding);
                    binding.unbind(v);
                    Link tmp = Link.cartesian(Link.of(Link.Type.SAT, v, qa.getContext()), subL);
                    r.unionWith(tmp);

                    incre += tmp.size();
                }
            }

//            setIncrementalLinkCount(cct, incre);

            cct.setLK(r);
            return r;
        }
    }

    @Override
    public Link visit(BFuncCCT cct, Binding binding) {
        cct.bind(binding);
        Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
//        setIncrementalLinkCount(cct, 0);
        cct.setLK(r);
        return r;
    }
}
