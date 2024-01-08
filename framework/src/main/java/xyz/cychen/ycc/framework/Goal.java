package xyz.cychen.ycc.framework;

public enum Goal {
    VIO, SAT, NULL;
    public boolean match(boolean truthvalue) {
        return (this == VIO && !truthvalue) || (this == SAT && truthvalue);
    }

    public static Goal flip(Goal goal) {
        return switch (goal) {
            case VIO -> SAT;
            case SAT -> VIO;
            case NULL -> NULL;
        };
    }
}
