package xyz.cychen.ycc.impl.check.opcc;

import xyz.cychen.ycc.framework.*;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.cct.ExistentialCCT;
import xyz.cychen.ycc.framework.cct.UniversalCCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.impl.check.pcc.PCCGenerator;

public class OPCCGenerator extends PCCGenerator {
    public OPCCGenerator(Generator totalGen) {
        super(totalGen);
    }

    @Override
    public Link visit(UniversalCCT cct, Binding binding) {
        ContextSet set = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        Variable variable = ((QuantifiedFormula) cct.getFormula()).getVariable();
        boolean setChange = currentChange.getTargetSet().equals(set.getId());
        boolean subAf = cct.getFormula().getChildren()[0].isAffected();
        Context ctx = currentChange.getContext();
        if (!setChange && !subAf) {
//            cct.setIncrementalCount(0);
            return cct.getLK();
        } else if (cct.getFormula().getGoal() != Goal.VIO) {
//            setIncrementalLinkCount(cct, 0);

            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
            cct.setLK(r);
            return r;
        } else if (setChange && currentChange instanceof Checker.AddChange && !subAf) {
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
//        if (cct.getFormula().getGoal() != Goal.VIO) {
//            setIncrementalLinkCount(cct, 0);
//
//            return Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
//        } else {
//            return super.visit(cct, binding);
//        }
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
        } else if (cct.getFormula().getGoal() != Goal.SAT) {
            //setIncrementalLinkCount(cct, 0);
            Link r = Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
            cct.setLK(r);
            return r;
        } else if (setChange && currentChange instanceof Checker.AddChange && !subAf) {
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
//        if (cct.getFormula().getGoal() != Goal.SAT) {
//            setIncrementalLinkCount(cct, 0);
//            return Link.of(cct.getTV() ? Link.Type.SAT : Link.Type.VIO);
//        } else {
//            return super.visit(cct, binding);
//        }
    }
}
