package xyz.cychen.ycc.impl.check.xconc;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class XConCChecker extends Checker implements Closable {
//    protected void initExecutor(int paraNum, Map<String, Pair<Boolean, Formula>> constraints) {
//        constraints.forEach((k, v) -> initExecutor(paraNum, v.getValue1()));
//    }

//    protected void initExecutor(int paraNum, Formula formula) {
//        if (formula instanceof BinaryFormula) {
//            initExecutor(paraNum, formula.getChildren()[0]);
//            initExecutor(paraNum, formula.getChildren()[1]);
//        }
//        else if (formula instanceof NotFormula) {
//            initExecutor(paraNum, formula.getChildren()[0]);
//        }
//        else if (formula instanceof QuantifiedFormula qFormula) {
//            qFormula.setExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(paraNum));
//            initExecutor(paraNum, formula.getChildren()[0]);
//        }
//    }

//    protected Map<Integer, Map<String, Formula>> threadToFormula;

    protected ThreadPoolExecutor executor;

    public XConCChecker(int paraNum, Map<String, Pair<Boolean, Formula>> constraints) {
        super(new ConCBuilder(), new ConCEvaluator(), new XConCGenerator(), constraints);
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(paraNum);
        ((ConCEvaluator) evaluator).setExecutor(executor);
        ((XConCGenerator) generator).setExecutor(executor);
        ((ConCBuilder) builder).setExecutor(executor);
//        this.evaluator = new ConCEvaluator(executor);
//        this.generator = new ConCGenerator(executor);
        constraints.forEach((k, v) -> {
            if (v.getValue0()) {
                deducer.deduce(v.getValue1(), Goal.VIO);
            }
            else {
                deducer.deduce(v.getValue1(), Goal.SAT);
            }
        });
//        initExecutor(paraNum, constraints);
    }

    @Override
    public String getName() {
        return "ConC-MG";
    }

    public void shutdown() {
        executor.shutdown();
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
            if (truthValue != constraints.get(constraintID).getValue0()) {
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
            if (truthValue != constraints.get(constraintID).getValue0()) {
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
//            updateMeasure(constraintID, cct);
            // DEBUG: END
        }

        return result;
    }
}
