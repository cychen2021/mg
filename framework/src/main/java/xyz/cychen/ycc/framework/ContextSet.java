package xyz.cychen.ycc.framework;

import java.util.*;
import java.util.function.Consumer;

public class ContextSet implements Iterable<Context> {
    protected Set<Context> pool;
    protected String id;

    public ContextSet(String id) {
        this.id = id;
        this.pool = new HashSet<>();
    }

    public void purge() {
        pool.clear();
    }

    public boolean contains(Context o) {
        return pool.contains(o);
    }

    public String getId() {
        return id;
    }

    public int size() {
        return pool.size();
    }

    @Override
    public Iterator<Context> iterator() {
        return pool.iterator();
    }

    @Override
    public void forEach(Consumer<? super Context> action) {
        pool.forEach(action);
    }

    @Override
    public Spliterator<Context> spliterator() {
        return pool.spliterator();
    }

    public boolean add(Context c) {
        return pool.add(c);
    }

    public boolean remove(Context c) {
        return pool.remove(c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextSet that = (ContextSet) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
