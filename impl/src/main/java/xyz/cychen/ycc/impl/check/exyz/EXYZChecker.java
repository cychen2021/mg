package xyz.cychen.ycc.impl.check.exyz;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.*;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EXYZChecker extends Checker {
    public EXYZChecker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new EXYZEvaluator(), new EXYZGenerator(), constraints);
        constraints.forEach((k, v) -> {
            if (v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
            v.getValue1().analyzeEConditions();
        });
    }

    Map<String, CCT> olds = new HashMap<>();

    @Override
    protected Map<String, Pair<Boolean, Link>> checkAddChange(AddChange addChange) {
        Map<String, Pair<Boolean, Link>> result = new HashMap<>();

        String targetSet = addChange.getTargetSet();
        sets.get(targetSet).add(addChange.getContext());

        Set<String> related = setToConstraints.get(targetSet);
        ((EXYZEvaluator) evaluator).setChange(addChange);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END

            CCT cct = builder.build(f);

            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            CCT old = olds.get(constraintID);
            ((EXYZEvaluator) evaluator).setOldCCT(old);


            boolean truthValue = ((EXYZEvaluator) evaluator).realEvaluate(cct, new Binding());

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            olds.put(constraintID, cct);
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
        ((EXYZEvaluator) evaluator).setChange(delChange);
        for (String constraintID: related) {
            Formula f = constraints.get(constraintID).getValue1();

            // DEBUG: BEGIN
            long time0 = System.nanoTime();
            // DEBUG: END

            CCT cct = builder.build(f);
            // DEBUG: BEGIN
            long time1 = System.nanoTime();
            // DEBUG: END

            CCT old = olds.get(constraintID);
            ((EXYZEvaluator) evaluator).setOldCCT(old);

            boolean truthValue = ((EXYZEvaluator) evaluator).realEvaluate(cct, new Binding());

            // DEBUG: BEGIN
            long time2 = System.nanoTime();
            // DEBUG: END

            Link link;
            if (truthValue != constraints.get(constraintID).getValue0()) {
                link = generator.generate(cct, new Binding());
            }
            else {
                link = Link.of(truthValue ? Link.Type.SAT : Link.Type.VIO);
            }

            olds.put(constraintID, cct);
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
    public String getName() {
        return "ECC-MG+";
    }
}
