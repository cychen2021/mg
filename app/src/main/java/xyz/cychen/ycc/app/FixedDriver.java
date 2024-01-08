package xyz.cychen.ycc.app;

import me.tongfei.progressbar.ProgressBar;
import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.impl.check.Closable;
import xyz.cychen.ycc.impl.check.ecc.ECCChecker;
import xyz.cychen.ycc.impl.check.occ.OCCChecker;
import xyz.cychen.ycc.impl.check.xyz.XYZChecker;
import xyz.cychen.ycc.impl.schedule.direct.DirectImmediateScheduler;
import xyz.cychen.ycc.impl.schedule.direct.FixedScheduler;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class FixedDriver extends ChangeDriver {
    protected List<Pair<Long, CarContext>> contexts;
    protected int fixedVolume;
    protected String fixedSetName;
    protected boolean validationEnabled;
    protected int fixedRepeatTimes;
    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName, int fixedVolume,
                       int fixedRepeatTimes) {
        super(checkMethod, "FIXED", "", ruleFile);
        this.fixedVolume = fixedVolume;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = false;
        this.fixedRepeatTimes = fixedRepeatTimes;
    }

    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName, int fixedVolume,
                       boolean validationEnabled, int fixedRepeatTimes) {
        super(checkMethod, "FIXED", "", ruleFile);
        this.fixedVolume = fixedVolume;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = validationEnabled;
        this.fixedRepeatTimes = fixedRepeatTimes;
    }

    protected boolean suppressOutput;

    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName, int fixedVolume,
                       boolean validationEnabled, int concParaNum, boolean suppressOutput, int fixedRepeatTimes) {
        super(checkMethod, "FIXED", "", ruleFile, concParaNum);
        this.fixedVolume = fixedVolume;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = validationEnabled;
        this.fixedRepeatTimes = fixedRepeatTimes;
        this.suppressOutput = suppressOutput;
    }

//    @Override
//    protected void setupSchedulers() {
//        this.schedulers = new HashMap<>();
//        for (int i=0; i< checkerString.length; i++) {
//            String m = checkerString[i];
//            Checker checker = null;
//            var patternDesc =
//                    (new FixedPatternBuilder(fixedSetName, fixedVolume)).build();
//            var rules =
//                    (new RuleBuilder(ruleFile, patternDesc.getValue1(), validationEnabled)).build();
//            switch (m) {
//                case "ECC":
//                    checker = new ECCChecker(rules);
//                    break;
//                case "OCC":
//                    checker = new OCCChecker(rules);
//                    break;
//                case "XYZ":
//                    checker = new XYZChecker(rules);
//                    break;
//                default:
//                    System.err.println("Unknown checker "+m);
//                    System.exit(-1);
//            }
//            schedulers.put(m, buildScheduler("FIXED", checker, result.get(m), patternDesc.getValue1()));
//        }
//    }

    protected CarPredicate.RandomP4Valid.Cache cache = null;

    protected Pair<Scheduler, List<Pair<String, Link>>> setupScheduler(String scheduleMethod, String checkMethod,
                                                                       String patternFile) {
        List<Pair<String, Link>> result = new LinkedList<>();
        Checker checker = null;
        var patternDesc =
                (new FixedPatternBuilder(fixedSetName, fixedVolume)).build();
        var rules =
                (new RuleBuilder(ruleFile, patternDesc.getValue1(), validationEnabled, cache)).build();
        switch (checkMethod) {
            case "ECC":
                checker = new ECCChecker(rules);
                break;
            case "OCC":
                checker = new OCCChecker(rules);
                break;
            case "XYZ":
                checker = new XYZChecker(rules);
                break;
            default:
                System.err.println("Unknown checker "+checkMethod);
                System.exit(-1);
        }
        Scheduler scheduler = buildScheduler(scheduleMethod, checker, result, patternDesc.getValue1());
        return Pair.with(scheduler, result);
    }

    protected static class CarContextFactory implements FixedScheduler.ContextFactory {
        @Override
        public Context produceContext(String id) {
            return new CarContext(id, id, 0, 0, 0, false);
        }
    }

    @Override
    protected Scheduler buildScheduler(String scheduleMethod, Checker checker, List<Pair<String, Link>> result,
                                       Map<String, Pair<ContextSet, Scheduler.Filter>> patterns) {
        assert scheduleMethod.equals("FIXED");
        return new FixedScheduler(checker, result, patterns, fixedVolume, fixedSetName, new CarContextFactory(),
                                  suppressOutput);
    }

    @Override
    public void exec(String resultDir, String statDir) {
        if (!validationEnabled) {
            for (var checkMethod: checkerString) {
                var p = setupScheduler(scheduleMethod, checkMethod, patternFile);
                Scheduler scheduler = p.getValue0();
                var result = p.getValue1();
                System.out.println("\n" + scheduler.getChecker().getName() + " begin!");
                ProgressBar.wrap(IntStream.range(0, fixedRepeatTimes), "Checking").forEach( _i -> {
                    scheduler.process(null);
                });
                output(Path.of(resultDir, scheduler.getChecker().getName()+".txt").toString(), result);
                outputStats(Path.of(statDir, scheduler.getChecker().getName()+".csv").toString(), scheduler.getStatistics());
                if (checkMethod.equals("ConC") || checkMethod.equals("OConC") || checkMethod.equals("XConC")) {
                    ((Closable) scheduler.getChecker()).shutdown();
                }
            }
        }
        else {
            boolean firstRun = true;
            cache = new CarPredicate.RandomP4Valid.Cache();
            for (var checkMethod: checkerString) {
                var p = setupScheduler(scheduleMethod, checkMethod, patternFile);
                var result = p.getValue1();
                if (firstRun) {
                    cache.setCacheIn();
                    firstRun = false;
                }
                else {
                    cache.setCacheOut();
                }
                Scheduler scheduler = p.getValue0();
                System.out.println("\n" + checkMethod +" begin!");
                cache.resetRoundCount();
                ProgressBar.wrap(IntStream.range(0, fixedRepeatTimes), "Checking").forEach( _i -> {
                    scheduler.process(null);
                    cache.incRoundCount();
                });
                output(Path.of(resultDir, checkMethod +".txt").toString(), result);
                outputStats(Path.of(statDir, checkMethod +".csv").toString(), scheduler.getStatistics());
            }
            cache.clearCache();
        }
    }

    @Override
    public void load(String path) {
        throw new UnsupportedOperationException();
    }
}
