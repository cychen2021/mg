package xyz.cychen.ycc.app;

import xyz.cychen.ycc.framework.Scheduler;

import java.nio.file.Path;
import java.util.List;

import me.tongfei.progressbar.*;
import xyz.cychen.ycc.impl.check.Closable;
import xyz.cychen.ycc.impl.check.conc.ConCChecker;

public class ChangeDriver extends Driver {
//    protected Map<String, List<Scheduler.Event>> changes;

    public ChangeDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile,
                        int concParanum) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile, concParanum);
    }

    public ChangeDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile);
    }

    protected List<Scheduler.Event> loadData(String path, Scheduler scheduler) {
        return (new ContextChangeBuilder(path, scheduler)).build();
//        schedulers.forEach((k, v) -> changes.put(k, (new ContextChangeBuilder(path, v)).build()));

    }

    @Override
    public void exec(String resultDir, String statDir) {
        for (var checkMethod: checkerString) {
            var p = setupScheduler(scheduleMethod, checkMethod, patternFile);
            Scheduler scheduler = p.getValue0();
            var result = p.getValue1();
            var changes = loadData(inputFile, scheduler);
            System.out.println("\n" + scheduler.getChecker().getName() + " begin!");
//            int count = 0;
            for (var c: ProgressBar.wrap(changes, "Checking")) {
//                System.out.println(count);
                scheduler.process(c);
//                count++;
//                if (count == 3000) {
//                    break;
//                }
            }
            output(Path.of(resultDir, scheduler.getChecker().getName()+".txt").toString(), result);
            outputStats(Path.of(statDir, scheduler.getChecker().getName()+".csv").toString(), scheduler.getStatistics());
            if (scheduler.getChecker().getName().startsWith("ConC")) {
                ((Closable) scheduler.getChecker()).shutdown();
            }
        }
    }
}
