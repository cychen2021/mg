package xyz.cychen.ycc.impl.schedule.direct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.List;
import java.util.Map;

public abstract class DirectScheduler extends Scheduler {
    public DirectScheduler(Checker checker, List<Pair<String, Link>> resultStore, Map<String, Pair<ContextSet, Filter>> patterns) {
        super(checker, resultStore, patterns);
    }

    @Override
    public final void receive(long timestamp, Context context) {
        System.err.println("Cannot use the direct scheduler's receive interface");
        System.exit(-1);
    }
}
