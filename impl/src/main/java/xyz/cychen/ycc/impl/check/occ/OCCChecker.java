package xyz.cychen.ycc.impl.check.occ;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.*;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OCCChecker extends Checker{

    private OCCChecker inner1 = null;
    private OCCChecker inner2 = null;

    boolean testMode;

    public OCCChecker(Map<String, Pair<Boolean, Formula>> constraints) {
        this(constraints, null, false);
    }

    public OCCChecker(Map<String, Pair<Boolean, Formula>> constraints, Boolean defaultGoal, boolean testMode) {
        super(new ECCBuilder(), new ECCEvaluator(), new OCCGenerator(), constraints);
        this.testMode = testMode;
        if (!testMode) {
            constraints.forEach((k, v) -> {
                if (v.getValue0() == null) {
                    if (defaultGoal) {
                        deducer.deduce(v.getValue1(), Goal.VIO);
                    }
                    else {
                        deducer.deduce(v.getValue1(), Goal.SAT);
                    }
                }
                else if (v.getValue0()) {
                    deducer.deduce(v.getValue1(), Goal.VIO);
                }
                else {
                    deducer.deduce(v.getValue1(), Goal.SAT);
                }
            });
        } else {
            inner1 = new OCCChecker(constraints, true, false);

            Map<String, Pair<Boolean, Formula>> constraints1 = new HashMap<>();
            for (String key: constraints.keySet()) {
                try {
                    constraints1.put(key, new Pair<>(constraints.get(key).getValue0(), constraints.get(key).getValue1().deepClone()));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

            inner2 = new OCCChecker(constraints1, false, false);
        }
    }

    @Override
    public String getName() {
        return "ECC-OG";
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkInner(String targetSet) {
        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        Set<String> related = setToConstraints.get(targetSet);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END

            CCT cct = builder.build(f);

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            boolean truthValue = evaluator.evaluate(cct, new Binding());

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link = generator.generate(cct, new Binding());

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3);
            // DEBUG: END
        }

        return result;
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkAddChange(Checker.AddChange addChange) {
        if (testMode) {
            var result1 = inner1.checkAddChange(addChange);
            var result2 = inner2.checkAddChange(addChange);
            Map<String, Pair<Boolean, Link>> result = new HashMap<>();
            for (var cst: constraints.keySet()) {
                boolean r = result1.get(cst).getValue0();
                result.put(cst, r ? result2.get(cst) : result1.get(cst));
            }
            return result;
        }

        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        String targetSet = addChange.getTargetSet();
        sets.get(targetSet).add(addChange.getContext());

        Set<String> related = setToConstraints.get(targetSet);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END

            CCT cct = builder.build(f);

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            boolean truthValue = evaluator.evaluate(cct, new Binding());

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link = generator.generate(cct, new Binding());

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3);
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkDelChange(Checker.DelChange delChange) {
        if (testMode) {
            var result1 = inner1.checkDelChange(delChange);
            var result2 = inner2.checkDelChange(delChange);
            Map<String, Pair<Boolean, Link>> result = new HashMap<>();
            for (var cst: constraints.keySet()) {
                boolean r = result1.get(cst).getValue0();
                result.put(cst, r ? result1.get(cst) : result2.get(cst));
            }
            return result;
        }

        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        String targetSet = delChange.getTargetSet();
        sets.get(targetSet).remove(delChange.getContext());

        Set<String> related = setToConstraints.get(targetSet);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END

            CCT cct = builder.build(f);

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            boolean truthValue = evaluator.evaluate(cct, new Binding());

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link = generator.generate(cct, new Binding());

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3);
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }
}
