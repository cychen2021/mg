package xyz.cychen.ycc.app;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Scheduler;

import java.util.HashMap;
import java.util.Map;

public class FixedPatternBuilder extends PatternBuilder{
    int fixedVolume;
    String patternName;

    public FixedPatternBuilder(String patternName, int fixedVolume) {
        this.fixedVolume = fixedVolume;
        this.patternName = patternName;
    }

    @Override
    public Pair<Map<String, Long>, Map<String, Pair<ContextSet, Scheduler.Filter>>> build() {
        Map<String, Pair<ContextSet, Scheduler.Filter>> result = new HashMap<>();
        result.put(patternName, Pair.with(new ContextSet(patternName), CarFilter.any));
        return Pair.with(null, result);
    }
}
