package xyz.cychen.ycc.app;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Scheduler;

import java.util.Map;

public abstract class PatternBuilder {
    public abstract Pair<Map<String, Long>, Map<String, Pair<ContextSet, Scheduler.Filter>>> build();
}
