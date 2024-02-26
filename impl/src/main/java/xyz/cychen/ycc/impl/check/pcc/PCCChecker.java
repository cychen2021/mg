package xyz.cychen.ycc.impl.check.pcc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Builder;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.*;
import xyz.cychen.ycc.framework.measure.IncrementalMeasure;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;
import xyz.cychen.ycc.impl.check.ecc.ECCGenerator;
import xyz.cychen.ycc.impl.check.pcctotalgen.ECCGeneratorForPCC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PCCChecker extends Checker {
    public PCCChecker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new PCCEvaluator(new ECCEvaluator()), new PCCGenerator(new ECCGeneratorForPCC()),
                constraints);
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
        return "PCC-CG";
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

            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((PCCEvaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                ((PCCEvaluator) evaluator).setChange(addChange);
                truthValue = evaluator.evaluate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (!alreadIn) {
                link = ((PCCGenerator) generator).totalGenerate(cct, new Binding());
            }
            else {
                ((PCCGenerator) generator).setChange(addChange);
                link = generator.generate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3);
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

            boolean truthValue;
            if (!alreadIn) {
                truthValue = ((PCCEvaluator) evaluator).totalEvaluate(cct, new Binding());
            }
            else {
                ((PCCEvaluator) evaluator).setChange(delChange);
                truthValue = evaluator.evaluate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (!alreadIn) {
                link = ((PCCGenerator) generator).totalGenerate(cct, new Binding());
            }
            else {
                ((PCCGenerator) generator).setChange(delChange);
                link = generator.generate(cct, new Binding());
            }

            // DEBUG: BEGIN
            long time3 = System.nanoTime();
            // DEBUG: END

            result.put(constraintID, Pair.with(truthValue, link));

            // DEBUG: BEGIN
            updateTimeCount(constraintID, time0, time1, time2, time3);
//            updateMeasure(constraintID, cct);
            // DEBUG: END
        }

        return result;
    }
}
