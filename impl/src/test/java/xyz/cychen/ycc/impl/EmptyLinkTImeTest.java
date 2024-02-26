package xyz.cychen.ycc.impl;

import org.junit.jupiter.api.*;
import xyz.cychen.ycc.framework.*;
import xyz.cychen.ycc.framework.cct.CCT;
import xyz.cychen.ycc.framework.check.Builder;
import xyz.cychen.ycc.framework.check.Evaluator;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.framework.formula.BFuncFormula;
import xyz.cychen.ycc.framework.formula.Formula;
import xyz.cychen.ycc.framework.formula.UniversalFormula;

import xyz.cychen.ycc.app.CarContext;
import xyz.cychen.ycc.impl.check.ecc.ECCBuilder;
import xyz.cychen.ycc.impl.check.ecc.ECCEvaluator;
import xyz.cychen.ycc.impl.check.ecc.ECCGenerator;


public class EmptyLinkTImeTest {
    static class TestPredicateBot implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            return false;
        }
    }

    static class TestPredicateTop implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            return true;
        }
    }

    static ContextSet set = new ContextSet("S1");
    static Builder builder = new ECCBuilder();
    static Evaluator evaluator = new ECCEvaluator();
    static Generator generator = new ECCGenerator();
    static Context context;

    @BeforeAll
    static void setUp() {
        context = new CarContext("1", "114514", 19.19, 8.10, 520, true);
        set.add(context);
    }

    @Test
    void countTimeEmptyLink() {
        Formula bfunc = new BFuncFormula(new TestPredicateTop(), new Variable("v1"));
        Formula univ = new UniversalFormula(new Variable("v1"), set, bfunc);
        CCT cct = builder.build(univ);
        evaluator.evaluate(cct, new Binding());
        var start = System.nanoTime();
        generator.generate(cct, new Binding());
        var end = System.nanoTime();
        System.out.println("EmptyLink: " + (end - start) + " ns");
    }

    @Test
    void countTimeOneLink() {
        Formula bfunc = new BFuncFormula(new TestPredicateBot(), new Variable("v1"));
        Formula univ = new UniversalFormula(new Variable("v1"), set, bfunc);
        CCT cct = builder.build(univ);
        evaluator.evaluate(cct, new Binding());
        var start = System.nanoTime();
        generator.generate(cct, new Binding());
        var end = System.nanoTime();
        System.out.println("OneLink: " + (end - start) + " ns");
    }

    @Test
    void countTimeBFunc() {
        Variable v1 = new Variable("v1");
        Formula bfunc = new BFuncFormula(new TestPredicateTop(), v1);
        CCT cct = builder.build(bfunc);
        Binding binding = new Binding();
        binding.bind(v1, context);
        evaluator.evaluate(cct, binding);
        var start = System.nanoTime();
        generator.generate(cct, binding);
        var end = System.nanoTime();
        System.out.println("BFunc: " + (end - start) + " ns");
    }
}
