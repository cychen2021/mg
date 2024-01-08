package xyz.cychen.ycc.framework.check;

import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.cct.*;

public abstract class Evaluator {
    public boolean evaluate(CCT cct, Binding binding) {
        if (cct instanceof AndCCT andCCT) {
            return visit(andCCT, binding);
        } else if (cct instanceof OrCCT orCCT) {
            return visit(orCCT, binding);
        } else if (cct instanceof ImpliesCCT implCCT) {
            return visit(implCCT, binding);
        } else if (cct instanceof NotCCT notCCT) {
            return visit(notCCT, binding);
        } else if (cct instanceof UniversalCCT univCCT) {
            return visit(univCCT, binding);
        } else if (cct instanceof ExistentialCCT exisCCT) {
            return visit(exisCCT, binding);
        } else if (cct instanceof BFuncCCT bfuncCCT) {
            return visit(bfuncCCT, binding);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public abstract boolean visit(AndCCT cct, Binding binding);
    public abstract boolean visit(OrCCT cct, Binding binding);
    public abstract boolean visit(ImpliesCCT cct, Binding binding);
    public abstract boolean visit(NotCCT cct, Binding binding);
    public abstract boolean visit(UniversalCCT cct, Binding binding);
    public abstract boolean visit(ExistentialCCT cct, Binding binding);
    public abstract boolean visit(BFuncCCT cct, Binding binding);
}
