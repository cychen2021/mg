package xyz.cychen.ycc.framework;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.check.Checker;
import xyz.cychen.ycc.framework.measure.Statistics;

import java.util.*;

public abstract class Scheduler {
    public interface Filter {
        boolean filter(Context context);
    }

    protected final List<Pair<String, Link>> result;

    protected List<Pair<String, Link>> lastResult;

    protected final Checker checker;

    protected final Map<String, Pair<ContextSet, Filter>> patterns;

    public Scheduler(Checker checker, List<Pair<String, Link>> resultStore,
                     Map<String, Pair<ContextSet, Filter>> patterns) {
        this.checker = checker;
        this.result = resultStore;
        this.patterns = patterns;
    }

    public interface Event {
        void callback();
    }

    public class IncEvent implements Event{
        protected final String setID;
        protected final Context context;
        protected final long timestamp;

        public IncEvent(long timestamp, String setID, Context context) {
            this.setID = setID;
            this.context = context;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSetID() {
            return setID;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public void callback() {
            checkAdd(setID, context);
        }
    }

    public class DecEvent implements Event {
        protected final String setID;
        protected final Context context;

        public DecEvent(String setID, Context context) {
            this.setID = setID;
            this.context = context;
        }

        @Override
        public void callback() {
            checkDel(setID, context);
        }
    }

    public void receive(long timestamp, Context context) {
        patterns.forEach((k, v) -> {
            if (v.getValue1().filter(context)) {
                IncEvent incEvent = new IncEvent(timestamp, k, context);
                process(incEvent);
            }
        });
    }

    public String getFirstPattern() {
        return patterns.keySet().iterator().next();
    }

    public void process(Event event) {
        if (event instanceof IncEvent incEvent) {
            process(incEvent);
        }
        else if (event instanceof DecEvent decEvent) {
            process(decEvent);
        }
        else {
            assert false;
        }
    }

    public Checker getChecker() {
        return checker;
    }

    protected abstract void process(IncEvent incEvent);
    protected abstract void process(DecEvent decEvent);

    public Map<String, Statistics> getStatistics() {
        return this.checker.getStatistics();
    }

    private void checkAdd(String setID, Context context) {
        var checkResult =
                checker.check(new Checker.AddChange(setID, context));
        result.addAll(checkResult);
        lastResult = checkResult;
    }

    private void checkDel(String setID, Context context) {
        var checkResult =
                checker.check(new Checker.DelChange(setID, context));
        result.addAll(checkResult);
        lastResult = checkResult;
    }

    public List<Pair<String, Link>> getLastResult() {
        return lastResult;
    }
}
