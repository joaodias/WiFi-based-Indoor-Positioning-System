package joaodias.thesis_02.Structures;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by joaodias on 10/2/15.
 */
public class WifiReading {
    private float pos_x;
    private float pos_y;
    private double timestamp;
    private List<ScanResult> wifiList;

    public WifiReading(float pos_x, float pos_y, double timestamp, List<ScanResult> wifiList){
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.timestamp = timestamp;
        this.wifiList = wifiList;
    }

    public float getPos_x() {
        return pos_x;
    }

    public float getPos_y() {
        return pos_y;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public String getBssid(int index){
        return wifiList.get(index).BSSID;
    }

    public int getRSSI(int index){
        return wifiList.get(index).level;
    }

    public List<ScanResult> getWifiList(){
        return this.wifiList;
    }
}
