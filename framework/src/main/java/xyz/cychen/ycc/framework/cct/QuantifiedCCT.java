package xyz.cychen.ycc.framework.cct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;

import java.util.*;

public abstract class QuantifiedCCT extends CCT{
    protected Set<Context> newlyAdded = new HashSet<>();

    public void record(Context context) {
        newlyAdded.add(context);
    }

    public Set<Context> getNewlyAdded() {
        return newlyAdded;
    }

    public boolean obliterate(Context context) {
        return newlyAdded.remove(context);
    }

    public void clearNewlyAdded() {
        this.newlyAdded = new HashSet<>();
    }

    public boolean newlyAddedIsEmpty() {
        return newlyAdded == null || newlyAdded.size() == 0;
    }

    protected final Map<Context, CCT> children;

    public QuantifiedCCT(QuantifiedFormula formula, boolean initialTV, List<Pair<Context, CCT>> children) {
        super(formula, initialTV);
        this.children = new HashMap<>();
        children.forEach(p -> this.children.put(p.getValue0(), p.getValue1()));
    }

    @Override
    public List<Arrow> getChildren() {
        List<Arrow> result = new LinkedList<>();
        children.forEach(
                (ctx, cct) ->
                        result.add(QuantifiedArrow.of(ctx, cct))
        );
        return result;
    }

    public void addBranch(Context ctx, CCT cct) {
        assert ((QuantifiedFormula) formula).getUniverse().contains(ctx);
        children.put(ctx, cct);
    }

    public void deleteBranch(Context ctx) {
        assert children.containsKey(ctx);
        children.remove(ctx);
    }

    public CCT getChild(Context ctx) {
        return children.get(ctx);
    }

    public CCT getChild(String id) {
        return children.entrySet().stream().filter(e -> e.getKey().getId().equals(id)).findFirst().get().getValue();
    }
}
