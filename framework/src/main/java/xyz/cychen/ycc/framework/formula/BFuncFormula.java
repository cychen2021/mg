package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.Predicate;

import java.util.Arrays;

public class BFuncFormula extends Formula {
    protected final Variable[] parameters;

    @Deprecated
    protected final Context[] arguments;

    protected final Predicate predicate;

    public Variable[] getParameters() {
        return parameters;
    }

    public BFuncFormula(Predicate predicate, Variable ...paras) {
        this.parameters = paras;
        this.predicate = predicate;
        this.arguments = new Context[paras.length];
    }

    @Deprecated
    public void bind(Binding binding) {
        for (int i = 0; i < parameters.length; i++) {
            arguments[i] = binding.get(parameters[i]);
        }
    }

    public Context[] bindAndReturn(Binding binding) {
        Context[] result = new Context[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            result[i] = binding.get(parameters[i]);
        }
        return  result;
    }

    @Deprecated
    public void unbind() {
        Arrays.fill(arguments, null);
    }

    @Deprecated
    public boolean evaluate() {
        return predicate.testOn(arguments);
    }

    public boolean evaluate(Context[] arguments) {
        return predicate.testOn(arguments);
    }

    @Override
    public void analyzeEConditions() {
        posConditions = new EConditionStore();
        negConditions = new EConditionStore();
    }

    @Override
    public Formula[] getChildren() {
        return new Formula[0];
    }

    @Override
    public int hashCode() {
        int paraHash = Arrays.hashCode(parameters);
        int predicateHash = predicate.hashCode();
        return Arrays.hashCode(
            new int[]{FormulaType.BFUNC.ordinal(), paraHash, predicateHash}
        );
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    protected Formula clone() throws CloneNotSupportedException {
        return new BFuncFormula(predicate, parameters.clone());
    }
}
