package xyz.cychen.ycc.impl.check.pcc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.*;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

public class PCCGenerator extends Generator {
    protected Generator totalGen;

    protected Checker.Change currentChange = null;

    public PCCGenerator(Generator totalGen) {
        this.totalGen = totalGen;
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

    private Pair<Boolean, Boolean> binaryTV(CCT cct1, CCT cct2) {
        return Pair.with(cct1.getTV(), cct2.getTV());
    }

    public Link totalGenerate(CCT cct, Binding binding) {
        return totalGen.generate(cct, binding);
    }

    @Override
    public Link visit(AndCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
//            cct.setIncrementalCount(0);

            return cct.getLK();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue0(), binding);
            if (tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.cartesian(nsub, children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK(nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.union(nsub, children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK(nLK);
                return nLK;
            }
            else if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.copy(children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue1(), binding);
            if (tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.cartesian(children.getValue0().getLK(), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.union(children.getValue0().getLK(), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.copy(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(children.getValue0().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public Link visit(OrCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
//            cct.setIncrementalCount(0);

            return cct.getLK();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue0(), binding);
            if (tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.union(nsub, children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.cartesian(nsub, children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.copy(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue1(), binding);
            if (tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.union(children.getValue0().getLK(), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.cartesian(children.getValue0().getLK(), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.copy(children.getValue0().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public Link visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
//            cct.setIncrementalCount(0);

            return cct.getLK();
        }
        else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue0(), binding);
            if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.cartesian(Link.flip(nsub), children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.union(Link.flip(nsub), children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.flip(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(children.getValue1().getLK());

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
            var tvs = binaryTV(children.getValue0(), children.getValue1());
            Link nsub = generate(children.getValue1(), binding);
            if (tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.cartesian(Link.flip(children.getValue0().getLK()), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && tvs.getValue1()) {
                Link nLK = Link.union(Link.flip(children.getValue0().getLK()), nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else if (!tvs.getValue0() && !tvs.getValue1()) {
                Link nLK = Link.flip(Link.copy(children.getValue0().getLK()));

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
            else {
                Link nLK = Link.copy(nsub);

//                setIncrementalLinkCount(cct, nLK.size());

                cct.setLK( nLK);
                return nLK;
            }
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public Link visit(NotCCT cct, Binding binding) {
        if (!isAffected(cct)) {
//            cct.setIncrementalCount(0);

            return cct.getLK();
        }
        else {
            Link subL = generate(cct.getChildren().get(0).getCCT(), binding);
            Link nLK = Link.flip(subL);

//            setIncrementalLinkCount(cct, nLK.size());

            cct.setLK( nLK);
            return nLK;
        }
    }

    @Override
    public Link visit(UniversalCCT cct, Binding binding) {
        ContextSet set = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean setChange = currentChange.getTargetSet().equals(set.getId());
        boolean subAf = cct.getFormula().getChildren()[0].isAffected();
        Context ctx = currentChange.getContext();
        if (!setChange && !subAf) {
            //cct.setIncrementalCount(0);
            return cct.getLK();
        }
        else if (setChange && currentChange instanceof Checker.AddChange && !subAf) {
            binding.bind(variable, ctx);
            Link subL = totalGen.generate(cct.getChild(ctx), binding);
            binding.unbind(variable);

            int r = 0;
            Link oldLink = cct.getLK();
            Link newLink;
            if (!cct.getChild(ctx).getTV()) {
                Link nLK = Link.cartesian(Link.of(Link.Type.VIO, variable, ctx), subL);
                newLink = Link.union(oldLink, nLK);

                r = nLK.size();
            } else {
                newLink = Link.copy(oldLink);
            }
//            setIncrementalLinkCount(cct, r);
            cct.setLK( newLink);
            return newLink;
        }
        else if (setChange && currentChange instanceof Checker.DelChange && !subAf) {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var arrow = (CCT.QuantifiedArrow) a;
                boolean subTV = arrow.getCCT().getTV();
                Link subL = arrow.getCCT().getLK();
                if (!subTV) {
                    Link nLK = Link.cartesian(Link.of(Link.Type.VIO, variable, arrow.getContext()), subL);
                    r.unionWith(nLK);

                    incre += nLK.size();
                }
            }
//            setIncrementalLinkCount(cct, incre);
            cct.setLK( r);
            return r;
        }
        else if (!setChange && subAf) {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var arrow = (CCT.QuantifiedArrow) a;
                boolean subTV = arrow.getCCT().getTV();
                binding.bind(variable, arrow.getContext());
                Link subL = generate(arrow.getCCT(), binding);

                binding.unbind(variable);
                if (!subTV) {
                    Link nLK = Link.cartesian(Link.of(Link.Type.VIO, variable, arrow.getContext()), subL);
                    r.unionWith(nLK);

                    incre += nLK.size();
                }
            }
//            setIncrementalLinkCount(cct, incre);
            cct.setLK( r);
            return r;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public Link visit(ExistentialCCT cct, Binding binding) {
        ContextSet set = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean setChange = currentChange.getTargetSet().equals(set.getId());
        boolean subAf = cct.getFormula().getChildren()[0].isAffected();
        Context ctx = currentChange.getContext();
        if (!setChange && !subAf) {
//            cct.setIncrementalCount(0);
            return cct.getLK();
        }
        else if (setChange && currentChange instanceof Checker.AddChange && !subAf) {
            binding.bind(variable, ctx);
            Link subL = totalGen.generate(cct.getChild(ctx), binding);
            binding.unbind(variable);

            int r = 0;

            Link oldLink = cct.getLK();
            Link newLink;
            if (cct.getChild(ctx).getTV()) {
                Link nLK = Link.cartesian(Link.of(Link.Type.SAT, variable, ctx), subL);
                newLink = Link.union(oldLink, nLK);

                r = nLK.size();
            } else {
                newLink = Link.copy(oldLink);
            }

//            setIncrementalLinkCount(cct, r);

            cct.setLK(newLink);
            return newLink;
        }
        else if (setChange && currentChange instanceof Checker.DelChange && !subAf) {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var arrow = (CCT.QuantifiedArrow) a;
                boolean subTV = arrow.getCCT().getTV();
                Link subL = arrow.getCCT().getLK();
                if (subTV) {
                    Link nLK = Link.cartesian(Link.of(Link.Type.SAT, variable, arrow.getContext()), subL);
                    r.unionWith(nLK);

                    incre += nLK.size();
                }
            }

//            setIncrementalLinkCount(cct, incre);

            cct.setLK( r);
            return r;
        }
        else if (!setChange && subAf) {
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);

            int incre = 0;

            for (var a: cct.getChildren()) {
                var arrow = (CCT.QuantifiedArrow) a;
                boolean subTV = arrow.getCCT().getTV();
                binding.bind(variable, arrow.getContext());
                Link subL = generate(arrow.getCCT(), binding);
                binding.unbind(variable);

                if (subTV) {
                    Link nLK = Link.cartesian(Link.of(Link.Type.SAT, variable, arrow.getContext()), subL);
                    r.unionWith(nLK);

                    incre += nLK.size();
                }
            }

//            setIncrementalLinkCount(cct, incre);

            cct.setLK( r);
            return r;
        }
        else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return null;
        }
    }

    @Override
    public Link visit(BFuncCCT cct, Binding binding) {
        cct.bind(binding);
//        setIncrementalLinkCount(cct, 0);
        return cct.getLK();
    }
}
