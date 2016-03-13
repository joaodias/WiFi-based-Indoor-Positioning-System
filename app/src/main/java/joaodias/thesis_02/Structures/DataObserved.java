package joaodias.thesis_02.Structures;

/**
 * Created by joaodias on 10/1/15.
 */

import java.util.ArrayList;
import java.util.HashMap;

public class DataObserved {

    private HashMap<String, String> hash_observed = new HashMap<>();

    private ArrayList<String> ObservedMacList = new ArrayList<>();

//    public void setObservedMacList(String mac){
//        this.ObservedMacList.add(mac);
//    }

    public void equalObservedMacList(ArrayList<String> value){
        this.ObservedMacList = value;
    }

    public ArrayList<String> getObservedMacList(){
        return this.ObservedMacList;
    }

    public void setHash_observed(String key, String value){
        this.hash_observed.put(key, value);
    }

//    public HashMap<String, String> getHash_observed(){
//        return this.hash_observed;
//    }

    public String getRssi_observed(String key){
        return this.hash_observed.get(key);
    }
}
