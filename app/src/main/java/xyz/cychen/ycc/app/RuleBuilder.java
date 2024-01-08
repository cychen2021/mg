package xyz.cychen.ycc.app;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.javatuples.Pair;
import org.dom4j.io.SAXReader;
import xyz.cychen.ycc.app.wrapper.WrapperPredicate;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Predicate;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.Variable;
import xyz.cychen.ycc.framework.formula.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleBuilder {
    private String fileName;
    private Map<String, Pair<ContextSet, Scheduler.Filter>> patterns;
    boolean validationMode;

    boolean testeeMode = false;

    public void setTesteeMode() {
        testeeMode = true;
    }

    public void unsetTesteeMode() {
        testeeMode = false;
    }

    public RuleBuilder(String fileName, Map<String, Pair<ContextSet, Scheduler.Filter>> patterns) {
        this.fileName = fileName;
        this.patterns = patterns;
        this.validationMode = false;
    }

    CarPredicate.RandomP4Valid.Cache cache = null;

    public RuleBuilder(String fileName, Map<String, Pair<ContextSet, Scheduler.Filter>> patterns,
                       boolean validationMode, CarPredicate.RandomP4Valid.Cache cache) {
        this.fileName = fileName;
        this.patterns = patterns;
        this.validationMode = validationMode;
        this.cache = cache;
    }


    public Map<String, Pair<Boolean, Formula>> build() {
        Map<String, Pair<Boolean, Formula>> result = new HashMap<>();
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
        Element eRules = document.getRootElement();
        assert eRules.getName().equals("rules");
        List<Node> ruleList = eRules.selectNodes("rule");
        for (var rule: ruleList) {
            assert rule.getName().equals("rule");
            Element eID = (Element) rule.selectSingleNode("id");
            assert  eID.getName().equals("id");
            String id = eID.getText();
            Element eFormula = (Element) rule.selectSingleNode("formula");
            assert eFormula.elements().size() == 1;
            Formula f = buildFormula(eFormula.elements().get(0));
            result.put(id, Pair.with(testeeMode? null : true, f));
        }
        return result;
    }

    private Formula buildFormula(Element element) {
        String tag = element.getName();
        List<Element> eChildren = element.elements();
        List<Formula> children = new ArrayList<>(2);
        Variable var = null;
        ContextSet universe = null;
        Variable[] paras = null;
        Predicate predicate = null;
        switch (tag) {
            case "and":
            case "or":
            case "implies":
                assert eChildren.size() == 2;
                children.add(0, buildFormula(eChildren.get(0)));
                children.add(1, buildFormula(eChildren.get(1)));
                break;
            case "not":
                assert eChildren.size() == 1;
                children.add(0, buildFormula(eChildren.get(0)));
                break;
            case "forall":
            case "exists":
                assert eChildren.size() == 1;
                children.add(0, buildFormula(eChildren.get(0)));
                var = new Variable(element.attributeValue("var"));
                universe = patterns.get(element.attributeValue("in")).getValue0();
                break;
            case "bfunc":
                paras = new Variable[eChildren.size()];
                for (var e: eChildren) {
                    assert e.getName().equals("param");
                    int pos = Integer.parseInt(e.attributeValue("pos"));
                    Variable v = new Variable(e.attributeValue("var"));
                    paras[pos-1] = v;
                }
                String bfunc = element.attributeValue("name");
                if (testeeMode) {
                    predicate = new WrapperPredicate(bfunc, paras);
                } else {
                    switch (bfunc) {
                        case "same":
                            predicate = CarPredicate.same;
                            break;
                        case "sz_loc_dist":
                            predicate = CarPredicate.szLocDist;
                            break;
                        case "sz_loc_range":
                            predicate = CarPredicate.szLocRange;
                            break;
                        case "sz_loc_close":
                            predicate = CarPredicate.szLocClose;
                            break;
                        case "sz_spd_close":
                            predicate = CarPredicate.szSpdClose;
                            break;
                        default:
                            assert bfunc.startsWith("random");
                            String[] tokens = bfunc.split("_");
                            Integer id = null;
                            assert tokens.length == 2 || tokens.length == 1;
                            if (tokens.length == 2) {
                                id = Integer.parseInt(tokens[1]);
                            }
                            if (validationMode) {
                                predicate = new CarPredicate.RandomP4Valid(id, cache);
                            }
                            else {
                                predicate = new CarPredicate.RandomP();
                            }
                            break;
                    }
                }
                break;
            default:
                assert false;
        }
        switch (tag) {
            case "and":
                return new AndFormula(children.get(0), children.get(1));
            case "or":
                return new OrFormula(children.get(0), children.get(1));
            case "implies":
                return new ImpliesFormula(children.get(0), children.get(1));
            case "not":
                return new NotFormula(children.get(0));
            case "forall":
                return new UniversalFormula(var, universe, children.get(0));
            case "exists":
                return new ExistentialFormula(var, universe, children.get(0));
            case "bfunc":
                return new BFuncFormula(predicate, paras);
            default:
                System.err.println("Unkown formula type " + tag);
                System.exit(-1);
                return null;
        }
    }
}
