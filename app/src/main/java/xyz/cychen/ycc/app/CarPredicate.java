package xyz.cychen.ycc.app;

import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Predicate;

import java.util.*;

class MyRandom extends Random {
    protected int dom;
    protected int nom;

    public MyRandom(int nom, int dom) {
        this.dom = dom;
        this.nom = nom;
    }

    public MyRandom(long seed, int nom, int dom) {
        super(seed);
        this.dom = dom;
        this.nom = nom;
    }

    @Override
    public boolean nextBoolean() {
        int r = this.nextInt(dom);
        return r < nom;
    }
}

public class CarPredicate {
    public static Predicate same = new Same();
    public static Predicate szLocRange = new SZLocRange();
    public static Predicate szLocClose = new SZLocClose();
    public static Predicate szSpdClose = new SZSpdClose();
    public static Predicate szLocDist = new SZLocDist();

    public static class RandomP implements Predicate {
        protected static Integer dom = null;
        protected static Integer nom = null;

        public static void setProb(int nom, int dom) {
            RandomP.nom = nom;
            RandomP.dom = dom;
        }

        protected MyRandom random;

        public RandomP() {
            this.random = new MyRandom(nom, dom);
        }

        @Override
        public boolean testOn(Context... args) {
            return random.nextBoolean();
        }
    }

    public static class RandomP4Valid extends RandomP {
        public static class Cache {

            protected int roundCount;

            protected Map<Integer, Map<Key, Boolean>> cache = new HashMap<>();

            public void clearCache() {
                cache = new HashMap<>();
            }

            public void incRoundCount() {
                this.roundCount++;
            }

            public void resetRoundCount() {
                this.roundCount = 0;
            }

            protected static class Key {
                private final String[] contexts;
                private final int round;


                public Key(int round, Context[] contexts) {
                    this.round = round;
                    this.contexts = new String[contexts.length];
                    for (int i=0; i<contexts.length; i++) {
                        this.contexts[i] = contexts[i].getId();
                    }
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    Key key = (Key) o;
                    return round == key.round && Arrays.equals(contexts, key.contexts);
                }

                @Override
                public int hashCode() {
                    int result = Objects.hash(round);
                    result = 31 * result + Arrays.hashCode(contexts);
                    return result;
                }
            }

            protected void cacheIn(int id, int round, Context[] args, boolean val) {
                if (!cache.containsKey(id)) {
                    cache.put(id, new HashMap<>());
                }
                var key = new Key(round, args);
                if (cache.get(id).containsKey(key)) {
                    System.err.println("Cache key conflict!");
                    System.exit(-1);
                }
                cache.get(id).put(key, val);
            }

            protected boolean cacheOut(int id, int round, Context[] args) {
                return cache.get(id).get(new Key(round, args));
            }

            protected enum Mode {
                CACHE_IN, CACHE_OUT;
            }

            protected Mode mode = Mode.CACHE_IN;

            public void setCacheIn() {
                mode = Mode.CACHE_IN;
            }

            public void setCacheOut() {
                mode = Mode.CACHE_OUT;
            }
        }

        protected Cache cache = null;

        protected int id;

        public RandomP4Valid(int id, Cache cache) {
            this.id = id;
            this.cache = cache;
        }

        public void setCache(Cache cache) {
            this.cache = cache;
        }

        @Override
        public boolean testOn(Context... args) {
            switch (cache.mode) {
                case CACHE_IN:
                    boolean result = random.nextBoolean();
                    cache.cacheIn(id, cache.roundCount, args, result);
                    return result;
                case CACHE_OUT:
                    return cache.cacheOut(id, cache.roundCount, args);
                default:
                    System.exit(-1);
                    return false;
            }
        }
    }

    private static class Same implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            assert args.length == 2;
            return ((CarContext) args[0]).getCarID().equals(((CarContext) args[1]).getCarID());
        }
    }

    private static class SZLocRange implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            assert args.length == 1;
            double longitude = ((CarContext) args[0]).getLongitude();
            double latitude = ((CarContext) args[0]).getLatitude();
            return longitude >= 112.0 && longitude <= 116.0
                    && latitude >= 20.0 && latitude <= 24.0;
        }
    }

    private static double computeDistanceSq(CarContext ctx1, CarContext ctx2) {
        double long1 = ctx1.getLongitude();
        double lati1 = ctx1.getLatitude();
        double long2 = ctx2.getLongitude();
        double lati2 = ctx2.getLatitude();
        return (long1 - long2) * (long1 - long2)
                + (lati1 - lati2) * (lati1 - lati2);
    }

    private static class SZLocClose implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            assert args.length == 2;
            double distSq = computeDistanceSq((CarContext) args[0], (CarContext) args[1]);
            return distSq <= 0.001 * 0.001;
        }
    }

    private static class SZSpdClose implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            assert args.length == 2;
            return Math.abs(((CarContext) args[0]).getSpeed() - ((CarContext) args[1]).getSpeed()) <= 50;
        }
    }

    private static class SZLocDist implements Predicate {
        @Override
        public boolean testOn(Context... args) {
            assert args.length == 2;
            double distSq = computeDistanceSq((CarContext) args[0], (CarContext) args[1]);
            return distSq <= 0.025 * 0.025;
        }
    }
}
