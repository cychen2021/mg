package xyz.cychen.ycc.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Binding implements Cloneable {
    private final Map<Variable, Context> map;

    public Binding() {
        map = new HashMap<>();
    }

    public static Binding union(Binding binding1, Binding binding2) {
        Binding result = new Binding();
        binding1.map.forEach(result::bind);
        binding2.map.forEach(result::bind);
        return result;
    }

    public void bind(Variable key, Context value) {
        Context exists = map.put(key, value);
        assert exists == null;
    }

    public void unbind(Variable key) {
        map.remove(key);
    }

    public Context get(Variable key) {
        return map.get(key);
    }

//    public void forEach(BiConsumer<? super Variable, ? super Context> action) {
//        map.forEach(action);
//    }

    public static Binding cartesian(Binding binding1, Binding binding2) {
        Binding result = new Binding();
        binding1.map.forEach(result::bind);
        binding2.map.forEach(result::bind);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        map.forEach((var, ctx) ->
                builder.append(var + ":=" + ctx.getId()).append(","));
        builder.append(">");
        return builder.toString();
    }

    @Override
    public Binding clone() {
        Binding result = new Binding();
        map.forEach((var, ctx) -> result.bind(var.clone(), ctx));
        return result;
    }

    private static boolean equalHelper( Map<Variable, Context> map1,
                                        Map<Variable, Context> map2) {
        if (map1 == null && map2 == null) {
            return true;
        }
        else if (map1 == null && map2 != null) {
            return false;
        }
        else if (map1 != null && map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) return false;
        for (Variable v: map1.keySet()) {
            if (!map2.get(v).equals(map1.get(v))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Binding binding = (Binding) o;
        return equalHelper(map, binding.map);
    }
}
