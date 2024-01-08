package xyz.cychen.ycc.app;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Scheduler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NormalPatternBuilder extends PatternBuilder{
    private String fileName;

    public NormalPatternBuilder(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Pair<Map<String, Long>, Map<String, Pair<ContextSet, Scheduler.Filter>>> build() {
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(new File(fileName));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
        Map<String, Pair<ContextSet, Scheduler.Filter>> patterns =  new HashMap<>();
        Map<String, Long> freshness = new HashMap<>();

        Element ePatterns = document.getRootElement();
        assert ePatterns.getName().equals("patterns");
        for (var ePattern: ePatterns.elements()) {
            assert ePattern.getName().equals("pattern");
            String id = ePattern.selectSingleNode("id").getText();
            ContextSet set = new ContextSet(id);
            long theFreshness = Long.parseLong(ePattern.selectSingleNode("freshness").getText());
            freshness.put(id, theFreshness);
            String predicate = ePattern.selectSingleNode("predicate").getText();
            String site = ePattern.selectSingleNode("site").getText();
            Scheduler.Filter predFilter;
            Scheduler.Filter siteFilter;
            switch (predicate) {
                case "run_with_service":
                    predFilter = CarFilter.runWithService;
                    break;
                case "any":
                    predFilter = CarFilter.any;
                    break;
                default:
                    System.err.println("Unknown predicate "+predicate);
                    System.exit(-1);
                    return null;
            }

            switch (site) {
                case "sutpc_0":
                    siteFilter = CarFilter.sutPC0;
                    break;
                case "sutpc_1":
                    siteFilter = CarFilter.sutPC1;
                    break;
                case "sutpc_2":
                    siteFilter = CarFilter.sutPC2;
                    break;
                case "sutpc_3":
                    siteFilter = CarFilter.sutPC3;
                    break;
                case "sutpc_4":
                    siteFilter = CarFilter.sutPC4;
                    break;
                case "sutpc_5":
                    siteFilter = CarFilter.sutPC5;
                    break;
                case "sutpc_6":
                    siteFilter = CarFilter.sutPC6;
                    break;
                case "sutpc_7":
                    siteFilter = CarFilter.sutPC7;
                    break;
                case "sutpc_8":
                    siteFilter = CarFilter.sutPC8;
                    break;
                case "sutpc_9":
                    siteFilter = CarFilter.sutPC9;
                    break;
                case "sutpc_A":
                    siteFilter = CarFilter.sutPCA;
                    break;
                case "sutpc_B":
                    siteFilter = CarFilter.sutPCB;
                    break;
                case "sutpc_C":
                    siteFilter = CarFilter.sutPCC;
                    break;
                case "sutpc_D":
                    siteFilter = CarFilter.sutPCD;
                    break;
                case "sutpc_E":
                    siteFilter = CarFilter.sutPCE;
                    break;
                case "sutpc_F":
                    siteFilter = CarFilter.sutPCF;
                    break;
                case "sutpc_G":
                    siteFilter = CarFilter.sutPCG;
                    break;
                case "sutpc_H":
                    siteFilter = CarFilter.sutPCH;
                    break;
                case "sutpc_I":
                    siteFilter = CarFilter.sutPCI;
                    break;
                case "sutpc_J":
                    siteFilter = CarFilter.sutPCJ;
                    break;
                case "sutpc_K":
                    siteFilter = CarFilter.sutPCK;
                    break;
                case "sutpc_L":
                    siteFilter = CarFilter.sutPCL;
                    break;
                case "sutpc_M":
                    siteFilter = CarFilter.sutPCM;
                    break;
                case "sutpc_N":
                    siteFilter = CarFilter.sutPCN;
                    break;
                case "sutpc_O":
                    siteFilter = CarFilter.sutPCO;
                    break;
                case "sutpc_P":
                    siteFilter = CarFilter.sutPCP;
                    break;
                case "sutpc_Q":
                    siteFilter = CarFilter.sutPCQ;
                    break;
                case "sutpc_R":
                    siteFilter = CarFilter.sutPCR;
                    break;
                case "sutpc_S":
                    siteFilter = CarFilter.sutPCS;
                    break;
                case "sutpc_T":
                    siteFilter = CarFilter.sutPCT;
                    break;
                case "sutpc_U":
                    siteFilter = CarFilter.sutPCU;
                    break;
                case "sutpc_V":
                    siteFilter = CarFilter.sutPCV;
                    break;
                case "sutpc_W":
                    siteFilter = CarFilter.sutPCW;
                    break;
                case "sutpc_X":
                    siteFilter = CarFilter.sutPCX;
                    break;
                case "sutpc_Y":
                    siteFilter = CarFilter.sutPCY;
                    break;
                case "sutpc_Z":
                    siteFilter = CarFilter.sutPCZ;
                    break;
                case "hotarea_A":
                    siteFilter = CarFilter.hotAreaA;
                    break;
                case "hotarea_B":
                    siteFilter = CarFilter.hotAreaB;
                    break;
                case "hotarea_C":
                    siteFilter = CarFilter.hotAreaC;
                    break;
                case "hotarea_D":
                    siteFilter = CarFilter.hotAreaD;
                    break;
                case "hotarea_E":
                    siteFilter = CarFilter.hotAreaE;
                    break;
                case "hotarea_F":
                    siteFilter = CarFilter.hotAreaF;
                    break;
                case "hotarea_G":
                    siteFilter = CarFilter.hotAreaG;
                    break;
                case "hotarea_H":
                    siteFilter = CarFilter.hotAreaH;
                    break;
                case "hotarea_I":
                    siteFilter = CarFilter.hotAreaI;
                    break;
                case "any":
                    siteFilter = CarFilter.any;
                    break;
                default:
                    System.err.println("Unknown site "+site);
                    System.exit(-1);
                    return null;
            }
            Scheduler.Filter filter = new CarFilter.ConjunctiveFilter(predFilter, siteFilter);
            patterns.put(id, Pair.with(set, filter));
        }
        return Pair.with(freshness, patterns);
    }
}
