package xyz.cychen.ycc.impl.check.oconc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.impl.check.Closable;
import xyz.cychen.ycc.impl.check.conc.ConCBuilder;
import xyz.cychen.ycc.impl.check.conc.ConCEvaluator;
import xyz.cychen.ycc.impl.check.conc.ConCGenerator;
import xyz.cychen.ycc.impl.check.occ.OCCChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class OConCChecker extends Checker implements Closable {
    protected ThreadPoolExecutor executor;
    private boolean testMode = false;


    private OConCChecker inner1 = null;
    private OConCChecker inner2 = null;

    public OConCChecker(int paraNum, Map<String, Pair<Boolean, Formula>> constraints) {
        this(paraNum, constraints, null, false);
    }

    public OConCChecker(int paraNum, Map<String, Pair<Boolean, Formula>> constraints, Boolean defaultGoal, boolean testMode) {
        super(new ConCBuilder(), new ConCEvaluator(), new OConCGenerator(), constraints);
        this.testMode = testMode;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(paraNum);
        ((ConCEvaluator) evaluator).setExecutor(executor);
        ((OConCGenerator) generator).setExecutor(executor);
        ((ConCBuilder) builder).setExecutor(executor);
//        this.evaluator = new ConCEvaluator(executor);
//        this.generator = new ConCGenerator(executor);
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
            inner1 = new OConCChecker(paraNum, constraints, true, false);
            inner2 = new OConCChecker(paraNum, constraints, false, false);
        }
//        initExecutor(paraNum, constraints);
    }

    @Override
    public String getName() {
        return "ConC-OG";
    }

    public void shutdown() {
        if (testMode) {
            inner1.shutdown();
            inner2.shutdown();
        }
        executor.shutdown();
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkAddChange(AddChange addChange) {
        if (testMode) {
            var result1 = inner1.checkAddChange(addChange);
            var result2 = inner2.checkAddChange(addChange);
            Map<String, Pair<Boolean, Link>> result = new HashMap<>();
            for (var cst: constraints.keySet()) {
                boolean r = result1.get(cst).getValue0();
                result.put(cst, r ? result1.get(cst) : result2.get(cst));
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
    protected Map<String, Pair<Boolean, Link>> checkDelChange(DelChange delChange) {
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
