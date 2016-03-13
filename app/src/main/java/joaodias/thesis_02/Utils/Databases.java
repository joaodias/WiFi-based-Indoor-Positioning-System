package joaodias.thesis_02.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import joaodias.thesis_02.Structures.DataHandle;
import joaodias.thesis_02.Structures.DataObserved;
import joaodias.thesis_02.Structures.MagneticReading;
import joaodias.thesis_02.Structures.MapNav;
import joaodias.thesis_02.Structures.WifiMean;
import joaodias.thesis_02.Structures.WifiReading;

/**
 * Created by joaodias on 10/1/15.
 */
public class Databases {

    //insert activity object, array with queries to create tables
    public void createDB(Activity activity, String DBNAME, ArrayList<String> queries){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        for (int i = 0; i < queries.size(); i++){
            try {
                db.execSQL(queries.get(i));
            }
            catch(Exception e){
                Log.d("Database", "Failure executing query,", e);
            }
        }
        db.close();
    }

    public void add_magnetic(Activity activity, String DBNAME, MagneticReading mr) {
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);

        try {
            ContentValues cv1 = new ContentValues();
            cv1.put("pos_x", mr.getPos_x());
            cv1.put("pos_y", mr.getPos_y());
            cv1.put("absolute", mr.getAbsolute());
            cv1.put("azimuth", mr.getAzimuth());
            cv1.put("pitch", mr.getPitch());
            cv1.put("roll", mr.getRoll());
            db.insert("magnetic", null, cv1);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "Error writing to magnetic table", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public WifiMean add_wifi(Activity activity, String DBNAME, String TABLE, WifiReading wr, WifiMean wm) {
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        Operations _Operations = new Operations();

        try{
            for(int i=0; i< wr.getWifiList().size(); i++) {
                if (wr.getRSSI(i) > -90) {
                    ContentValues cv = new ContentValues();
                    cv.put("pos_x", wr.getPos_x());
                    cv.put("pos_y", wr.getPos_y());
                    cv.put("timestamp", wr.getTimestamp());
                    cv.put("mac_address", wr.getBssid(i));
                    cv.put("rssi", wr.getRSSI(i));
                    db.insert(TABLE, null, cv);

                    //to build wifi_mean
                    if(wm.containsKey(wr.getBssid(i))){
                        //add rss to existing list
                        wm.getRss(wr.getBssid(i)).add(wr.getRSSI(i));
                    }
                    else{
                        ArrayList<Integer> rss = new ArrayList<>();
                        rss.add(wr.getRSSI(i));
                        wm.addRssList(wr.getBssid(i), rss);
                        wm.addMac(wr.getBssid(i));
                    }
                }
            }
        } catch (RuntimeException e) {
            Toast.makeText(activity, "Error writing to wifi table", Toast.LENGTH_SHORT).show();
            return null;
        }
        db.close();
        return wm;
    }

    public void delete_all_db(Activity activity, String DBNAME){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        try{
            db.execSQL("delete from magnetic");
            db.execSQL("delete from wifi");
            Toast.makeText(activity, "Databases were deleted", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(activity, "Error deleting DBs", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    public void addDBMap(Activity activity, String DBNAME, Bitmap bitmap, Float value){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try{
            ContentValues cv = new  ContentValues();
            cv.put("image", byteArray);
            cv.put("orientation", value);
            db.insert("map", null, cv);
        }
        catch(Exception e){
            Toast.makeText(activity, "Error writing to the database", Toast.LENGTH_SHORT).show();
        }
    }

    public MapNav getMap(Activity activity, String DBNAME, MapNav _map){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        String query = "select * from map";

        try{
            Cursor c = db.rawQuery(query, null);
            int index = c.getColumnIndex("image");
            int index1 = c.getColumnIndex("orientation");
            byte[] image = null;
            float orientation = 0;

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                image = c.getBlob(index);
                orientation = Float.valueOf(c.getString(index1));
            }
            db.close();
            if (image != null){
                _map.setBaseImage(BitmapFactory.decodeByteArray(image, 0, image.length));
                _map.setOrientation(orientation);
                return _map;
            }
            else{
                return null;
            }
        }
        catch(Exception e){
            Toast.makeText(activity, "Error querying the database", Toast.LENGTH_SHORT).show();
            db.close();
            return null;
        }
    }

    public void addWifiMean(Activity activity, String DBNAME, String TABLE_MEAN, double pos_x, double pos_y, WifiMean wm){

        Operations _Operations = new Operations();
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);

        for (int i = 0; i < wm.getMacList().size(); i++){
            ArrayList<Integer> rss = wm.getRss(wm.getMacList().get(i));
            double average =_Operations.calc_vect_average(rss);
            double stdev = _Operations.calc_vect_stdev(average, rss);

            ContentValues cv = new ContentValues();
            cv.put("pos_x", pos_x);
            cv.put("pos_y", pos_y);
            cv.put("rssi", average);
            cv.put("deviation", stdev);
            cv.put("mac_address", wm.getMacList().get(i));
            db.insert(TABLE_MEAN, null, cv);
        }
        db.close();
    }

    public DataHandle build_MacList(Activity activity, String DBNAME, DataHandle dh){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        String query = "select * from wifi_mean";
        Cursor c = db.rawQuery(query, null);

        int index = c.getColumnIndex("mac_address");
        int index1 = c.getColumnIndex("rssi");
        int index2 = c.getColumnIndex("pos_x");
        int index3 = c.getColumnIndex("pos_y");
        int index4 = c.getColumnIndex("deviation");

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String mac = c.getString(index);
            String rssi = c.getString(index1);
            String x = c.getString(index2);
            String y = c.getString(index3);
            String deviation = c.getString(index4);

            if(dh.getHash1().get(mac) == null){
                dh.setHash1(mac, x, y, rssi, deviation);
            }
            else{
                dh.update_ds(mac, x, y, rssi, deviation);
            }
        }

        c.close();
        db.close();

        return dh;
    }

    public DataHandle build_Positions(Activity activity, String DBNAME, DataHandle dh){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        String query = "select * from wifi_mean";
        Cursor c = db.rawQuery(query, null);

        int index = c.getColumnIndex("pos_x");
        int index1 = c.getColumnIndex("pos_y");

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String x = c.getString(index);
            String y = c.getString(index1);

            String temp = dh.MergeString(x, y);

            if(dh.getPositions().contains(temp) == true){
            }
            else{
                dh.addtoPositions(temp);
            }
        }
        c.close();
        db.close();
        return dh;
    }

    public ArrayList<String> getTestPositions(Activity activity, String DBNAME){ //To get all positions in the db to perform the test to find the parameter
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        String query = "select * from test_data_mean";

        Cursor c = db.rawQuery(query, null);
        int index = c.getColumnIndex("pos_x");
        int index1 = c.getColumnIndex("pos_y");
        ArrayList<String> TestPositions = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            if(!TestPositions.contains(c.getString(index) + "&" + c.getString(index1))){
                TestPositions.add(c.getString(index) + "&" + c.getString(index1));
            }
        }
        c.close();
        db.close();

        return TestPositions;
    }

    public ArrayList<String> build_ObservedMacList_tp(Activity activity, String DBNAME, DataHandle dh, ArrayList<String> ObservedMacList){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);
        String query = "select distinct mac_address from test_data_mean";
        Cursor c = db.rawQuery(query, null);
        int index = c.getColumnIndex("mac_address");

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            ObservedMacList.add(c.getString(index));
        }

        //Compare Both Lists to check similar Mac Adress
        for(int i = 0; i < ObservedMacList.size(); i++) {
            if (dh.getHash1().get(ObservedMacList.get(i)) == null) {
                ObservedMacList.remove(i);
            }
        }
        c.close();
        db.close();

        return ObservedMacList;
    }

    public DataObserved build_ObservedRSS_tp(Activity activity, String DBNAME, DataObserved datao, String testX, String testY, ArrayList<String> ObservedMacList){
        SQLiteDatabase db = activity.openOrCreateDatabase(DBNAME + ".db3", activity.MODE_PRIVATE, null);

        //Build observed RSS Hash
        for (int i = 0; i < ObservedMacList.size(); i++){
            String query = "select * from test_data_mean where mac_address='" + ObservedMacList.get(i) + "' and pos_x=" + testX + " and pos_y=" + testY;
            Cursor c = db.rawQuery(query, null);
            int index = c.getColumnIndex("rssi");

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                datao.setHash_observed(ObservedMacList.get(i), c.getString(index));
            }

            c.close();
        }
        db.close();

        return datao;
    }
}
