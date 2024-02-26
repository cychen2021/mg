package xyz.cychen.ycc.framework;

public abstract class Context {
    public Context(String id) {
        this.id = id;
    }

    protected String id;
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return id.equals(context.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
