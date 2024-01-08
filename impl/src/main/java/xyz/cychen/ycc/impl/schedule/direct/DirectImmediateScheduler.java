package xyz.cychen.ycc.impl.schedule.direct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.List;
import java.util.Map;

public class DirectImmediateScheduler extends DirectScheduler {
    public DirectImmediateScheduler(Checker checker, List<Pair<String, Link>> resultStore,
                                    Map<String, Pair<ContextSet, Filter>> patterns) {
        super(checker, resultStore, patterns);
    }

    @Override
    protected void process(IncEvent incEvent) {
        incEvent.callback();
    }

    @Override
    protected void process(DecEvent decEvent) {
        decEvent.callback();
    }
}
