package xyz.cychen.ycc.app;

import org.javatuples.Pair;
import xyz.cychen.ycc.framework.Scheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class ContextChangeBuilder {
    private static long count = 1;

    private String contextFileName;
    private Scheduler scheduler;

    public ContextChangeBuilder(String contextFileName, Scheduler scheduler) {
        this.contextFileName = contextFileName;
        this.scheduler = scheduler;
    }

    public List<Scheduler.Event> build() {
        try (BufferedReader file =
                     new BufferedReader(new FileReader(contextFileName))) {
            List<Scheduler.Event> result = new LinkedList<>();
            String line;
            while ((line = file.readLine()) != null) {
                String[] tokens = line.split(",");
                String indicator = tokens[0];
                assert indicator.equals("+") || indicator.equals("-");
                String target = tokens[1];
                String id = tokens[2];
                long timestamp = Long.parseLong(tokens[3]);
                String carID = tokens[4];
                String object = tokens[5];
                String[] objects = object.split("_");
                CarContext context = new CarContext(id, carID, Double.parseDouble(objects[0]),
                        Double.parseDouble(objects[1]), Double.parseDouble(objects[2]), true);
                Scheduler.Event e;
                if (indicator.equals("+")) {
                    e = scheduler.new IncEvent(timestamp, target, context);
                }
                else {
                    e = scheduler.new DecEvent(target, context);
                }
                result.add(e);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
}
