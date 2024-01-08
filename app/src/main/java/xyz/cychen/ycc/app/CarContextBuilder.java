package xyz.cychen.ycc.app;

import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;


public class CarContextBuilder {
    private static long count = 1;

    private String contextFileName;

    public CarContextBuilder(String contextFileName) {
        this.contextFileName = contextFileName;
    }

    public List<Pair<Long, CarContext>> build() {
        try (BufferedReader file =
                     new BufferedReader(new FileReader(contextFileName))) {
            List<Pair<Long, CarContext>> result = new LinkedList<>();
            String line;
            while ((line = file.readLine()) != null) {
                var item = fromLine(line);
                result.add(item);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    protected Pair<Long, CarContext> fromLine(String line) {
        String id = Long.toString(count);
        count++;

        line = line.strip();
        String[] tokens = line.split(",");
        assert tokens.length == 9;
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        long timestamp = -1;
        try {
             timestamp = simpleDateFormat.parse(tokens[0]).getTime();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String carID = tokens[2];
        double longitude = Double.parseDouble(tokens[3]);
        double latitude = Double.parseDouble(tokens[4]);
        double speed = Double.parseDouble(tokens[5]);
        boolean inUse = Integer.parseInt(tokens[7]) == 1;
        return Pair.with(timestamp, new CarContext(id, carID, longitude, latitude, speed, inUse));
    }
}
