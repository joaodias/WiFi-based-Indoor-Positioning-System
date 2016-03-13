package joaodias.thesis_02;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import joaodias.thesis_02.Structures.DataHandle;
import joaodias.thesis_02.Structures.DataObserved;
import joaodias.thesis_02.Structures.MapNav;
import joaodias.thesis_02.Utils.Algorithms;
import joaodias.thesis_02.Utils.Databases;
import joaodias.thesis_02.Utils.Operations;
import joaodias.thesis_02.Views.ZoomImageViewNav;
import joaodias.thesis_02.Wifi.SimpleWifiManager;
import joaodias.thesis_02.Wifi.WifiReceiver;

public class Tracker extends AppCompatActivity implements SensorEventListener {

    ZoomImageViewNav zoomImageViewNav;
    TextView textView;
    String DBNAME;
    String LOCALG = "mmse";
    Databases _Databases = new Databases();

    private SimpleWifiManager wifi;
    private WifiReceiver receiverWifi;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    List<ScanResult> wifiList;

    DataHandle dh = new DataHandle();

    HashMap<String, Integer> obsmacrss = new HashMap<>();

    boolean ready = false;
    boolean wifi_ready = false;
    boolean parameter_calculated = false;
    double parameter = 1;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private double STEP_THRESHOLD = 0.7;

    private double startTimeSteps = 0;
    private double finalTimeSteps = 0;
    private double durationSteps = 0;

    private double THRESHOLD_TIME_STEPS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DBNAME = extras.getString("DatabaseName");
        }

        zoomImageViewNav = (ZoomImageViewNav) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView3);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        wifi = new SimpleWifiManager(Tracker.this);
        //wifi.setScanResultsTextView(textView4);
        receiverWifi = new SimpleWifiReceiver();
        wifi.startScan(receiverWifi, "10");

        MapNav _map = MapNav.getInstance();
        _map = getMap(_map);

        if(_map.getBaseImage() == null){
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(myIntent);
            finish();
        }
        else{

            dh = _Databases.build_MacList(Tracker.this, DBNAME, dh);
            dh = _Databases.build_Positions(Tracker.this, DBNAME, dh);

            parameter = calcParameter();

            zoomImageViewNav.setImageBitmap(_map.getBaseImage());

            ready = true;

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, 200000);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (ready && wifi_ready && parameter_calculated){

            //Orientation
            if (event.sensor == mAccelerometer) {
                if (isStep(event)) {
                    finalTimeSteps = System.currentTimeMillis();
                    durationSteps = finalTimeSteps - startTimeSteps;
                    if(durationSteps < THRESHOLD_TIME_STEPS){
                        textView.setText("Moving... ");
                    }
                    else{
                        textView.setText("Stopped... ");
                    }
                    startTimeSteps = System.currentTimeMillis();
                }
                else{
                    finalTimeSteps = System.currentTimeMillis();
                    durationSteps = finalTimeSteps - startTimeSteps;
                    if(durationSteps > THRESHOLD_TIME_STEPS || startTimeSteps == 0){
                        textView.setText("Stopped... ");
                    }
                }
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor == mMagnetometer) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);
                float azimuthInRadians = mOrientation[0];
                float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;

                mCurrentDegree = -azimuthInDegress;
            }

            MapNav _map = MapNav.getInstance();

            mCurrentDegree = mCurrentDegree - _map.getOrientation();
            mCurrentDegree = -mCurrentDegree;

            //Localization
            DataObserved datao = new DataObserved();
            datao.equalObservedMacList(build_ObservedMacList(dh, datao.getObservedMacList()));
            datao = build_ObservedRSS(datao, datao.getObservedMacList());
            //textView.setText("" + mCurrentDegree);
            String curLoc = select_alg(LOCALG, dh, datao);
            //curLoc = stabilizeNavigation();
            zoomImageViewNav.updateImageView(curLoc, mCurrentDegree);
        }
    }

    public boolean isStep(SensorEvent event){
        float[] lastEvent = mLastAccelerometer;

        ArrayList<float[]> accVect = new ArrayList<>();
        ArrayList<Double> accelScalar = new ArrayList<>();
        ArrayList<Double> accelScalarNoGravity = new ArrayList<>();
        ArrayList<Boolean> aboveZero = new ArrayList<>();

        accVect.add(0, lastEvent);
        accVect.add(1, event.values);

        for (int i = 0; i < accVect.size(); i++){
            accelScalar.add(Math.sqrt(Math.pow(accVect.get(i)[0], 2) + Math.pow(accVect.get(i)[1], 2) + Math.pow(accVect.get(i)[2], 2)));
            accelScalarNoGravity.add(accelScalar.get(i) - 9.8);
            if (accelScalarNoGravity.get(i) > STEP_THRESHOLD){
                aboveZero.add(true);
            }
            else{
                aboveZero.add(false);
            }
        }

        if(aboveZero.get(0) == false && aboveZero.get(1) == true){
            return true;
        }
        else{
            return false;
        }
    }

    public MapNav getMap(MapNav _map){
        return _Databases.getMap(Tracker.this, DBNAME, _map);
    }

//    public String stabilizeNavigation(String curLoc){
//        Algorithms alg = new Algorithms();
//
//        String parts[] = lastMyloc.split("&");
//
//        //1 - Detect if movement happened
//        if(alg.calcErrors(curLoc, parts[0], parts[1]) > THRESHOLD_LAST_POS){
//            // Do some calculations (maybe move a bit instead of hardcore moves)
//            return lastMyloc;
//        }
//        else{
//            return lastMyloc;
//        }
//
//        //3 - orientation
//    }

    public ArrayList<String> build_ObservedMacList(DataHandle dh, ArrayList<String> ObservedMacList){
        //Compare Both Lists to check similar Mac Adress
        for(int i = 0; i < wifiList.size(); i++){
            if(wifiList.get(i).level > -90) {
                ObservedMacList.add(wifiList.get(i).BSSID);
                obsmacrss.put(wifiList.get(i).BSSID, wifiList.get(i).level);
            }
        }
        for(int i = 0; i < ObservedMacList.size(); i++) {
            if (dh.getHash1().get(ObservedMacList.get(i)) == null) {
                ObservedMacList.remove(i);
                i--;
            }
        }

        return ObservedMacList;
    }

    public String select_alg(String selection, DataHandle dh, DataObserved datao){

        Algorithms alg = new Algorithms();
        String myloc = "";

        switch(selection){
            case "mmse":
                myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), parameter);
                break;

            case "trilateration":
                //dummy
                myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), parameter);

                break;
        }
        return myloc;
    }

    public DataObserved build_ObservedRSS(DataObserved datao, ArrayList<String> ObservedMacList){
        for(int i= 0; i<ObservedMacList.size(); i++) {
            datao.setHash_observed(ObservedMacList.get(i), Integer.toString(obsmacrss.get(ObservedMacList.get(i))));
        }

        return datao;
    }

    public double calcParameter(){
        ArrayList<String> TestPositions = _Databases.getTestPositions(Tracker.this, DBNAME);

        ArrayList<Integer> ErrorList_mmse = new ArrayList<>();

        int bestParameter_mmse = 1;

        HashMap<String, Integer> Parameters_mmse = new HashMap<>();

        for(int i = 0; i<TestPositions.size(); i++){ //7 is the number of tps
            DataObserved datao = new DataObserved();
            Algorithms alg = new Algorithms();

            //big numbers to not disturb the results
            double error_mmse = 1000;

            String positions[] = TestPositions.get(i).split("&");
            datao.equalObservedMacList(_Databases.build_ObservedMacList_tp(Tracker.this, DBNAME, dh, datao.getObservedMacList()));
            datao = _Databases.build_ObservedRSS_tp(Tracker.this, DBNAME, datao, positions[0], positions[1], datao.getObservedMacList());

            for (int j=1; j<=15; j++) {
                String myloc = alg.map_mmse(true, dh, datao, datao.getObservedMacList(), j);
                double v1 = alg.calcErrors(myloc, positions[0], positions[1]);
                if(v1 < error_mmse){
                    bestParameter_mmse = j;
                    error_mmse = v1;
                }
            }

            ErrorList_mmse.add(i, bestParameter_mmse);
            Parameters_mmse.put(TestPositions.get(i), bestParameter_mmse);
        }

        Operations _Operations = new Operations();
        parameter_calculated = true;
        return _Operations.calculateAverage(ErrorList_mmse);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //TODO: switch this id
        if (id == 16908332) {
            Intent intent = new Intent(Tracker.this, MainActivity.class);
            this.finish();
            Tracker.this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public class SimpleWifiReceiver extends WifiReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            try {
                wifiList = wifi.getScanResults();

                wifi_ready = true;
            } catch (RuntimeException e) {
                return;
            }
        }
    }
}
