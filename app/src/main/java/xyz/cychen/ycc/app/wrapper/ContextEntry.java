package xyz.cychen.ycc.app.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

class ContextEntry {

    public ContextEntry() {
    }

    private String ctxId;
    private Map<String, String> fields;

    public ContextEntry(String ctxId, Map<String, String> fields) {
        this.ctxId = ctxId;
        this.fields = fields;
    }

    @JsonProperty("ctx_id")
    public String getCtxId() {
        return ctxId;
    }

    public void setCtxId(String ctxId) {
        this.ctxId = ctxId;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }


}

