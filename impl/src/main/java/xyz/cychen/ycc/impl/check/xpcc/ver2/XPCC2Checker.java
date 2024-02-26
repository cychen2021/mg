package xyz.cychen.ycc.impl.check.xpcc.ver2;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XPCC2Checker extends Checker {
    public XPCC2Checker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new XPCC2Evaluator(), new XPCC2Generator(), constraints);
        constraints.forEach((k, v) -> {
            if (v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
        });
        this.measure = new IncrementalMeasure();
    }

    protected PCCAdjuster adjuster = new PCCAdjuster(builder);

    protected Map<String, CCT> ccts =  new HashMap<>();

    @Override
    public String getName() {
        return "PCC-MG-2";
    }

    protected void updateTimeCount(String constraintID, long start, long afterBuild, long afterEval,
                                   long afterGen, long overhead1, long overhead2) {
        updateStats(constraintID, afterBuild-start, afterEval-afterBuild-overhead1,
                afterGen-afterEval-overhead2, overhead1+overhead2);
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

            ((XPCC2Evaluator) evaluator).setChange(addChange);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((XPCC2Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                ((XPCC2Evaluator) evaluator).setChange(addChange);
                truthValue = evaluator.evaluate(cct, new Binding());
            }
            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC2Generator) generator).setChange(addChange);
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            //TODO
            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3, 0, 0);
//            updateMeasure(constraintID, cct);
            ((XPCC2Evaluator) evaluator).clearOverhead();
            ((XPCC2Generator) generator).clearOverhead();
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

            ((XPCC2Evaluator) evaluator).setChange(delChange);
            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((XPCC2Evaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                truthValue = evaluator.evaluate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                ((XPCC2Generator) generator).setChange(delChange);
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
//            updateMeasure(constraintID, cct);
            ((XPCC2Evaluator) evaluator).clearOverhead();
            ((XPCC2Generator) generator).clearOverhead();
            // DEBUG: END
        }

        return result;
    }
}
