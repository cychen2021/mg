package xyz.cychen.ycc.impl.check.opcc;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Goal;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;
import xyz.cychen.ycc.impl.check.pcc.PCCAdjuster;
import xyz.cychen.ycc.impl.check.pcc.PCCEvaluator;
import xyz.cychen.ycc.impl.check.pcctotalgen.OCCGeneratorForPCC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OPCCChecker extends Checker {
    public OPCCChecker(Map<String, Pair<Boolean, Formula>> constraints) {
        this(constraints, null, false);
    }

    public OPCCChecker(Map<String, Pair<Boolean, Formula>> constraints, Boolean defaultGoal, boolean testMode) {
        super(new ECCBuilder(), new PCCEvaluator(new ECCEvaluator()), new OPCCGenerator(new OCCGeneratorForPCC()),
                constraints);
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
            inner1 = new OPCCChecker(constraints, true, false);
            inner2 = new OPCCChecker(constraints, false, false);
        }
    }

    private boolean testMode = false;

    public void setTestMode() {
        this.testMode = true;
    }

    public void unsetTestMode() {
        this.testMode = false;
    }

    private OPCCChecker inner1 = null;
    private OPCCChecker inner2 = null;
    protected PCCAdjuster adjuster = new PCCAdjuster(builder);

    protected Map<String, CCT> ccts =  new HashMap<>();

    @Override
    public String getName() {
        return "PCC-OG";
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
                link = ((OPCCGenerator) generator).totalGenerate(cct, new Binding());
            }
            else {
                ((OPCCGenerator) generator).setChange(addChange);
                link = generator.generate(cct, new Binding());
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
                link = ((OPCCGenerator) generator).totalGenerate(cct, new Binding());
            }
            else {
                ((OPCCGenerator) generator).setChange(delChange);
                link = generator.generate(cct, new Binding());
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
}
