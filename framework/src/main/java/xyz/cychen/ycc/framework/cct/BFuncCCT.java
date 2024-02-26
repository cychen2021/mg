package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.formula.BFuncFormula;

import java.util.ArrayList;
import java.util.List;

public class BFuncCCT extends CCT{
    public BFuncCCT(BFuncFormula formula) {
        super(formula, false);
        this.arguments = new Context[formula.getParameters().length];
    }

    protected Context[] arguments;

    public void bind(Binding binding) {
        Variable[] params = ((BFuncFormula) formula).getParameters();
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = binding.get(params[i]);
        }
    }

    public boolean evaluate() {
        return ((BFuncFormula) formula).evaluate(arguments);
    }

    private final ArrayList<Arrow> emptyChildren = new ArrayList<>();

    @Override
    public List<Arrow> getChildren() {
        return emptyChildren;
    }
}
