package xyz.cychen.ycc.framework;

import xyz.cychen.ycc.framework.formula.*;

public class Deducer {
    public void deduce(Formula formula, Goal goal) {
//        formula.putProperty(Formula.PROP_GL, goal);
        formula.setGoal(goal);
        if (formula instanceof AndFormula andFormula) {
            visit(andFormula, goal);
        } else if (formula instanceof OrFormula orFormula) {
            visit(orFormula, goal);
        } else if (formula instanceof ImpliesFormula impliesFormula) {
            visit(impliesFormula, goal);
        } else if (formula instanceof NotFormula notFormula) {
            visit(notFormula, goal);
        } else if (formula instanceof UniversalFormula universalFormula) {
            visit(universalFormula, goal);
        } else if (formula instanceof ExistentialFormula existentialFormula) {
            visit(existentialFormula, goal);
        } else if (formula instanceof BFuncFormula bFuncFormula) {
            visit(bFuncFormula, goal);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void visit(AndFormula formula, Goal goal) {
        deduce(formula.getChildren()[0], goal);
        deduce(formula.getChildren()[1], goal);
    }

    public void visit(OrFormula formula, Goal goal) {
        deduce(formula.getChildren()[0], goal);
        deduce(formula.getChildren()[1], goal);
    }

    public void visit(ImpliesFormula formula, Goal goal) {
        deduce(formula.getChildren()[0], Goal.flip(goal));
        deduce(formula.getChildren()[1], goal);
    }

    public void visit(NotFormula formula, Goal goal) {
        deduce(formula.getChildren()[0], Goal.flip(goal));
    }

    public void visit(UniversalFormula formula, Goal goal) {
        if (goal == Goal.VIO) {
            deduce(formula.getChildren()[0], goal);
        } else {
            deduce(formula.getChildren()[0], Goal.NULL);
        }
    }

    public void visit(ExistentialFormula formula, Goal goal) {
        if (goal == Goal.SAT) {
            deduce(formula.getChildren()[0], goal);
        } else {
            deduce(formula.getChildren()[0], Goal.NULL);
        }
    }

    public void visit(BFuncFormula formula, Goal goal) {

    }
}
