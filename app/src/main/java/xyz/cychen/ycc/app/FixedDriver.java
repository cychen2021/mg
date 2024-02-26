package xyz.cychen.ycc.app;

import me.tongfei.progressbar.ProgressBar;
import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.impl.check.ecc.ECCChecker;
import xyz.cychen.ycc.impl.check.occ.OCCChecker;
import xyz.cychen.ycc.impl.check.xyz.XYZChecker;
import xyz.cychen.ycc.impl.schedule.direct.DirectImmediateScheduler;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FixedDriver extends ChangeDriver {
    protected List<Pair<Long, CarContext>> contexts;
    protected int fixedVolume;
    protected String fixedSetName;
    protected boolean validationEnabled;
    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName) {
        super(checkMethod, "FIXED", "", ruleFile);
        this.fixedVolume = -1;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = false;
    }

    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName,
                       boolean validationEnabled) {
        super(checkMethod, "FIXED", "", ruleFile);
        this.fixedVolume = -1;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = validationEnabled;
    }

    public FixedDriver(String[] checkMethod, String ruleFile, String fixedSetName,
                       boolean validationEnabled, int concParaNum) {
        super(checkMethod, "FIXED", "", ruleFile, concParaNum);
        this.fixedVolume = -1;
        this.fixedSetName = fixedSetName;
        this.validationEnabled = validationEnabled;
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

    @Override
    protected Scheduler buildScheduler(String scheduleMethod, Checker checker, List<Pair<String, Link>> result,
                                       Map<String, Pair<ContextSet, Scheduler.Filter>> patterns) {
        assert scheduleMethod.equals("FIXED");
        return new DirectImmediateScheduler(checker, result, patterns);
    }

    @Override
    public void exec(String resultDir, String statDir) {
        if (!validationEnabled) {
            super.exec(resultDir, statDir);
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
                var changes = loadData(inputFile, scheduler);
                System.out.println("\n" + checkMethod +" begin!");
                cache.resetRoundCount();
                for (var r: ProgressBar.wrap(changes, "Checking")) {
                    scheduler.process(r);
                    cache.incRoundCount();
                }
                output(Path.of(resultDir, checkMethod +".txt").toString(), result);
                outputStats(Path.of(statDir, checkMethod +".csv").toString(), scheduler.getStatistics());
            }
            cache.clearCache();
        }
    }
}
