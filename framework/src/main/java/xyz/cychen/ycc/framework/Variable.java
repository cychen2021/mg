package xyz.cychen.ycc.framework;

import java.util.Objects;

public record Variable(String variable) implements Cloneable {

    @Override
    public String toString() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable1 = (Variable) o;
        return variable.equals(variable1.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    @Override
    public Variable clone() {
        return new Variable(variable);
    }
}
