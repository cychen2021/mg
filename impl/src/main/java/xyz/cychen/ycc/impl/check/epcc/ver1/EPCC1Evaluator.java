package xyz.cychen.ycc.impl.check.epcc.ver1;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

public class EPCC1Evaluator extends Evaluator {
    private int chrono;

    public void setChrono(int chrono) {
        this.chrono = chrono;
    }

    protected Checker.Change currentChange = null;
    protected Evaluator totalEval;

    public EPCC1Evaluator(Evaluator totalEval) {
        this.totalEval = totalEval;
    }

    public void setChange(Checker.Change change) {
        this.currentChange = change;
    }

    private boolean isAffected(CCT cct) {
        boolean r = cct.isAffectedForEPCC();
        cct.setAffectedForEPCC(false);
        return r;
    }

    private boolean isNewlyAdded(CCT cct) {
        boolean r = cct.isNewlyAddedForEPCC();
        cct.setNewlyAddedForEPCC(false);
        return r;
    }

//    private boolean isAnchor(CCT cct, CCT parent) {
//        return chrono - 1 != -1 && cct.getChrono() != chrono - 1 && (parent == null || parent.getChrono() == chrono - 1);
//    }
    private boolean isOnSCCT(CCT cct) {
        return chrono - 1 != -1 && cct.getChrono() == chrono - 1;
    }

    public boolean realEvaluate(CCT cct, Binding binding) {
        if (chrono == 0) {
            return totalEvaluate(cct, binding);
        }

        if (!isOnSCCT(cct)) {
            if (cct.getEConditions().hit(currentChange)) {
                return cct.getTV();
            } else {
                return evaluate(cct, binding);
            }
        }
        return evaluate(cct, binding);
    }

    public boolean totalEvaluate(CCT cct, Binding binding) {
        return totalEval.evaluate(cct, binding);
    }

    private Pair<CCT, CCT> binaryHelper(BinaryCCT cct) {
        var children = cct.getChildren();
        return Pair.with(children.get(0).getCCT(), children.get(1).getCCT());
    }

    @Override
    public boolean visit(AndCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
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
        } else if (isOnSCCT(children.getValue0()) && !isOnSCCT(children.getValue1())) {
            boolean left;
            if (!isAffected(children.getValue0())) {
                left = children.getValue0().getTV();
            } else {
                left = evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (children.getValue1().getEConditions().hit(currentChange)) {
                right = children.getValue1().getTV();
            } else {
                right = totalEval.evaluate(children.getValue1(), binding);
            }
            boolean tv = left && right;
            cct.setTV(tv);
            return tv;
        }  else if (!isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            boolean left;
            if (children.getValue0().getEConditions().hit(currentChange)) {
                left = children.getValue0().getTV();
            } else {
                left = totalEval.evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (!isAffected(children.getValue1())) {
                right = children.getValue1().getTV();
            } else {
                right = evaluate(children.getValue1(), binding);
            }
            boolean tv = left && right;
            cct.setTV(tv);
            return tv;
        } else {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
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
    }

    @Override
    public boolean visit(OrCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
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
                System.err.println("A multiple change happens!");
                System.exit(-1);
                return false;
            }
        } else if (isOnSCCT(children.getValue0()) && !isOnSCCT(children.getValue1())) {
            boolean left;
            if (!isAffected(children.getValue0())) {
                left = children.getValue0().getTV();
            } else {
                left = evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (children.getValue1().getEConditions().hit(currentChange)) {
                right = children.getValue1().getTV();
            } else {
                right = totalEval.evaluate(children.getValue1(), binding);
            }
            boolean tv = left || right;
            cct.setTV(tv);
            return tv;
        }  else if (!isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            boolean left;
            if (children.getValue0().getEConditions().hit(currentChange)) {
                left = children.getValue0().getTV();
            } else {
                left = totalEval.evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (!isAffected(children.getValue1())) {
                right = children.getValue1().getTV();
            } else {
                right = evaluate(children.getValue1(), binding);
            }
            boolean tv = left || right;
            cct.setTV(tv);
            return tv;
        } else {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
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
                System.err.println("A multiple change happens!");
                System.exit(-1);
                return false;
            }
        }
    }

    @Override
    public boolean visit(ImpliesCCT cct, Binding binding) {
        var children = binaryHelper(cct);
        if (isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                boolean nsub = evaluate(children.getValue0(), binding);
                boolean tv = !nsub || children.getValue1().getTV();
                cct.setTV(tv);
                return tv;
            }
            else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
                boolean nsub = evaluate(children.getValue1(), binding);
                boolean tv = nsub || !children.getValue0().getTV();
                cct.setTV(tv);
                return tv;
            }
            else {
                System.err.println("A multiple change happens!");
                System.exit(-1);
                return false;
            }
        } else if (isOnSCCT(children.getValue0()) && !isOnSCCT(children.getValue1())) {
            boolean left;
            if (!isAffected(children.getValue0())) {
                left = children.getValue0().getTV();
            } else {
                left = evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (children.getValue1().getEConditions().hit(currentChange)) {
                right = children.getValue1().getTV();
            } else {
                right = totalEval.evaluate(children.getValue1(), binding);
            }
            boolean tv = !left || right;
            cct.setTV(tv);
            return tv;
        }  else if (!isOnSCCT(children.getValue0()) && isOnSCCT(children.getValue1())) {
            boolean left;
            if (children.getValue0().getEConditions().hit(currentChange)) {
                left = children.getValue0().getTV();
            } else {
                left = totalEval.evaluate(children.getValue0(), binding);
            }
            boolean right;
            if (!isAffected(children.getValue1())) {
                right = children.getValue1().getTV();
            } else {
                right = evaluate(children.getValue1(), binding);
            }
            boolean tv = !left || right;
            cct.setTV(tv);
            return tv;
        } else {
            if (!isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                return cct.getTV();
            }
            else if (isAffected(children.getValue0()) && !isAffected(children.getValue1())) {
                boolean nsub = evaluate(children.getValue0(), binding);
                boolean tv = !nsub || children.getValue1().getTV();
                cct.setTV(tv);
                return tv;
            }
            else if (!isAffected(children.getValue0()) && isAffected(children.getValue1())) {
                boolean nsub = evaluate(children.getValue1(), binding);
                boolean tv = nsub || !children.getValue0().getTV();
                cct.setTV(tv);
                return tv;
            }
            else {
                System.err.println("A multiple change happens!");
                System.exit(-1);
                return false;
            }
        }
    }

    @Override
    public boolean visit(NotCCT cct, Binding binding) {
        var arrows = cct.getChildren();
        var child = arrows.get(0).getCCT();
        if (!isAffected(child)) {
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
        boolean isOnSCCT = isOnSCCT(cct);
        if (thisSet && currentChange instanceof Checker.AddChange) {
            if (isOnSCCT) {
                binding.bind(variable, ctx);
                var child = cct.getChild(ctx);
                boolean subtv;
                if (isNewlyAdded(child)) {
                    subtv = totalEval.evaluate(child, binding);
                } else {
                    throw new RuntimeException();
                }
                binding.unbind(variable);
                boolean currentTV = cct.getTV();
                boolean ntv = subtv && currentTV;
                cct.setTV(ntv);
                return ntv;
            } else {
                boolean ntv = true;
                for (var c: cct.getChildren()) {
                    boolean subtv;
                    binding.bind(variable, ((CCT.QuantifiedArrow) c).getContext());
                    if (isAffected(c.getCCT())) {
                        subtv = evaluate(c.getCCT(), binding);
                    } else if (isNewlyAdded(c.getCCT())) {
                        subtv = totalEval.evaluate(c.getCCT(), binding);
                    } else {
                        subtv = c.getCCT().getTV();
                    }
                    binding.unbind(variable);
                    ntv = ntv && subtv;
                }
                cct.setTV(ntv);
                return ntv;
            }
        } else if (thisSet && currentChange instanceof Checker.DelChange) {
            boolean init = true;
            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                boolean subtv;
                if (isOnSCCT) {
                    subtv = a.getCCT().getTV();
                } else {
                    if (isAffected(a.getCCT())) {
                        binding.bind(variable, qa.getContext());
                        subtv = evaluate(a.getCCT(), binding);
                        binding.unbind(variable);
                    } else if (isNewlyAdded(a.getCCT())) {
                        binding.bind(variable, qa.getContext());
                        subtv = totalEval.evaluate(a.getCCT(), binding);
                        binding.unbind(variable);
                    } else {
                        subtv = a.getCCT().getTV();
                    }
                }
                init = init && subtv;
            }
            cct.setTV(init);
            return init;
        } else if (!thisSet) {
            boolean init = true;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV;
                if (isOnSCCT(cct) && !isOnSCCT(a.getCCT())) {
                    if (a.getCCT().getEConditions().hit(currentChange)) {
                        subTV = a.getCCT().getTV();
                    } else {
                        if (isAffected(a.getCCT())) {
                            subTV = evaluate(a.getCCT(), binding);
                        } else if (isNewlyAdded(a.getCCT())) {
                            subTV = totalEval.evaluate(a.getCCT(), binding);
                        } else {
                            subTV = a.getCCT().getTV();
                        }
                    }
                } else if (isOnSCCT(cct) && isOnSCCT(a.getCCT())) {
                    if (isAffected(a.getCCT())) {
                        subTV = evaluate(a.getCCT(), binding);
                    } else {
                        subTV = a.getCCT().getTV();
                    }
                } else if (!isOnSCCT(cct) && !isOnSCCT(a.getCCT())) {
                    if (isNewlyAdded(a.getCCT())) {
                        subTV = totalEval.evaluate(a.getCCT(), binding);
                    } else if (isAffected(a.getCCT())) {
                        subTV = evaluate(a.getCCT(), binding);
                    } else {
                        subTV = a.getCCT().getTV();
                    }
                } else {
                    throw new RuntimeException();
                }
                init = init && subTV;
                binding.unbind(variable);
            }
            cct.setTV(init);
            return init;
        } else {
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
        boolean isOnSCCT = isOnSCCT(cct);
        if (thisSet && currentChange instanceof Checker.AddChange) {
            if (isOnSCCT) {
                var child = cct.getChild(ctx);
                binding.bind(variable, ctx);
                boolean subtv;
                if (isNewlyAdded(child)) {
                     subtv = totalEval.evaluate(cct.getChild(ctx), binding);
                } else {
                    throw new RuntimeException();
                }
                binding.unbind(variable);
                boolean currentTV = cct.getTV();
                boolean ntv = subtv || currentTV;
                cct.setTV(ntv);
                return ntv;
            } else {
                boolean ntv = false;
                for (var c: cct.getChildren()) {
                    boolean subtv;
                    binding.bind(variable, ((CCT.QuantifiedArrow) c).getContext());
                    if (isAffected(c.getCCT())) {
                        subtv = evaluate(c.getCCT(), binding);
                    } else if (isNewlyAdded(c.getCCT())) {
                        subtv = totalEval.evaluate(c.getCCT(), binding);
                    } else {
                        subtv = c.getCCT().getTV();
                    }
                    binding.unbind(variable);
                    ntv = ntv || subtv;
                }
                cct.setTV(ntv);
                return ntv;
            }
        } else if (thisSet && currentChange instanceof Checker.DelChange) {
            boolean init = false;
            for (var a: cct.getChildren()) {
                var qa = (CCT.QuantifiedArrow) a;
                boolean subtv;
                if (isOnSCCT) {
                    subtv = a.getCCT().getTV();
                } else {
                    if (isAffected(a.getCCT())) {
                        binding.bind(variable, qa.getContext());
                        subtv = evaluate(a.getCCT(), binding);
                        binding.unbind(variable);
                    } else if (isNewlyAdded(a.getCCT())) {
                        binding.bind(variable, qa.getContext());
                        subtv = totalEval.evaluate(a.getCCT(), binding);
                        binding.unbind(variable);
                    } else {
                        subtv = a.getCCT().getTV();
                    }
                }
                init = init || subtv;
            }
            cct.setTV(init);
            return init;
        } else if (!thisSet) {
            boolean init = false;
            for (var a: cct.getChildren()) {
                binding.bind(variable, ((CCT.QuantifiedArrow) a).getContext());
                boolean subTV;
                if (isOnSCCT(cct) && !isOnSCCT(a.getCCT())) {
                    if (a.getCCT().getEConditions().hit(currentChange)) {
                        subTV = a.getCCT().getTV();
                    } else {
                        if (isAffected(a.getCCT())) {
                            subTV = evaluate(a.getCCT(), binding);
                        } else if (isNewlyAdded(a.getCCT())) {
                            subTV = totalEval.evaluate(a.getCCT(), binding);
                        } else {
                            subTV = a.getCCT().getTV();
                        }
                    }
                } else if (isOnSCCT(cct) && isOnSCCT(a.getCCT())) {
                    if (isAffected(a.getCCT())) {
                        subTV = evaluate(a.getCCT(), binding);
                    } else {
                        subTV = a.getCCT().getTV();
                    }
                } else if (!isOnSCCT(cct) && !isOnSCCT(a.getCCT())) {
                    if (isNewlyAdded(a.getCCT())) {
                        subTV = totalEval.evaluate(a.getCCT(), binding);
                    } else if (isAffected(a.getCCT())) {
                        subTV = evaluate(a.getCCT(), binding);
                    } else {
                        subTV = a.getCCT().getTV();
                    }
                } else {
                    throw new RuntimeException();
                }
                init = init || subTV;
                binding.unbind(variable);
            }
            cct.setTV(init);
            return init;
        } else {
            System.err.println("A multiple change happens!");
            System.exit(-1);
            return false;
        }
    }

    @Override
    public boolean visit(BFuncCCT cct, Binding binding) {
        return cct.getTV();
    }
}
