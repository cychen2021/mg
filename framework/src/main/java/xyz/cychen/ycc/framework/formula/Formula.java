package xyz.cychen.ycc.framework.formula;

import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.HashSet;
import java.util.Set;

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

    protected EConditionStore posConditions = null;
    protected EConditionStore negConditions = null;

    public EConditionStore getEConditions(boolean tv) {
        return tv ? posConditions : negConditions;
    }

    public abstract void analyzeEConditions();


    public static class EConditionStore {
        private final Set<String> posConditions;
        private final Set<String> negConditions;

        private EConditionStore sibling;

        public void add(boolean pos, String condition) {
            if (pos) {
                posConditions.add(condition);
            } else {
                negConditions.add(condition);
            }
        }

        public boolean isIrrelevantWith(Checker.Change change) {
            return !posConditions.contains(change.getTargetSet()) && !negConditions.contains(change.getTargetSet());
        }

        public boolean hit(Checker.Change change) {
            if (change instanceof Checker.AddChange) {
                if (posConditions.contains(change.getTargetSet())) {
                    return true;
                }
                if (sibling != null) {
                    if (sibling.posConditions.contains(change.getTargetSet())) {
                        return true;
                    }
                    return !negConditions.contains(change.getTargetSet()) && !sibling.negConditions.contains(change.getTargetSet());
                }
                return !negConditions.contains(change.getTargetSet());
            } else if (change instanceof Checker.DelChange) {
                if (negConditions.contains(change.getTargetSet())) {
                    return true;
                }
                if (sibling != null) {
                    if (sibling.negConditions.contains(change.getTargetSet())) {
                        return true;
                    }
                    return !posConditions.contains(change.getTargetSet()) && !sibling.posConditions.contains(change.getTargetSet());
                }
                return !posConditions.contains(change.getTargetSet());
            } else {
                throw new RuntimeException();
            }
        }

        private EConditionStore(Set<String> posConditions, Set<String> negConditions, EConditionStore sibling) {
            this.posConditions = posConditions;
            this.negConditions = negConditions;
            this.sibling = sibling;
        }

        public EConditionStore() {
            this(new HashSet<>(), new HashSet<>(), null);
        }

        public EConditionStore(EConditionStore sibling) {
            this(new HashSet<>(), new HashSet<>(), sibling);
        }

        public void linkTo(EConditionStore sibling) {
            this.sibling = sibling;
        }

        public EConditionStore union(EConditionStore other) {
            Set<String> pos = new HashSet<>(posConditions.size() + other.posConditions.size());
            pos.addAll(posConditions);
            pos.addAll(other.posConditions);
            Set<String> neg = new HashSet<>(negConditions.size() + other.negConditions.size());
            neg.addAll(negConditions);
            neg.addAll(other.negConditions);
            return new EConditionStore(pos, neg, null);
        }
    }
}
