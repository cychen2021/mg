package xyz.cychen.ycc.impl.check.xyz;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;
import xyz.cychen.ycc.impl.check.ecc.ECCGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XYZChecker extends Checker {
    public XYZChecker(Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ECCBuilder(), new ECCEvaluator(), new XYZGenerator(), constraints);
        constraints.forEach((k, v) -> {
            if (v.getValue0() != null && v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else if (v.getValue0() != null) {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
        });
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

            Link link;
            if (constraints.get(constraintID).getValue0() == null || truthValue != constraints.get(constraintID).getValue0()) {
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
            updateTimeCount(constraintID, time0, time1, time2, time3);
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }

    @Override
    protected Map<String, Pair<Boolean, Link>> checkAddChange(AddChange addChange) {
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

            Link link;
            if (constraints.get(constraintID).getValue0() == null || truthValue != constraints.get(constraintID).getValue0()) {
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
            updateTimeCount(constraintID, time0, time1, time2, time3);
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

            Link link;
            if (constraints.get(constraintID).getValue0() == null || truthValue != constraints.get(constraintID).getValue0()) {
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
            updateTimeCount(constraintID, time0, time1, time2, time3);
            // DEBUG: END
            this.cct = cct;
        }

        return result;
    }

    @Override
    public String getName() {
        return "ECC-MG";
    }
}
