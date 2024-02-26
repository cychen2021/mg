package xyz.cychen.ycc.framework.measure;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.*;

public class Measure {
    public Statistics measure(boolean constraint, CCT cct) {
        Statistics result = new Statistics();
        visit(cct, constraint != cct.getTV(), result);
        return result;
    }

    protected void visit(CCT cct, boolean onSCCT, Statistics result) {
        if (cct instanceof AndCCT andCCT) {
            visit(andCCT, onSCCT, result);
        } else if (cct instanceof OrCCT orCCT) {
            visit(orCCT, onSCCT, result);
        } else if (cct instanceof NotCCT notCCT) {
            visit(notCCT, onSCCT, result);
        } else if (cct instanceof ImpliesCCT impliesCCT) {
            visit(impliesCCT, onSCCT, result);
        } else if (cct instanceof UniversalCCT universalCCT) {
            visit(universalCCT, onSCCT, result);
        } else if (cct instanceof ExistentialCCT existentialCCT) {
            visit(existentialCCT, onSCCT, result);
        } else if (cct instanceof BFuncCCT bFuncCCT) {
            visit(bFuncCCT, onSCCT, result);
        } else {
            System.err.println("Unsupported CCT type: " + cct.getClass().getName());
            System.exit(-1);
        }
    }

    protected Pair<CCT, CCT> binaryHelper(BinaryCCT cct) {
        return Pair.with(cct.getChildren().get(0).getCCT(), cct.getChildren().get(1).getCCT());
    }

    protected void visit(AndCCT andCCT, boolean onSCCT, Statistics result) {
        var children = binaryHelper(andCCT);
        if (children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        } else if (children.getValue0().getTV() && !children.getValue1().getTV()) {
            visit(children.getValue0(), false, result);
            visit(children.getValue1(), onSCCT, result);
        } else if (!children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), false, result);
        } else {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        }
    }

    protected void visit(OrCCT orCCT, boolean onSCCT, Statistics result) {
        var children = binaryHelper(orCCT);
        if (children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        } else if (children.getValue0().getTV() && !children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), false, result);
        } else if (!children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), false, result);
            visit(children.getValue1(), onSCCT, result);
        } else {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        }
    }

    protected void visit(NotCCT notCCT, boolean onSCCT, Statistics result) {
        var child = notCCT.getChildren().get(0).getCCT();
        visit(child, onSCCT, result);
    }

    protected void visit(ImpliesCCT impliesCCT, boolean onSCCT, Statistics result) {
        var children = binaryHelper(impliesCCT);
        if (children.getValue0().getTV() && !children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        } else if (!children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), onSCCT, result);
        } else if (children.getValue0().getTV() && children.getValue1().getTV()) {
            visit(children.getValue0(), false, result);
            visit(children.getValue1(), onSCCT, result);
        } else {
            visit(children.getValue0(), onSCCT, result);
            visit(children.getValue1(), false, result);
        }
    }

    protected void visit(ExistentialCCT existentialCCT, boolean onSCCT, Statistics result) {
        for (var child : existentialCCT.getChildren()) {
            visit(child.getCCT(), onSCCT && child.getCCT().getTV(), result);
        }
    }

    protected void visit(UniversalCCT universalCCT, boolean onSCCT, Statistics result) {

        for (var child : universalCCT.getChildren()) {
            visit(child.getCCT(), onSCCT && !child.getCCT().getTV(), result);
        }
    }

    protected void visit(BFuncCCT bFuncCCT, boolean onSCCT, Statistics result) {
    }
}
