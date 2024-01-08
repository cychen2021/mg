package xyz.cychen.ycc.framework.cct;

import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.formula.Formula;

import java.util.*;

public abstract class CCT {

    protected boolean toBeTotallyRecomputed = false;

    public void unsetToBeTotallyRecomputed() {
        toBeTotallyRecomputed = false;
    }

    protected boolean[] outdated = null;

    public boolean isOutdated() {
        if (outdated == null) {
            outdated = new boolean[2];
            outdated[0] = false;
            outdated[1] = true;
            return false;
        } else {
            return outdated[0];
        }
    }

//    boolean newlyAdded = true;

//    public boolean isNewlyAdded() {
//        return newlyAdded;
//    }
//
//    public void unsetNewlyAdded() {
//        this.newlyAdded = false;
//    }

    public void prepareOutdated() {
        if (outdated == null) {
            outdated = new boolean[2];
        }
        this.outdated[0] = this.outdated[1];
        this.outdated[1] = true;
    }

    public void unprepareOutdated() {
        this.outdated[0] = false;
        this.outdated[1] = false;
    }

    public boolean isAffectedForXPCC2() {
        return outdated[1];
    }

    protected Link link = null;

    public Link getLK() {
//        if (link == null) {
//            link = Link.of(tv ? Link.Type.SAT : Link.Type.VIO);
//        }
        return link;
    }

//    public Boolean getTVNoDefault() {
//        return tv;
//    }
    public boolean tvIsNull() {
        return tv == null;
    }

    public Link getLinkNoDefault() {
        return link;
    }

    protected int chrono;

    public void setChrono(int chrono) {
        this.chrono = chrono;
    }

    public int getChrono() {
        return chrono;
    }

    //    protected boolean affected;

//    public boolean isAffected() {
//        return affected;
//    }

    protected boolean scct = false;

    public void setSCCT() {
        this.scct = true;
    }

    public boolean isSCCT() {
        return this.scct;
    }

    public void clearSCCT() {
        this.scct = false;
        var children = this.getChildren();
        if (children != null && !children.isEmpty()) {
            for (var child : children) {
                child.getCCT().clearSCCT();
            }
        }
    }

    public abstract String toStringRepr(boolean tagSCCT);

    protected Boolean tv;

    public boolean getTV() {
        return tv == null ? initialTV : tv;
    }

    public void setTV(boolean tv) {
        this.tv = tv;
    }

    public void setLK(Link link) {
//        assert !((link.getType() == Link.Type.SAT && !getTV()) || (link.getType() == Link.Type.VIO && getTV()));
        this.link = link;
    }

    public boolean isToBeTotallyRecomputed() {
        return toBeTotallyRecomputed;
    }

    public void setToBeTotallyRecomputed() {
        this.toBeTotallyRecomputed = true;
        if (this instanceof QuantifiedCCT) {
            ((QuantifiedCCT) this).clearNewlyAdded();
        }
    }

    /**
     * An arrow is a possible direction to which the traversal
     * may get downward, together with the context assign of
     * that direction, if any.
     */
    public abstract static class Arrow {
        protected final CCT cct;

        protected Arrow(CCT cct) {
            this.cct = cct;
        }

        public CCT getCCT() {
            return cct;
        }
    }

    public static final class NormalArrow extends Arrow {
        private NormalArrow(CCT cct) {
            super(cct);
        }

        public static NormalArrow of(CCT cct) {
            return new NormalArrow(cct);
        }
    }

    public static final class QuantifiedArrow extends Arrow {
        private final Context context;

        private QuantifiedArrow(CCT cct, Context context) {
            super(cct);
            this.context = context;
        }

        public static QuantifiedArrow of(Context ctx, CCT cct) {
            return new QuantifiedArrow(cct, ctx);
        }

        public Context getContext() {
            return context;
        }
    }

//    protected final Map<String, Object> properties;
    protected boolean initialTV;

    protected final Formula formula;

    public CCT(Formula formula, boolean initialTV) {
        this.formula = formula;
//        this.properties = new HashMap<>();
        this.initialTV = initialTV;
        this.link = null;
        this.tv = null;
        this.chrono = -1;
    }

    public boolean isAffected() {
        return getFormula().isAffected();
    }

//    public boolean containsProperty(String key) {
//        return properties.containsKey(key);
//    }

//    public void removeProperty(String key) {
//        properties.remove(key);
//    }

    public Formula getFormula() {
        return formula;
    }

//    public Object getProperty(String key) {
//        return properties.get(key);
//    }

//    public void putProperty(String key, Object value) {
//        properties.put(key, value);
//    }

    public abstract List<Arrow> getChildren();
}
