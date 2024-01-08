package xyz.cychen.ycc.framework.formula;

public abstract class BinaryFormula extends Formula {
    protected Formula left;
    protected Formula right;

    protected BinaryFormula(Formula left, Formula right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Formula[] getChildren() {
        return new Formula[]{left, right};
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    protected abstract Formula clone() throws CloneNotSupportedException;
}
