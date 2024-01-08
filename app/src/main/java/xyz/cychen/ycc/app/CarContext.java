package xyz.cychen.ycc.app;

import xyz.cychen.ycc.framework.Context;

public class CarContext extends Context {
    private final String carID;
    private final double longitude;
    private final double latitude;
    private final double speed;
    private final boolean inUse;

    public CarContext(String id, String carID, double longitude,
                      double latitude, double speed, boolean inUse) {
        super(id);
        this.carID = carID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.inUse = inUse;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isInUse() {
        return inUse;
    }

    public String getCarID() {
        return carID;
    }
}
