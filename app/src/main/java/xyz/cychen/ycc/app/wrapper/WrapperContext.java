package xyz.cychen.ycc.app.wrapper;

import xyz.cychen.ycc.framework.Context;

import java.util.Map;

public class WrapperContext extends Context {
    private final Map<String, String> fields;
    public WrapperContext(String id, Map<String, String> fields) {
        super(id);
        this.fields = fields;
    }

    @Override
    public Map<String, String> toMap() {
        return fields;
    }
}
