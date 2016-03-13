package joaodias.thesis_02.Structures;

/**
 * Created by joaodias on 10/2/15.
 */
public class MagneticReading {
    private float pos_x;
    private float pos_y;
    private double absolute;
    private float azimuth;
    private float pitch;
    private float roll;

    public MagneticReading(float pos_x, float pos_y, float azimuth, float pitch, float roll){
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.absolute = Math.sqrt((azimuth*azimuth) + (pitch*pitch) + (roll*roll));
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }

    public float getPos_x() {
        return pos_x;
    }

    public float getPos_y() {
        return pos_y;
    }

    public double getAbsolute() {
        return absolute;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

}
