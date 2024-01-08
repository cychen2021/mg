package xyz.cychen.ycc.impl.schedule.timing;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.Scheduler;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class TimingScheduler extends Scheduler {
    protected Map<String, Long> freshness;
    private long time = -1;
    private final Map<Long, List<Event>> hooks = new HashMap<>();

    protected TimingScheduler(Checker checker, List<Pair<String, Link>> resultStore,
                              Map<String, Pair<ContextSet, Filter>> patterns,
                              Map<String, Long> freshness) {
        super(checker, resultStore, patterns);
        this.freshness = freshness;
    }

    public void addHook(long time, Event event) {
        List<Event> theList;
        if (!hooks.containsKey(time)) {
            theList = new LinkedList<>();
            hooks.put(time, theList);
        }
        else {
            theList = hooks.get(time);
        }
        theList.add(event);
    }

    public void setTime(long time) {
        if (time < this.time) {
            System.err.println("Time turns back!");
            System.exit(-1);
        }

        List<Event> eventList = hooks.get(time);
        if (eventList != null) {
            assert !eventList.isEmpty();
            for (Event e: eventList) {
                process(e);
            }
        }
        hooks.remove(time);
        this.time = time;
    }

    @Override
    protected final void process(IncEvent incEvent) {
        setTime(incEvent.getTimestamp());
        processIncEvent(incEvent);
    }

    protected abstract void processIncEvent(IncEvent incEvent);
}
