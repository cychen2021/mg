package xyz.cychen.ycc.impl.check.xpcc.ver3;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.pcc.PCCAdjuster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XPCC3Checker extends Checker {
    public XPCC3Checker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new XPCC3Evaluator(), new XPCC3Generator(), constraints);
        constraints.forEach((k, v) -> {
            if (v.getValue0() != null && v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else if (v.getValue0() != null) {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
        });
    }

    protected PCCAdjuster adjuster = new PCCAdjuster(builder);

    protected Map<String, CCT> ccts =  new HashMap<>();

    @Override
    public String getName() {
        return "PCC-MG-3";
    }

    protected void updateTimeCount(String constraintID, long start, long afterBuild, long afterEval,
                                   long afterGen, long overheadTime1, long overheadTime2) {
        updateStats(constraintID, afterBuild-start, afterEval-afterBuild-overheadTime1,
                afterGen-afterEval-overheadTime2, overheadTime1+overheadTime2);
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkAddChange(AddChange addChange) {
        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        String targetSet = addChange.getTargetSet();
        sets.get(targetSet).add(addChange.getContext());

        Set<String> related = setToConstraints.get(targetSet);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            boolean alreadIn = ccts.containsKey(constraintID);
            CCT cct;
            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END
            if (!alreadIn) {
                cct = builder.build(f);
                ccts.put(constraintID, cct);
            }
            else {
                cct = ccts.get(constraintID);
                adjuster.affect(f, addChange);
                adjuster.adjust(cct, addChange);
            }

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            ((XPCC3Evaluator) evaluator).setChange(addChange);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((XPCC3Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                truthValue = evaluator.evaluate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (constraints.get(constraintID).getValue0() == null || truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC3Generator) generator).setChange(addChange);
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3, 0, 0);
            ((XPCC3Evaluator) evaluator).clearOverhead();
            ((XPCC3Generator) generator).clearOverhead();
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkDelChange(DelChange delChange) {
        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        String targetSet = delChange.getTargetSet();
        sets.get(targetSet).remove(delChange.getContext());

        Set<String> related = setToConstraints.get(targetSet);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            boolean alreadIn = ccts.containsKey(constraintID);


            CCT cct;
            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END
            if (!alreadIn) {
                cct = builder.build(f);
                ccts.put(constraintID, cct);
            }
            else {
                cct = ccts.get(constraintID);
                adjuster.affect(f, delChange);
                adjuster.adjust(cct, delChange);
            }

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            ((XPCC3Evaluator) evaluator).setChange(delChange);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((XPCC3Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                truthValue = evaluator.evaluate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (constraints.get(constraintID).getValue0() == null || truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC3Generator) generator).setChange(delChange);
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3, 0, 0);
            ((XPCC3Evaluator) evaluator).clearOverhead();
            ((XPCC3Generator) generator).clearOverhead();
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }
}
