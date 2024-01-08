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
import xyz.cychen.ycc.impl.schedule.timing.TimingImmediateScheduler;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NormalDriver extends Driver{
    protected List<Pair<Long, CarContext>> contexts;

    public NormalDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile);
    }

    public NormalDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile,
                        int concParaNum) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile, concParaNum);
    }

    @Override
    public void load(String path) {
        this.contexts = (new CarContextBuilder(path)).build();
    }


    @Override
    protected Pair<Scheduler, List<Pair<String, Link>>> setupScheduler(String scheduleMethod, String checkMethod,
                                                                       String patternFile) {
        List<Pair<String, Link>> result = new LinkedList<>();
        Checker checker = null;
        var patternDesc = (new NormalPatternBuilder(patternFile)).build();
        var rules = (new RuleBuilder(ruleFile, patternDesc.getValue1())).build();
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
        Scheduler scheduler =
                buildScheduler(scheduleMethod, checker, result, patternDesc.getValue1(), patternDesc.getValue0());
        return Pair.with(scheduler, result);
    }

    protected Scheduler buildScheduler(String scheduleMethod, Checker checker, List<Pair<String, Link>> result,
                                       Map<String, Pair<ContextSet, Scheduler.Filter>> patterns,
                                       Map<String, Long> freshness) {
        switch (scheduleMethod) {
            case "IMD":
                return new TimingImmediateScheduler(checker, result, patterns, freshness);
            default:
                System.err.println("Unknown scheduler "+scheduleMethod);
                System.exit(-1);
                return null;
        }
    }

    @Override
    public void exec(String resultDir, String statDir) {
        for (var checkMethod: checkerString) {
            var p = setupScheduler(scheduleMethod, checkMethod, patternFile);
            Scheduler scheduler = p.getValue0();
            var result = p.getValue1();
            System.out.println("\n" + checkMethod + " begin!");
            for (Pair<Long, CarContext> c: ProgressBar.wrap(contexts, "Checking")) {
                scheduler.receive(c.getValue0(), c.getValue1());
            }
            output(Path.of(resultDir, checkMethod +".txt").toString(), result);
            outputStats(Path.of(statDir, checkMethod +".csv").toString(), scheduler.getStatistics());
        }
    }
}
