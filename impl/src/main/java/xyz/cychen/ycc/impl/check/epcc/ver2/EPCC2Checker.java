package xyz.cychen.ycc.impl.check.epcc.ver2;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.framework.measure.IncrementalMeasure;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;
import xyz.cychen.ycc.impl.check.pcc.PCCAdjuster;
import xyz.cychen.ycc.impl.check.pcc.PCCEvaluator;
import xyz.cychen.ycc.impl.check.xpcc.ver1.XPCC1Generator;
import xyz.cychen.ycc.impl.check.xpcc.ver1.XPCC1Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EPCC2Checker extends Checker {
    public EPCC2Checker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new EPCC2Evaluator(new ECCEvaluator()), new XPCC1Generator(), constraints);
        constraints.forEach((k, v) -> {
            timestamp.put(k, 0);
            if (v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
            v.getValue1().analyzeEConditions();
        });

        this.measure = new IncrementalMeasure();
    }

    protected Map<String, Integer> timestamp = new HashMap<>();

    protected PCCAdjuster adjuster = new PCCAdjuster(builder);

    protected XPCC1Marker marker = new XPCC1Marker();

    protected Map<String, CCT> ccts =  new HashMap<>();

    @Override
    public String getName() {
        return "PCC-MG+-2";
    }

    protected void updateTimeCount(String constraintID, long start, long afterBuild, long afterEval,
                                   long afterGen, long afterMark) {
        updateStats(constraintID, afterBuild-start, afterEval-afterBuild,
                afterGen-afterEval, 0);
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

            int ts = timestamp.get(constraintID);

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            ((EPCC2Evaluator) evaluator).setChange(addChange);
            ((EPCC2Evaluator) evaluator).setChrono(ts);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((EPCC2Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                if (((EPCC2Evaluator) evaluator).isAnchor(cct, null) && cct.getEConditions().hit(addChange)) {
                    truthValue = cct.getTV();
                    ((EPCC2Evaluator) evaluator).getUpdater().update(cct, new Binding());
                } else {
                    truthValue = evaluator.evaluate(cct, new Binding());
                }
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            ((XPCC1Generator) generator).setChrono(ts);
            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC1Generator) generator).setChange(addChange);
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            // DEBUG: BEGIN
//            long time3 = System.nanoTime();
            // DEBUG: END

            if (truthValue != constraints.get(constraintID).getValue0()) {
                marker.mark(cct, ts);
            }

            timestamp.put(constraintID, ts + 1);

            // DEBUG: BEGIN
            long time4 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time4, 0);
//            updateMeasure(constraintID, cct);
            // DEBUG: END
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

            int ts = timestamp.get(constraintID);
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

            ((EPCC2Evaluator) evaluator).setChange(delChange);
            ((EPCC2Evaluator) evaluator).setChrono(ts);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((EPCC2Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                if (((EPCC2Evaluator) evaluator).isAnchor(cct, null) && cct.getEConditions().hit(delChange)) {
                    truthValue = cct.getTV();
                    ((EPCC2Evaluator) evaluator).getUpdater().update(cct, new Binding());
                } else {
                    truthValue = evaluator.evaluate(cct, new Binding());
                }
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            ((XPCC1Generator) generator).setChrono(ts);
            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC1Generator) generator).setChange(delChange);
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

//            // DEBUG: BEGIN
//            long time3 = System.nanoTime();
//            // DEBUG: END


            if (truthValue != constraints.get(constraintID).getValue0()) {
                marker.mark(cct, ts);
            }

            timestamp.put(constraintID, ts + 1);

            // DEBUG: BEGIN
            long time4 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time4, 0);
//            updateMeasure(constraintID, cct);
            // DEBUG: END
        }

        return result;
    }
}
