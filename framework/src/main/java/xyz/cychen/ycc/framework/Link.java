package xyz.cychen.ycc.framework;

import java.util.ArrayList;
import java.util.List;

public final class Link implements Cloneable{
    public enum Type {
        VIO, SAT;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private final List<Binding> bindings;
    private final Type type;

    private Link(Type type) {
        if (type == null) throw new IllegalArgumentException();
        this.bindings = new ArrayList<>();
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    private Link(Type type, ArrayList<Binding> binding) {
        if (type == null) throw new IllegalArgumentException();
        this.type = type;
        this.bindings = binding;
    }

    /**
     * Get a link containing one binding which contains one assignment.
     * @param type - the link's type, can be vio or sat
     * @param var - the variable of the assignment
     * @param ctx - the value of the assignment
     * @return the link
     */
    public static Link of(Type type, Variable var, Context ctx) {
        Link result = new Link(type);
        Binding binding = new Binding();
        binding.bind(var, ctx);
        result.bindings.add(binding);
        return result;
    }

    /**
     * Get an empty link with type info.
     * @param type - the link's type
     * @return the empty link
     */
    public static Link of(Type type) {
        return new Link(type);
    }

    /**
     * Get a pure empty link that doesn't even have a type.
     * This method is intentionally left unimplemented.
     * @return the pure empty link
     */
    public static Link of() {
        throw new UnsupportedOperationException();
    }


    public static Link flip(Link link) {
        Link result = new Link(
                link.type == Type.VIO ? Type.SAT : Type.VIO,
                new ArrayList<>(link.bindings.size())
        );
        link.bindings.forEach(b -> result.bindings.add(b.clone()));
        return result;
    }

    public static Link union(Link link1, Link link2) {
        if (link1 == null && link2 == null) {
            return null;
        }
        else if (link1 == null) {
            return link2;
        }
        else if (link2 == null) {
            return link1;
        }

        Type type;
        if (link1.type != link2.type) {
            if (!link1.isEmpty() && !link2.isEmpty() || link1.isEmpty() && link2.isEmpty()) {
                throw new IllegalArgumentException();
            }
            else if (link1.isEmpty()) {
                type = link2.type;
            }
            else {
                type = link1.type;
            }
        }
        else {
            type = link1.type;
        }

        // TODO: Reduce redundant links
        Link result = new Link(
                type,
                new ArrayList<>(link1.bindings.size()+link2.bindings.size())
        );
        link1.bindings.forEach(b -> result.bindings.add(b.clone()));
        link2.bindings.forEach(b -> result.bindings.add(b.clone()));

        return result;
    }

    public static Link cartesian(Link link1, Link link2) {
        int bindingSize;
        if (link1 == null && link2 == null) {
            return Link.of();
        }
        else if (link1 == null) {
            return Link.copy(link2);
        }
        else if (link2 == null) {
            return Link.copy(link1);
        }
        if (link1.isEmpty()) {
            bindingSize = link2.size();
        }
        else if (link2.isEmpty()) {
            bindingSize = link1.size();
        }
        else {
            bindingSize = link1.size() * link2.size();
        }

        ArrayList<Binding> bindings = new ArrayList<>(bindingSize);
        if (!link1.isEmpty() && !link2.isEmpty()) {
            link1.bindings.forEach(b1 -> link2.bindings.forEach(
                    b2 -> bindings.add(Binding.cartesian(b1, b2))
            ));
        }
        else if (link1.isEmpty() && !link2.isEmpty()) {
            bindings.addAll(link2.bindings);
        }
        else if (!link1.isEmpty() && link2.isEmpty()) {
            bindings.addAll(link1.bindings);
        }

        // TODO: Consider the type of the result link more clearly
        return new Link(
                link1.type,
                bindings
        );
    }

    public boolean isEmpty() {
        return bindings.isEmpty();
    }

    public int size() {
        return bindings.size();
    }

    public void unionWith(Link link) {
        if (link == null) {
            return;
        }
        assert type == link.type;
        bindings.addAll(link.bindings);
    }

    public List<String> toStrings() {
        ArrayList<String> result = new ArrayList<>(bindings.size());
        bindings.forEach(b -> result.add("("+type+":"+b+")"));
        return result.stream().sorted().toList();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ArrayList<Binding> newBindings = new ArrayList<>(this.bindings);
        return new Link(type, newBindings);
    }

    public static Link copy(Link link) {
        if (link == null) {
            return null;
        }
        try {
            return (Link) link.clone();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return Link.of();
        }
    }
}
