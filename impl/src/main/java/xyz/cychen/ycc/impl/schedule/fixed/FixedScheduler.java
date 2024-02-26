package xyz.cychen.ycc.impl.schedule.fixed;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class FixedScheduler extends Scheduler {
    protected int fixedVolume;

    protected String fixedSetName;
    protected Queue<Context> record;

    public FixedScheduler(Checker checker, List<Pair<String, Link>> resultStore,
                          Map<String, Pair<ContextSet, Filter>> patterns, int fixedVolume, String fixedSetName) {
        super(checker, resultStore, patterns);
        this.fixedSetName = fixedSetName;
        assert patterns.keySet().size() == 1 && patterns.containsKey(fixedSetName);
        this.fixedVolume = fixedVolume;
        this.record = new LinkedList<Context>();
    }

    @Override
    public final void receive(long timestamp, Context context) {
        assert patterns.get(fixedSetName).getValue0().size() <= fixedVolume;
        if (patterns.get(fixedSetName).getValue0().size() < fixedVolume) {
            IncEvent e = new IncEvent(timestamp, fixedSetName, context);
            process(e);
            record.add(context);
        }
        else if (patterns.get(fixedSetName).getValue0().size() == fixedVolume) {
            Context toBePolled = record.poll();
            DecEvent decEvent = new DecEvent(fixedSetName, toBePolled);
            process(decEvent);
            IncEvent incEvent = new IncEvent(timestamp, fixedSetName, context);
            process(incEvent);
            record.add(context);
        }
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
