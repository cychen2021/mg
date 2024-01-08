package xyz.cychen.ycc.impl.schedule.timing;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.List;
import java.util.Map;

public class TimingImmediateScheduler extends TimingScheduler {
    public TimingImmediateScheduler(Checker checker, List<Pair<String, Link>> resultStore,
                                    Map<String, Pair<ContextSet, Filter>> patterns,
                                    Map<String, Long> freshness) {
        super(checker, resultStore, patterns, freshness);
    }

    @Override
    protected void processIncEvent(IncEvent incEvent) {
        incEvent.callback();

        String setID = incEvent.getSetID();
        DecEvent delEvent = new DecEvent(setID, incEvent.getContext());
        long timestamp = incEvent.getTimestamp();
        addHook(timestamp+freshness.get(setID), delEvent);
    }

    @Override
    protected void process(DecEvent decEvent) {
        decEvent.callback();
    }
}
