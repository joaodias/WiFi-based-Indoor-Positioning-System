package joaodias.thesis_02.Structures;

/**
 * Created by joaodias on 10/1/15.
 */

public class LocDistance {
    private double distance;
    private String location;

    public LocDistance(double distance, String location) {
        this.distance = distance;
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public String getLocation() {
        return location;
    }
}
