package xyz.cychen.ycc.impl.schedule.direct;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.check.Checker;

import java.util.*;

public class FixedScheduler extends DirectScheduler {
    protected int fixedVolume;

    protected String fixedSetName;
//    protected Set<Context> record;

    public interface ContextFactory {
        Context produceContext(String id);
    }

    protected ContextFactory contextFactory;
    protected boolean suppressOutput;

    public FixedScheduler(Checker checker, List<Pair<String, Link>> resultStore,
                          Map<String, Pair<ContextSet, Filter>> patterns, int fixedVolume, String fixedSetName,
                          ContextFactory factory, boolean suppressOutput) {
        super(checker, resultStore, patterns);
        this.fixedSetName = fixedSetName;
        assert patterns.keySet().size() == 1 && patterns.containsKey(fixedSetName);
        this.fixedVolume = fixedVolume;
//        this.record = new HashSet<>(fixedVolume);
        this.suppressOutput = suppressOutput;

        this.contextFactory = factory;
    }

    protected int round = 0;

    @Override
    public void process(Event event) {
        if (event != null) {
            System.err.println("Event is not null");
            System.exit(-1);
        }


        var sets = checker.getSets();
        ContextSet set = sets.get(fixedSetName);
        if (set.size() < fixedVolume) {
            assert set.size() == 0;
            for (int i = 0; i < fixedVolume; i++) {
                Context c = contextFactory.produceContext(round+"_"+i);
                set.add(c);
            }
        }

        var checkResult = checker.check(fixedSetName);
        round++;
        if (!suppressOutput) {
            result.addAll(checkResult);
        }
    }

    @Override
    protected void process(IncEvent incEvent) {
        System.err.println("FixedScheduler: process(IncEvent)");
        System.exit(-1);
    }

    @Override
    protected void process(DecEvent decEvent) {
        System.err.println("FixedScheduler: process(DecEvent)");
        System.exit(-1);
    }

    //
//    @Override
//    public final void receive(long timestamp, Context context) {
//        assert patterns.get(fixedSetName).getValue0().size() <= fixedVolume;
//        if (patterns.get(fixedSetName).getValue0().size() < fixedVolume) {
//            IncEvent e = new IncEvent(timestamp, fixedSetName, context);
//            process(e);
//            record.add(context);
//        }
//        else if (patterns.get(fixedSetName).getValue0().size() == fixedVolume) {
//            Context toBePolled = record.poll();
//            DecEvent decEvent = new DecEvent(fixedSetName, toBePolled);
//            process(decEvent);
//            IncEvent incEvent = new IncEvent(timestamp, fixedSetName, context);
//            process(incEvent);
//            record.add(context);
//        }
//    }
//
//    @Override
//    protected void process(IncEvent incEvent) {
//        incEvent.callback();
//    }
//
//    @Override
//    protected void process(DecEvent decEvent) {
//        decEvent.callback();
//    }
}
