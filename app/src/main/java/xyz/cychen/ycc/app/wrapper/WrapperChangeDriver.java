package xyz.cychen.ycc.app.wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.tongfei.progressbar.ProgressBar;
import org.javatuples.Pair;
import xyz.cychen.ycc.app.ChangeDriver;
import xyz.cychen.ycc.app.RuleBuilder;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.framework.measure.Statistics;
import xyz.cychen.ycc.impl.check.Closable;
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

import java.io.FileWriter;
import java.util.*;

public class WrapperChangeDriver extends ChangeDriver {
    public WrapperChangeDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile, int concParanum, String cctFile) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile, concParanum);
        assert checkMethod.length == 1;
        this.cctFile = cctFile;
    }

    public WrapperChangeDriver(String[] checkMethod, String scheduleMethod, String patternFile, String ruleFile) {
        super(checkMethod, scheduleMethod, patternFile, ruleFile);
        assert checkMethod.length == 1;
    }

    @Override
    protected List<Scheduler.Event> loadData(String path, Scheduler scheduler) {
        return (new WrapperContextChangeBuilder(path, scheduler)).build();
    }

    String cctFile = null;

    Map<String, Pair<Boolean, Formula>> rules;

    @Override
    protected Pair<Scheduler, List<Pair<String, Link>>> setupScheduler(String scheduleMethod, String checkMethod,
                                                                       String patternFile) {
        List<Pair<String, Link>> result = new LinkedList<>();
        Checker checker = null;
        var patternDesc = (new WrapperPatternBuilder(patternFile)).build();
        var ruleBuilder = new RuleBuilder(ruleFile, patternDesc.getValue1());
        ruleBuilder.setTesteeMode();
        var rules = ruleBuilder.build();
        this.rules = rules;
        switch (checkMethod) {
            case "ECC":
                checker = new ECCChecker(rules);
                break;
            case "OCC":
                checker = new OCCChecker(rules, null, true);
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
                checker = new OPCCChecker(rules, null, true);
                break;
            case "OConC":
                checker = new OConCChecker(concParaNum, rules, null, true);
                break;
            default:
                System.err.println("Unknown checker "+checkMethod);
                System.exit(-1);
        }
        Scheduler scheduler = buildScheduler(scheduleMethod, checker, result, patternDesc.getValue1());
        return Pair.with(scheduler, result);
    }

    class LinkEntry {
        private String var;
        private ContextEntry value;

        public LinkEntry(String var, ContextEntry value) {
            this.var = var;
            this.value = value;
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public ContextEntry getValue() {
            return value;
        }

        public void setValue(ContextEntry value) {
            this.value = value;
        }
    }

    class ResultEntry {
        private boolean truth;

        private List<List<LinkEntry>> links;

        public boolean isTruth() {
            return truth;
        }

        public void setTruth(boolean truth) {
            this.truth = truth;
        }

        public List<List<LinkEntry>> getLinks() {
            return links;
        }

        public void setLinks(List<List<LinkEntry>> links) {
            this.links = links;
        }

        public ResultEntry(boolean truth, List<List<LinkEntry>> links) {
            this.truth = truth;
            this.links = links;
        }
    }


    @Override
    protected void outputStats(String fileName, Map<String, Statistics> statistics) {

    }

    @Override
    public void exec(String resultDir, String statDir) {
        for (var checkMethod: checkerString) {
            var p = setupScheduler(scheduleMethod, checkMethod, patternFile);
            Scheduler scheduler = p.getValue0();
            var changes = loadData(inputFile, scheduler);
            if (changes.isEmpty()) {
                var setName = scheduler.getFirstPattern();
                var tmp1 = new HashMap<String, String>();
                tmp1.put("data", "1");
                var tmpc = new WrapperContext("1", tmp1);
                changes.add(scheduler.new IncEvent(0, setName, tmpc));
                var tmp2 = new HashMap<String, String>();
                tmp2.put("data", "2");
                changes.add(scheduler.new DecEvent(setName, tmpc));
            }
            System.out.println("\n" + scheduler.getChecker().getName() + " begin!");
            for (var c: ProgressBar.wrap(changes, "Checking")) {
                scheduler.process(c);
            }
            var result = p.getValue0().getLastResult();
            output(resultDir, result);
            if (this.cctFile != null) {
                outputCCT(this.cctFile, scheduler);
            }
            if (checkMethod.equals("ConC") || checkMethod.equals("OConC") || checkMethod.equals("XConC")) {
                ((Closable) scheduler.getChecker()).shutdown();
            }
        }
    }

    public void outputCCT(String fileName, Scheduler scheduler) {
        var checker = scheduler.getChecker();
        String cctRepr;
        var cct = checker.getCCT();
        if (!cct.getTV() && (Objects.equals(checker.getName(), "ECC-MG") || Objects.equals(checker.getName(), "ConC-MG") || Objects.equals(checker.getName(), "PCC-MG-1"))) {
            cctRepr = cct.toStringRepr(true);
        } else {
            cctRepr = cct.toStringRepr(false);
        }
        try (var fw = new FileWriter(fileName)) {
            fw.write(cctRepr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void output(String fileName, List<Pair<String, Link>> result) {
        try {
            Set<String> record = new HashSet<>();
            Map<String, ResultEntry> resultList = new HashMap<>();
            for (Pair<String, Link> pair : result) {
                String pat = pair.getValue0();
                Link link = pair.getValue1();
                boolean truth = link.getTruth();

                List<List<LinkEntry>> linkList = new LinkedList<>();

                for (var binding: link.getBindings()) {
                    List<LinkEntry> entryList = binding.getMap().entrySet().stream().map(
                            entry -> new LinkEntry(entry.getKey().toString(),
                                    new ContextEntry(entry.getValue().getId(), entry.getValue().toMap()))
                    ).toList();
                    linkList.add(entryList);
                }
                resultList.put(pat, new ResultEntry(truth, linkList));
                record.add(pat);
            }
            for (var e: this.rules.entrySet()) {
                if (!record.contains(e.getKey())) {
                    resultList.put(e.getKey(), new ResultEntry(e.getValue().getValue0(), new LinkedList<>()));
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(fileName), resultList);
        } catch (Exception e) {
            System.err.println("Error in outputing result");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
