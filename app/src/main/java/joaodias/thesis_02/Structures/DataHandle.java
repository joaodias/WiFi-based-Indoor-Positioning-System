package joaodias.thesis_02.Structures;

/**
 * Created by joaodias on 10/1/15.
 */

import java.util.ArrayList;
import java.util.HashMap;

public class DataHandle{

    private HashMap<String, HashMap<String, String>> hash1 = new HashMap<>();
    private HashMap<String, String> hash2 = new HashMap<>();
    private ArrayList<String> positions =  new ArrayList<>();

    public DataHandle() {
    }

    public void addtoPositions(String value){
        positions.add(value);
    }

    public ArrayList<String> getPositions(){
        return this.positions;
    }

    public String getX(int index){
        String temp = positions.get(index);
        String parts [] = temp.split("&");
        return parts[0];
    }

    public String getY(int index){
        String temp = positions.get(index);
        String parts [] = temp.split("&");
        return parts[1];
    }

    public String MergeString(String value1, String value2){
        String final_value = value1 + "&" + value2;
        return final_value;
    }

    public HashMap<String, String> setHash2(String pos_x, String pos_y, String value1, String value2) {
        String final_value = value1 + "&" + value2;
        String key = pos_x + "&" + pos_y;
        this.hash2.put(key, final_value);
        return this.hash2;
    }

    public void setHash1(String mac_address, String pos_x, String pos_y, String value1, String value2){
        this.hash2 = new HashMap<>();
        this.hash1.put(mac_address, setHash2(pos_x, pos_y, value1, value2));
    }

    public String getRSSI(String mac_address, String pos_x, String pos_y){
        String key = pos_x + "&" + pos_y;
        HashMap<String, String> temp_hash = this.hash1.get(mac_address);
        String temp = temp_hash.get(key);
        String parts [] = temp.split("&");
        return parts[0];
    }
//
//    public String getDeviation(String mac_address, String pos_x, String pos_y){
//        String key = pos_x + "&" + pos_y;
//        HashMap<String, String> temp_hash = this.hash1.get(mac_address);
//        String temp = temp_hash.get(key);
//        String parts [] = temp.split("&");
//        return parts[1];
//    }

    public HashMap<String, HashMap<String, String>> getHash1(){
        return this.hash1;
    }

    public void update_ds(String mac, String pos_x, String pos_y, String value1, String value2){
        this.hash1.put(mac, updateHash2(this.hash1.get(mac), pos_x, pos_y, value1, value2));
    }

    public HashMap<String, String> updateHash2(HashMap<String, String> hash, String pos_x, String pos_y, String value1, String value2){
        String final_value = value1 + "&" + value2;
        String key = pos_x + "&" + pos_y;
        hash.put(key, final_value);
        return hash;
    }

//    public HashMap<String, String> getHash2(String key){
//        return this.hash1.get(key);
//    }
}
