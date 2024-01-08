package xyz.cychen.ycc.app.wrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javatuples.Pair;
import xyz.cychen.ycc.app.PatternBuilder;
import xyz.cychen.ycc.framework.ContextSet;
import xyz.cychen.ycc.framework.Scheduler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrapperPatternBuilder extends PatternBuilder {
    protected String fileName;

    public WrapperPatternBuilder(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Pair<Map<String, Long>, Map<String, Pair<ContextSet, Scheduler.Filter>>> build() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Long> freshnesses = new HashMap<>();
        Map<String, Pair<ContextSet, Scheduler.Filter>> patternPool = new HashMap<>();
        try {
            List<PatternEntry> patternEntries = mapper.readValue(new File(this.fileName), new TypeReference<List<PatternEntry>>() {});
            for (var e: patternEntries) {
                freshnesses.put(e.getPatId(), Long.MAX_VALUE);
                patternPool.put(e.getPatId(), new Pair<>(new ContextSet(e.getPatId()), (ctx) -> true));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return new Pair<>(freshnesses, patternPool);
    }
}
