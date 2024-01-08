package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.Goal;

public abstract class Formula implements Cloneable{
    protected enum FormulaType {
        AND,
        OR,
        IMPLIES,
        NOT,
        BFUNC,
        UNIVERSAL,
        EXISTENTIAL
    }

//    public static String PROP_GL = "goal";

//    public static final String PROP_AF = "affected";

    protected boolean affected = false;

    public boolean isAffected() {
        return affected;
    }

    public void setAffected() {
        this.affected = true;
    }

    public void setAffected(boolean affected) {
        this.affected = affected;
    }

    public void unsetAffected() {
        this.affected = false;
    }

//    protected final Map<String, Object> properties = new HashMap<>();
    protected Goal goal;

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    //    public Object getProperty(String key) {
//        return properties.get(key);
//    }

//    public void putProperty(String key, Object value) {
//        properties.put(key, value);
//    }

//    public void removeProperty(String key) {
//        properties.remove(key);
//    }

//    public boolean containsProperty(String key) {
//        return properties.containsKey(key);
//    }

    public abstract Formula[] getChildren();

    /**
     * This method get the hash code of a formula. It considers and & or
     * formulas' symmetry, but doesn't consider alpha and beta equivalence.
     * Don't invoke it too often, since the evaluation is recursive.
     * @return the hash code
     */
    @Override
    public abstract int hashCode();

    /**
     * This method decide whether a formula is equal to this formula.
     * It considers and & or formulas' symmetry, but doesn't consider
     * alpha and beta equivalence.
     * Don't invoke it too often, since the evaluation is recursive.
     * @param obj - the formula to be compared with
     * @return whether the formulas are equal
     */
    @Override
    public abstract boolean equals(Object obj);

    @Override
    protected abstract Formula clone() throws CloneNotSupportedException;

    public Formula deepClone() throws CloneNotSupportedException {
        return this.clone();
    }
}
