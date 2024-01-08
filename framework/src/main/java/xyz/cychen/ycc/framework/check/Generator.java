package xyz.cychen.ycc.framework.check;

import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.*;

public abstract class Generator {
    public Link generate(CCT cct, Binding binding) {
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

    public abstract Link visit(AndCCT cct, Binding binding);
    public abstract Link visit(OrCCT cct, Binding binding);
    public abstract Link visit(ImpliesCCT cct, Binding binding);
    public abstract Link visit(NotCCT cct, Binding binding);
    public abstract Link visit(UniversalCCT cct, Binding binding);
    public abstract Link visit(ExistentialCCT cct, Binding binding);
    public abstract Link visit(BFuncCCT cct, Binding binding);
}
