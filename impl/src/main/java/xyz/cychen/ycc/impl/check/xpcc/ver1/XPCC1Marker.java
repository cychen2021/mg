package xyz.cychen.ycc.impl.check.xpcc.ver1;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.cct.*;

public class XPCC1Marker {
    public void mark(CCT cct, int chrono) {
        cct.setChrono(chrono);
        cct.setSCCT();
        if (cct instanceof AndCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof OrCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof ImpliesCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof NotCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof UniversalCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof ExistentialCCT ncct) {
            visit(ncct, chrono);
        }
        else if (cct instanceof BFuncCCT ncct) {
            visit(ncct, chrono);
        }
    }

    public Pair<Boolean, Boolean> binaryHelper(BinaryCCT cct) {
        boolean left = cct.getChildren().get(0).getCCT().getTV();
        boolean right = cct.getChildren().get(1).getCCT().getTV();
        return Pair.with(left, right);
    }

    public void visit(AndCCT cct, int timestamp) {
        var s = binaryHelper(cct);
        if (s.getValue0() == s.getValue1()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
        else if (!s.getValue0()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
        }
        else {
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
    }

    public void visit(ImpliesCCT cct, int timestamp) {
        var s = binaryHelper(cct);
        if (s.getValue0() != s.getValue1()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
        else if (!s.getValue0()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
        }
        else {
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
    }

    public void visit(OrCCT cct, int timestamp) {
        var s = binaryHelper(cct);
        if (s.getValue0() == s.getValue1()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
        else if (s.getValue0()) {
            mark(cct.getChildren().get(0).getCCT(), timestamp);
        }
        else {
            mark(cct.getChildren().get(1).getCCT(), timestamp);
        }
    }

    public void visit(NotCCT cct, int timestamp) {
        mark(cct.getChildren().get(0).getCCT(), timestamp);
    }

    public void visit(UniversalCCT cct, int timestamp) {
        for (var c: cct.getChildren()) {
            if (!c.getCCT().getTV()) {
                mark(c.getCCT(), timestamp);
            }
        }
    }

    public void visit(ExistentialCCT cct, int timestamp) {
        for (var c: cct.getChildren()) {
            if (c.getCCT().getTV()) {
                mark(c.getCCT(), timestamp);
            }
        }
    }

    public void visit(BFuncCCT cct, int timestamp) {

    }
}
