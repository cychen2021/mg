package xyz.cychen.ycc.framework.check;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.*;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.framework.formula.QuantifiedFormula;
import xyz.cychen.ycc.framework.measure.Statistics;

import java.util.*;

public abstract class Checker {
    public static class Change {
        private final String targetSet;
        private final Context context;

        public Change(String targetSet, Context context) {
            this.targetSet = targetSet;
            this.context = context;
        }

        public String getTargetSet() {
            return targetSet;
        }
        public Context getContext() {
            return context;
        }
    }

    public static class AddChange extends Change {
        public AddChange(String targetSet, Context context) {
            super(targetSet, context);
        }
    }

    public static class DelChange extends Change {
        public DelChange(String targetSet, Context context) {
            super(targetSet, context);
        }
    }

    protected final Builder builder;
    protected Evaluator evaluator;
    protected Generator generator;
    protected final Map<String, Pair<Boolean, Formula>> constraints;
    protected final Map<String, ContextSet> sets;
    protected final Map<String, Set<String>> setToConstraints;
    protected Map<String, Statistics> statistics;


    public Checker(Builder builder, Evaluator evaluator, Generator generator,
                   Map<String, Pair<Boolean, Formula>> constraints) {
        this.builder = builder;
        this.evaluator = evaluator;
        this.generator = generator;
        this.constraints = constraints;
        this.statistics = new HashMap<>();

        constraints.forEach((k, v) -> this.statistics.put(k, new Statistics()));

        this.sets = new HashMap<>();
        setToConstraints = new HashMap<>();
        for (var entry: constraints.entrySet()) {
            Set<ContextSet> relatedSets = collectConstraintSet(entry.getValue().getValue1());
            for (ContextSet set: relatedSets) {
                this.sets.put(set.getId(), set);

                Set<String> relatedConstraints;
                if (!setToConstraints.containsKey(set.getId())) {
                    relatedConstraints = new HashSet<>();
                    setToConstraints.put(set.getId(), relatedConstraints);
                }
                else {
                    relatedConstraints = setToConstraints.get(set.getId());
                }
                relatedConstraints.add(entry.getKey());
            }
        }
    }

    // TODO: Reimplement this to avoid efficiency issue
    private Set<ContextSet> collectConstraintSet(Formula formula) {
        Formula[] sub = formula.getChildren();
        Set<ContextSet> result = new HashSet<>();
        for (Formula s: sub) {
            result.addAll(collectConstraintSet(s));
        }

        if (formula instanceof QuantifiedFormula quantifiedFormula) {
            result.add(quantifiedFormula.getUniverse());
        }
        return result;
    }

    public Map<String, Statistics> getStatistics() {
        return statistics;
    }

    public void resetStatistics() {
        for (var k: constraints.keySet()) {
            this.statistics.put(k, new Statistics());
        }
    }

    protected CCT cct;

    public CCT getCCT() {
        return cct;
    }

    public abstract String getName();

    protected void updateStats(String constraintID, long buildTime, long evalTime, long genTime, long overheadTime) {
        var stat = statistics.get(constraintID);
        stat.updateBuildTime(buildTime);
        stat.updateEvalTime(evalTime);
        stat.updateGenTime(genTime);
        stat.updateOverheadTime(overheadTime);
    }

    protected abstract Map<String, Pair<Boolean, Link>> checkAddChange(AddChange addChange);
    protected abstract Map<String, Pair<Boolean, Link>> checkDelChange(DelChange delChange);
    protected Deducer deducer = new Deducer();

    protected Map<String, Pair<Boolean, Link>> checkInner(String targetSet) {
        throw new UnsupportedOperationException();
    }

    public List<Pair<String, Link>> check(String targetSet) {
        List<Pair<String, Link>> result = new LinkedList<>();
        Map<String, Pair<Boolean, Link>> checkResult = checkInner(targetSet);
        for (var p: checkResult.entrySet()) {
            Boolean target = constraints.get(p.getKey()).getValue0();
            boolean truthValue = p.getValue().getValue0();
            if (target == null || truthValue != target) {
                result.add(Pair.with(p.getKey(), p.getValue().getValue1()));
            }
        }
        return result;
    }

    public List<Pair<String, Link>> check(Change change) {
        List<Pair<String, Link>> result = new LinkedList<>();
        Map<String, Pair<Boolean, Link>> checkResult = null;
        if (change instanceof AddChange addChange) {
            checkResult = checkAddChange(addChange);
        }
        else if (change instanceof DelChange delChange) {
            checkResult = checkDelChange(delChange);
        }
        else {
            System.exit(-1);
        }
        for (var p: checkResult.entrySet()) {
            Boolean target = constraints.get(p.getKey()).getValue0();
            boolean truthValue = p.getValue().getValue0();
            if (target == null || truthValue != target) {
                result.add(Pair.with(p.getKey(), p.getValue().getValue1()));
            }
        }
        return result;
    }

    public Map<String, ContextSet> getSets() {
        return sets;
    }

    protected void updateTimeCount(String constraintID, long start, long afterBuild, long afterEval, long afterGen) {
        updateStats(constraintID, afterBuild-start, afterEval-afterBuild, afterGen-afterEval, 0);
    }

    private void updateStats(String constraintID, Statistics stat) {
        statistics.get(constraintID).update(stat);
    }
}
