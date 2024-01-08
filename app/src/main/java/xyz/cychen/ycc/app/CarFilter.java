package xyz.cychen.ycc.app;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Scheduler;

import java.util.ArrayList;

public class CarFilter {
    public static final Scheduler.Filter sutPC0 = new SutPCFilter(0);
    public static final Scheduler.Filter sutPC1 = new SutPCFilter(1);
    public static final Scheduler.Filter sutPC2 = new SutPCFilter(2);
    public static final Scheduler.Filter sutPC3 = new SutPCFilter(3);
    public static final Scheduler.Filter sutPC4 = new SutPCFilter(4);
    public static final Scheduler.Filter sutPC5 = new SutPCFilter(5);
    public static final Scheduler.Filter sutPC6 = new SutPCFilter(6);
    public static final Scheduler.Filter sutPC7 = new SutPCFilter(7);
    public static final Scheduler.Filter sutPC8 = new SutPCFilter(8);
    public static final Scheduler.Filter sutPC9 = new SutPCFilter(9);
    public static final Scheduler.Filter sutPCA = new SutPCFilter("A");
    public static final Scheduler.Filter sutPCB = new SutPCFilter("B");
    public static final Scheduler.Filter sutPCC = new SutPCFilter("C");
    public static final Scheduler.Filter sutPCD = new SutPCFilter("D");
    public static final Scheduler.Filter sutPCE = new SutPCFilter("E");
    public static final Scheduler.Filter sutPCF = new SutPCFilter("F");
    public static final Scheduler.Filter sutPCG = new SutPCFilter("G");
    public static final Scheduler.Filter sutPCH = new SutPCFilter("H");
    public static final Scheduler.Filter sutPCI = new SutPCFilter("I");
    public static final Scheduler.Filter sutPCJ = new SutPCFilter("J");
    public static final Scheduler.Filter sutPCK = new SutPCFilter("K");
    public static final Scheduler.Filter sutPCL = new SutPCFilter("L");
    public static final Scheduler.Filter sutPCM = new SutPCFilter("M");
    public static final Scheduler.Filter sutPCN = new SutPCFilter("N");
    public static final Scheduler.Filter sutPCO = new SutPCFilter("O");
    public static final Scheduler.Filter sutPCP = new SutPCFilter("P");
    public static final Scheduler.Filter sutPCQ = new SutPCFilter("Q");
    public static final Scheduler.Filter sutPCR = new SutPCFilter("R");
    public static final Scheduler.Filter sutPCS = new SutPCFilter("S");
    public static final Scheduler.Filter sutPCT = new SutPCFilter("T");
    public static final Scheduler.Filter sutPCU = new SutPCFilter("U");
    public static final Scheduler.Filter sutPCV = new SutPCFilter("V");
    public static final Scheduler.Filter sutPCW = new SutPCFilter("W");
    public static final Scheduler.Filter sutPCX = new SutPCFilter("X");
    public static final Scheduler.Filter sutPCY = new SutPCFilter("Y");
    public static final Scheduler.Filter sutPCZ = new SutPCFilter("Z");
    public static final Scheduler.Filter runWithService = new RunWithServiceFilter();
    public static final Scheduler.Filter hotAreaA =
            new PolygenFilter(
                    Pair.with(113.923059,22.571615),
                    Pair.with(113.864853,22.573121),
                    Pair.with(113.882534,22.590556),
                    Pair.with(113.901760,22.590873)
            );
    public static final Scheduler.Filter hotAreaB =
            new PolygenFilter(
                    Pair.with(113.89455,22.548391),
                    Pair.with(113.864853,22.573121),
                    Pair.with(113.882534,22.590556),
                    Pair.with(113.90176,22.590873)
            );
    public static final Scheduler.Filter hotAreaC =
            new PolygenFilter(
                    Pair.with(113.923059,22.571615),
                    Pair.with(113.89455,22.548391),
                    Pair.with(113.864853,22.573121),
                    Pair.with(113.882534,22.590556),
                    Pair.with(113.901761,22.590873)
            );
    public static final Scheduler.Filter hotAreaD =
            new PolygenFilter(
                    Pair.with(114.02018,22.559489),
                    Pair.with(114.085411,22.570902),
                    Pair.with(114.060348,22.503359)
            );

    public static final Scheduler.Filter hotAreaE =
            new PolygenFilter(
                    Pair.with(114.092304,22.559489),
                    Pair.with(114.142402,22.571853),
                    Pair.with(114.135879,22.541416),
                    Pair.with(114.08485,22.532457)
            );

    public static final Scheduler.Filter hotAreaF =
            new PolygenFilter(
                    Pair.with(114.02018,22.559489),
                    Pair.with(114.085411,22.570902),
                    Pair.with(114.142402,22.571853),
                    Pair.with(114.135879,22.541416),
                    Pair.with(114.060348,22.503359)
            );

    public static final Scheduler.Filter hotAreaG =
            new PolygenFilter(
                    Pair.with(113.927826,22.565195),
                    Pair.with(114.015056,22.55616),
                    Pair.with(114.019691,22.528414),
                    Pair.with(113.937809,22.514936),
                    Pair.with(113.902618,22.531744)
            );

    public static final Scheduler.Filter hotAreaH =
            new PolygenFilter(
                    Pair.with(113.927826,22.565195),
                    Pair.with(114.015056,22.55616),
                    Pair.with(113.988792,22.611317)
            );

    public static final Scheduler.Filter hotAreaI = hotAreaG;

    public static final Scheduler.Filter any = context -> true;

    protected static class SutPCFilter
            implements Scheduler.Filter {
        private final String tailNumber;

        public SutPCFilter(int tailNumber) {
            this.tailNumber = Integer.toString(tailNumber);
        }

        public SutPCFilter(String tail) {
            this.tailNumber = tail;
        }

        @Override
        public boolean filter(Context context1) {
            CarContext context = (CarContext) context1;
            return context.getCarID().endsWith(tailNumber);
        }
    }

    protected static class RunWithServiceFilter
        implements Scheduler.Filter {
        @Override
        public boolean filter(Context context1) {
            CarContext context = (CarContext) context1;
            return context.isInUse();
        }
    }

    public static class ConjunctiveFilter implements Scheduler.Filter {
        private final Scheduler.Filter[] filters;

        public ConjunctiveFilter(Scheduler.Filter... filters) {
            this.filters = filters;
        }

        @Override
        public boolean filter(Context context) {
            if (filters == null || filters.length == 0) {
                return true;
            }
            else {
                for (var f: filters) {
                    if (!f.filter(context)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    protected static class PolygenFilter implements Scheduler.Filter {
        private final Pair<Double, Double>[] points;

        public PolygenFilter(Pair<Double, Double>... points) {
            this.points = points;
        }

        @Override
        public boolean filter(Context context1) {
            CarContext context = (CarContext) context1;
            Pair<Double, Double> ctxPoint = Pair.with(context.getLongitude(), context.getLatitude());
            ArrayList<Pair<Double, Double>> vecs = new ArrayList<>(points.length);
            for (int i = 0; i < points.length; i++) {
                vecs.add(i, subtract(points[i], ctxPoint));
            }
            boolean firstNeg = times(vecs.get(points.length - 1), vecs.get(0)) < 0;
            for (int i = 0; i < points.length - 1; i++) {
                boolean neg = times(vecs.get(i), vecs.get(i+1)) < 0;
                if (neg != firstNeg) {
                    return false;
                }
            }
            return true;
        }

        private static Pair<Double, Double> subtract(Pair<Double, Double> op1, Pair<Double, Double> op2) {
            return Pair.with(op1.getValue0()-op2.getValue0(), op1.getValue1()-op2.getValue1());
        }

        private static double times(Pair<Double, Double> op1, Pair<Double, Double> op2) {
            return op1.getValue0() * op2.getValue1() - op1.getValue1() * op2.getValue0();
        }
    }
}
