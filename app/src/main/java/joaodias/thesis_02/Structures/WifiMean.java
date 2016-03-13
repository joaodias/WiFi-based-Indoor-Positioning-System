package joaodias.thesis_02.Structures;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joaodias on 10/5/15.
 */
public class WifiMean {
    private ArrayList<String> macList = new ArrayList<>();
    //private ArrayList<Integer> rssList = new ArrayList<>();
    private HashMap<String, ArrayList<Integer>> macRss = new HashMap<>();

    public ArrayList<String> getMacList() {
        return macList;
    }

//    public ArrayList<Integer> getRssList() {
//        return rssList;
//    }

    public ArrayList<Integer> getRss(String key){
        return this.macRss.get(key);
    }

    public void addRssList(String key, ArrayList<Integer> value){
        this.macRss.put(key, value);
    }

//    public void addRss(Integer value){
//        this.rssList.add(value);
//    }

    public void addMac(String value){
        this.macList.add(value);
    }

    public boolean containsKey(String value){
        if (macRss.containsKey(value)){
            return true;
        }
        else{
            return false;
        }
    }

}
