package xyz.cychen.ycc.app.wrapper;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PatternEntry {

    public PatternEntry() {
    }

    private String patId;
    private ContextEntry[] contexts;

    @JsonProperty("pat_id")
    public String getPatId() {
        return patId;
    }

    public void setPatId(String patId) {
        this.patId = patId;
    }

    public ContextEntry[] getContexts() {
        return contexts;
    }

    public void setContexts(ContextEntry[] contexts) {
        this.contexts = contexts;
    }
}
