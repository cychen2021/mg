package xyz.cychen.ycc.app.wrapper;

import xyz.cychen.ycc.framework.Context;
import xyz.cychen.ycc.framework.Named;
import xyz.cychen.ycc.framework.Predicate;
import xyz.cychen.ycc.framework.Variable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class WrapperPredicate implements Predicate, Named {
    public static void setBfuncClassPath(String bfuncClassPath) {
        File bfuncClassFile = new File(bfuncClassPath);
        String bfuncClassDir = bfuncClassFile.isFile() ? bfuncClassFile.getParent() : bfuncClassFile.getPath();
        String realDir = bfuncClassDir.isBlank() ? "." : bfuncClassDir;
        try (URLClassLoader loader =
                     new URLClassLoader(new URL[]{ new File(realDir).toURI().toURL() })) {
            Class<?> klass = loader.loadClass("Bfunction");
            realPredicate = klass.getDeclaredConstructor().newInstance();
            bfunc = klass.getDeclaredMethod("bfunc", String.class, Map.class);
            end = klass.getDeclaredMethod("end");
        } catch (Exception e) {
            System.err.println("Failed to load Bfunction.class");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private String bfuncName;
    private static Object realPredicate;
    private static Method bfunc;

    private static Method end;

    private final Variable[] variables;

    public WrapperPredicate(String bfuncName, Variable[] variables) {
        this.bfuncName = bfuncName;
        this.variables = variables;
    }

    @Override
    public boolean testOn(Context... args) {
        assert variables.length == args.length;
        Map<String, Map<String, String>> real_args = new HashMap<>();
        for (int i = 0; i < variables.length; i++) {
            real_args.put(variables[i].toString(), args[i].toMap());
        }
        try {
            return (boolean) bfunc.invoke(realPredicate, bfuncName, real_args);
        } catch (Exception e) {
            System.err.println("Failed to invoke Bfunction.bfunc");
            e.printStackTrace();
            System.exit(-1);
            return false;
        }
    }

    public static void terminate() {
        try {
            end.invoke(realPredicate);
        } catch (Exception e) {
            System.err.println("Failed to invoke Bfunction.end");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public String getName() {
        return bfuncName;
    }
}
