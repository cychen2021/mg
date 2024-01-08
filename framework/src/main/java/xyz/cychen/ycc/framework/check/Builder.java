package xyz.cychen.ycc.framework.check;

import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.formula.*;

public abstract class Builder {
    public CCT build(Formula formula) {
        if (formula instanceof AndFormula andFormula) {
            return visit(andFormula);
        }
        else if (formula instanceof OrFormula orFormula) {
            return visit(orFormula);
        }
        else if (formula instanceof ImpliesFormula implFormula) {
            return visit(implFormula);
        }
        else if (formula instanceof NotFormula notFormula) {
            return visit(notFormula);
        }
        else if (formula instanceof UniversalFormula univFormula) {
            return visit(univFormula);
        }
        else if (formula instanceof ExistentialFormula exisFormula) {
            return visit(exisFormula);
        }
        else if (formula instanceof BFuncFormula bfuncFormula) {
            return visit(bfuncFormula);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public abstract AndCCT visit(AndFormula formula);
    public abstract OrCCT visit(OrFormula formula);
    public abstract ImpliesCCT visit(ImpliesFormula formula);
    public abstract NotCCT visit(NotFormula formula);
    public abstract UniversalCCT visit(UniversalFormula formula);
    public abstract ExistentialCCT visit(ExistentialFormula formula);
    public abstract BFuncCCT visit(BFuncFormula formula);
}
