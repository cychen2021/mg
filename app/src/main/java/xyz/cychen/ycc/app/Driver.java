package xyz.cychen.ycc.app;

import me.tongfei.progressbar.ProgressBar;
import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.measure.Statistics;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.impl.check.conc.ConCChecker;
import xyz.cychen.ycc.impl.check.ecc.ECCChecker;
import xyz.cychen.ycc.impl.check.occ.OCCChecker;
import xyz.cychen.ycc.impl.check.oconc.OConCChecker;
import xyz.cychen.ycc.impl.check.opcc.OPCCChecker;
import xyz.cychen.ycc.impl.check.pcc.PCCChecker;
import xyz.cychen.ycc.impl.check.xconc.XConCChecker;
import xyz.cychen.ycc.impl.check.xpcc.ver1.XPCC1Checker;
import xyz.cychen.ycc.impl.check.xpcc.ver2.XPCC2Checker;
import xyz.cychen.ycc.impl.check.xpcc.ver3.XPCC3Checker;
import xyz.cychen.ycc.impl.check.xyz.XYZChecker;
import xyz.cychen.ycc.impl.schedule.direct.DirectImmediateScheduler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public abstract class Driver {
//    protected Map<String, Scheduler> schedulers;
//    protected Map<String, List<Pair<String, Link>>> result;

    protected final String[] checkerString;
    protected final String ruleFile;
    protected final String patternFile;
    protected final String scheduleMethod;
    protected String inputFile;

    protected int concParaNum;

    //    protected void setupSchedulers() {
//        this.schedulers = new HashMap<>();
//        for (int i=0; i< checkerString.length; i++) {
//            String m = checkerString[i];
//            Checker checker = null;
//            var patternDesc = (new NormalPatternBuilder(patternFile)).build();
//            var rules = (new RuleBuilder(ruleFile, patternDesc.getValue1())).build();
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
//            schedulers.put(m, buildScheduler(scheduleMethod, checker, result.get(m), patternDesc.getValue1()));
//        }
//    }

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
            case "PCC":
                checker = new PCCChecker(rules);
                break;
            case "XPCC1":
                checker = new XPCC1Checker(rules);
                break;
            case "XPCC2":
                checker = new XPCC2Checker(rules);
                break;
            case "XPCC3":
                checker = new XPCC3Checker(rules);
                break;
            case "ConC":
                checker = new ConCChecker(concParaNum, rules);
                break;
            case "XConC":
                checker = new XConCChecker(concParaNum, rules);
                break;
            case "OPCC":
                checker = new OPCCChecker(rules);
                break;
            case "OConC":
                checker = new OConCChecker(concParaNum, rules);
                break;
            default:
                System.err.println("Unknown checker "+checkMethod);
                System.exit(-1);
        }
        Scheduler scheduler = buildScheduler(scheduleMethod, checker, result, patternDesc.getValue1());
        return Pair.with(scheduler, result);
    }

    protected Scheduler buildScheduler(String scheduleMethod, Checker checker, List<Pair<String, Link>> result,
                                     Map<String, Pair<ContextSet, Scheduler.Filter>> patterns) {
        switch (scheduleMethod) {
            case "IMD":
                return new DirectImmediateScheduler(checker, result, patterns);
            default:
                System.err.println("Unknown scheduler "+scheduleMethod);
                System.exit(-1);
                return null;
        }
    }

    public Driver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile, int concParaNum) {
        this.ruleFile = ruleFile;
        this.checkerString = checkMethod;
        this.scheduleMethod = scheduleMethod;
        this.patternFile = patternFile;
        this.concParaNum = concParaNum;
//        this.result = new HashMap<>();
//        for (String m: checkMethod) {
//            this.result.put(m, new LinkedList<>());
//        }
    }

    public Driver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile) {
        this(checkMethod, scheduleMethod, patternFile, ruleFile, -1);
    }

    protected void output(String fileName, List<Pair<String, Link>> result) {
        try (BufferedWriter file =
                     new BufferedWriter(new FileWriter(fileName))) {
            Set<String> records = new HashSet<>();
            System.out.println("\nStart to output results...");
            for (var r: ProgressBar.wrap(result, "Outputting")) {
                List<String> links = r.getValue1().toStrings();
                for (var link: links) {
                    String lk = r.getValue0()+link+"\n";
                    if (!records.contains(lk)) {
                        file.write(lk);
                        records.add(lk);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    protected void outputStats(String fileName, Map<String, Statistics> statistics) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            StringBuilder headerBuild = new StringBuilder();
            headerBuild.append("ruleID").append(",");
            for (var h: Statistics.header) {
                headerBuild.append(h).append(",");
            }
            String header = headerBuild.toString();
            writer.write(header+"\n");
            System.out.println("\nStart to process statistics...");
            Statistics sum = new Statistics();
            for (var p: statistics.entrySet()) {
                writer.write(p.getKey() + "," + p.getValue() + "\n");
                sum.update(p.getValue());
            }
            writer.write("sum" + ","+ sum + "\n");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public void load(String path) {
        this.inputFile = path;
    }
    public abstract void exec(String resultDir, String statDir);
}
