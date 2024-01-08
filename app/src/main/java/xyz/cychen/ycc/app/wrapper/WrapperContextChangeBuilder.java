package xyz.cychen.ycc.app.wrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.cychen.ycc.app.ContextChangeBuilder;
import xyz.cychen.ycc.framework.Scheduler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WrapperContextChangeBuilder extends ContextChangeBuilder {
    public WrapperContextChangeBuilder(String contextFileName, Scheduler scheduler) {
        super(contextFileName, scheduler);
    }

    @Override
    public List<Scheduler.Event> build() {
        List<Scheduler.Event> result = new LinkedList<>();
        try {
            List<PatternEntry> patternEntries =
                    new ObjectMapper().readValue(new File(this.contextFileName), new TypeReference<List<PatternEntry>>() {});
            for (var e: patternEntries) {
                for (var c: e.getContexts()) {
                    result.add(scheduler.new
                            IncEvent(0, e.getPatId(), new WrapperContext(c.getCtxId(), c.getFields())));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return result;
    }
}
