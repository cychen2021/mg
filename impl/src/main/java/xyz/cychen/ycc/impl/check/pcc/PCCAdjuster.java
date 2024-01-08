package xyz.cychen.ycc.impl.check.pcc;

import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Builder;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.*;

public class PCCAdjuster {
    protected Builder builder;

    public PCCAdjuster(Builder builder) {
        this.builder = builder;
    }

    public boolean affect(Formula formula, Checker.Change change) {
        if (formula instanceof BinaryFormula) {
            boolean left = affect(formula.getChildren()[0], change);
            boolean right = affect(formula.getChildren()[1], change);
            boolean affected = left || right;
            formula.setAffected(affected);
            return affected;
        }
        else if (formula instanceof NotFormula) {
            boolean sub = affect(formula.getChildren()[0], change);
            formula.setAffected(sub);
            return sub;
        }
        else if (formula instanceof BFuncFormula) {
            formula.setAffected(false);
            return false;
        }
        else if (formula instanceof QuantifiedFormula quantifiedFormula) {
            boolean sub = affect(formula.getChildren()[0], change);
            boolean setChange = change.getTargetSet().equals(quantifiedFormula.getUniverse().getId());
            boolean affected = sub || setChange;
            formula.setAffected(affected);
            return affected;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public void adjust(CCT cct, Checker.Change change) {
        if (!cct.getFormula().isAffected()) {
            return;
        }
        if (cct instanceof AndCCT andCCT) {
            visit(andCCT, change);
        }
        else if (cct instanceof OrCCT orCCT) {
            visit(orCCT, change);
        }
        else if (cct instanceof ImpliesCCT impliesCCT) {
            visit(impliesCCT, change);
        }
        else if (cct instanceof NotCCT notCCT) {
            visit(notCCT, change);
        }
        else if (cct instanceof UniversalCCT universalCCT) {
            visit(universalCCT, change);
        }
        else if (cct instanceof ExistentialCCT existentialCCT) {
            visit(existentialCCT, change);
        }
        else if (cct instanceof BFuncCCT bFuncCCT) {
            visit(bFuncCCT, change);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    private void binaryHelper(BinaryCCT cct, Checker.Change change) {
        var children = cct.getChildren();
        var left = children.get(0).getCCT();
        var right = children.get(1).getCCT();
        adjust(left, change);
        adjust(right, change);
    }

    protected void visit(AndCCT cct, Checker.Change change) {
        binaryHelper(cct, change);
    }

    protected void visit(OrCCT cct, Checker.Change change) {
        binaryHelper(cct, change);
    }

    protected void visit(ImpliesCCT cct, Checker.Change change) {
        binaryHelper(cct, change);
    }

    protected void visit(NotCCT cct, Checker.Change change) {
        var child = cct.getChildren();
        var sub = child.get(0).getCCT();
        adjust(sub, change);
    }

    private void quantifiedHelper(QuantifiedCCT cct, Checker.Change change) {
        ContextSet universe = ((QuantifiedFormula) cct.getFormula()).getUniverse();
        var children = cct.getChildren();
        for (var arrow: children) {
            var ar = (CCT.QuantifiedArrow) arrow;
            adjust(ar.getCCT(), change);
        }

        if (change.getTargetSet().equals(universe.getId())) {
            if (change instanceof Checker.AddChange) {
                CCT newCCT = builder.build(cct.getFormula().getChildren()[0]);
                cct.addBranch(change.getContext(), newCCT);
            }
            else if (change instanceof Checker.DelChange) {
                cct.deleteBranch(change.getContext());
            }
        }
    }

    protected void visit(UniversalCCT cct, Checker.Change change) {
        quantifiedHelper(cct, change);
    }

    protected void visit(ExistentialCCT cct, Checker.Change change) {
        quantifiedHelper(cct, change);
    }

    protected void visit(BFuncCCT cct, Checker.Change change) {
    }
}
