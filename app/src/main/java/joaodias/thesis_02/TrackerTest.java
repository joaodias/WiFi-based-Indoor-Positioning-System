//package joaodias.thesis_02;
//
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.graphics.Bitmap;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.net.wifi.ScanResult;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import joaodias.thesis_02.Structures.DataHandle;
//import joaodias.thesis_02.Structures.DataObserved;
//import joaodias.thesis_02.Structures.MapNav;
//import joaodias.thesis_02.Utils.Algorithms;
//import joaodias.thesis_02.Utils.Databases;
//import joaodias.thesis_02.Utils.Operations;
//import joaodias.thesis_02.Views.ZoomImageViewNav;
//import joaodias.thesis_02.Wifi.SimpleWifiManager;
//import joaodias.thesis_02.Wifi.WifiReceiver;
//
//public class TrackerTest extends AppCompatActivity implements SensorEventListener {
//
//    ZoomImageViewNav zoomImageViewNav;
//    TextView textView;
//    String DBNAME;
//    String LOCALG = "mmse";
//    Databases _Databases = new Databases();
//
//    private SimpleWifiManager wifi;
//    private WifiReceiver receiverWifi;
//
//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
//    private Sensor mMagnetometer;
//
//    List<ScanResult> wifiList;
//
//    HashMap<String, Integer> obsmacrss = new HashMap<>();
//
//    boolean ready = false;
//    boolean wifi_ready = false;
//
//    String testX = "5";
//    String testY = "5";
//
//    double parameter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tracker);
//
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            DBNAME = extras.getString("DatabaseName");
//        }
//
//        zoomImageViewNav = (ZoomImageViewNav) findViewById(R.id.imageView);
//        textView = (TextView) findViewById(R.id.textView3);
//
//        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//
//        wifi = new SimpleWifiManager(TrackerTest.this);
//        //wifi.setScanResultsTextView(textView4);
//        receiverWifi = new SimpleWifiReceiver();
//        wifi.startScan(receiverWifi, "10");
//
////        Bitmap map = getMap();
////
////        if(map == null){
////            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
////            startActivity(myIntent);
////            finish();
////        }
////        else{
////            MapNav _map = MapNav.getInstance();
////
////            _map.setBaseImage(map);
////
////            zoomImageViewNav.setImageBitmap(_map.getBaseImage());
//
//        DataHandle dh = new DataHandle();
//
//        dh =  _Databases.build_MacList(Tracker.this, DBNAME, dh);
//        dh = _Databases.build_Positions(Tracker.this, DBNAME, dh);
//
//        parameter = calcParameter();
//
//        Toast.makeText(Tracker.this, "PAR: " + parameter, Toast.LENGTH_SHORT).show();
//
//        DataObserved datao = new DataObserved();
//
//        build_ObservedMacList_test(dh, datao.getObservedMacList());
//        build_ObservedRSS_test(datao, testX, testY, datao.getObservedMacList());
//
//        textView.setText(select_alg(LOCALG, dh, datao));
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        //}
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mSensorManager.registerListener(this, mAccelerometer, 1000000);
//        mSensorManager.registerListener(this, mMagnetometer, 1000000);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mSensorManager.unregisterListener(this, mAccelerometer);
//        mSensorManager.unregisterListener(this, mMagnetometer);
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // Do something here if sensor accuracy changes.
//        // You must implement this callback in your code.
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (ready && wifi_ready){
//            DataObserved datao = new DataObserved();
//            build_ObservedMacList(dh, datao.getObservedMacList());
//            build_ObservedRSS(datao, datao.getObservedMacList());
//            //Post-Positioning proccess need to be implemented
//            zoomImageViewNav.updateImageView(select_alg(LOCALG, dh, datao));
//        }
//    }
//
//    public Bitmap getMap(){
//        return _Databases.getMap(Tracker.this, DBNAME);
//    }
//
//    public void build_ObservedMacList(DataHandle dh, ArrayList<String> ObservedMacList){
//        //Compare Both Lists to check similar Mac Adress
//        for(int i = 0; i < wifiList.size(); i++){
//            if(wifiList.get(i).level > -90) {
//                ObservedMacList.add(wifiList.get(i).BSSID);
//                obsmacrss.put(wifiList.get(i).BSSID, wifiList.get(i).level);
//            }
//        }
//        for(int i = 0; i < ObservedMacList.size(); i++) {
//            if (dh.getHash1().get(ObservedMacList.get(i)) == null) {
//                ObservedMacList.remove(i);
//                i--;
//            }
//        }
//    }
//
//    public String select_alg(String selection, DataHandle dh, DataObserved datao){
//
//        Algorithms alg = new Algorithms();
//        String myloc = "";
//
//        switch(selection){
//            case "mmse":
//                myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), 6);
//                double error = alg.calcErrors(myloc, testX, testY);
//                myloc = String.valueOf(error);
//                break;
//
//            case "trilateration":
//                //dummy
//                myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), 6);
//
//                break;
//        }
//        return myloc;
//    }
//
//    public void build_ObservedRSS(DataObserved datao, ArrayList<String> ObservedMacList){
//        for(int i= 0; i<ObservedMacList.size(); i++) {
//            datao.setHash_observed(ObservedMacList.get(i), Integer.toString(obsmacrss.get(ObservedMacList.get(i))));
//        }
//    }
//
//    public void build_ObservedMacList_test(DataHandle dh, ArrayList<String> ObservedMacList){
//        SQLiteDatabase db = openOrCreateDatabase("logs.db3", MODE_PRIVATE, null);
//        String query = "select distinct mac_address from test_data_mean";
//        Cursor c = db.rawQuery(query, null);
//        int index = c.getColumnIndex("mac_address");
//
//        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
//            ObservedMacList.add(c.getString(index));
//        }
//
//        //Compare Both Lists to check similar Mac Adress
//        for(int i = 0; i < ObservedMacList.size(); i++) {
//            if (dh.getHash1().get(ObservedMacList.get(i)) == null) {
//                ObservedMacList.remove(i);
//            }
//        }
//        c.close();
//        db.close();
//    }
//
//    public DataObserved build_ObservedRSS_test(DataObserved datao, String testX, String testY, ArrayList<String> ObservedMacList){
//        SQLiteDatabase db = openOrCreateDatabase("logs.db3", MODE_PRIVATE, null);
//
//        //Build observed RSS Hash
//        for (int i = 0; i < ObservedMacList.size(); i++){
//            String query = "select * from test_data_mean where mac_address='" + ObservedMacList.get(i) + "' and pos_x=" + testX + " and pos_y=" + testY;
//            Cursor c = db.rawQuery(query, null);
//            int index = c.getColumnIndex("rssi");
//
//            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//                datao.setHash_observed(ObservedMacList.get(i), c.getString(index));
//            }
//
//            c.close();
//        }
//        db.close();
//
//        return datao;
//    }
//
//    public double calcParameter(){
//        ArrayList<String> TestPositions = _Databases.getTestPositions(Tracker.this, DBNAME);
//
//        ArrayList<Integer> ErrorList_mmse = new ArrayList<>();
//
//        int bestParameter_mmse = 1;
//
//        HashMap<String, Integer> Parameters_mmse = new HashMap<>();
//
//        for(int i = 0; i<TestPositions.size(); i++){ //7 is the number of tps
//            DataObserved datao = new DataObserved();
//            Algorithms alg = new Algorithms();
//
//            //big numbers to not disturb the results
//            double error_mmse = 1000;
//
//            String positions[] = TestPositions.get(i).split("&");
//            build_ObservedMacList_test(dh, datao.getObservedMacList());
//            datao = build_ObservedRSS_test(datao, positions[0], positions[1], datao.getObservedMacList());
//
//            for (int j=1; j<=15; j++) {
//                String myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), j);
//                double v1 = alg.calcErrors(myloc, positions[0], positions[1]);
//                if(v1 < error_mmse){
//                    bestParameter_mmse = j;
//                    error_mmse = v1;
//                }
//            }
//
//            ErrorList_mmse.add(i, bestParameter_mmse);
//            Parameters_mmse.put(TestPositions.get(i), bestParameter_mmse);
//        }
//
//        Operations _Operations = new Operations();
//        return _Operations.calculateAverage(ErrorList_mmse);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_tracker, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //TODO: switch this id
//        if (id == 16908332) {
//            Intent intent = new Intent(Tracker.this, MainActivity.class);
//            this.finish();
//            Tracker.this.startActivity(intent);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    public class SimpleWifiReceiver extends WifiReceiver {
//
//        @Override
//        public void onReceive(Context c, Intent intent) {
//            try {
//                wifiList = wifi.getScanResults();
//
//                wifi_ready = true;
//            } catch (RuntimeException e) {
//                return;
//            }
//        }
//    }
//}
//
