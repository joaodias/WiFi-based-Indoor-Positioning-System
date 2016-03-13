package joaodias.thesis_02;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import joaodias.thesis_02.Structures.MagneticReading;
import joaodias.thesis_02.Structures.Map;
import joaodias.thesis_02.Structures.Position;
import joaodias.thesis_02.Structures.WifiMean;
import joaodias.thesis_02.Structures.WifiReading;
import joaodias.thesis_02.Utils.Databases;
import joaodias.thesis_02.Utils.Operations;
import joaodias.thesis_02.Views.ZoomImageView;
import joaodias.thesis_02.Wifi.SimpleWifiManager;
import joaodias.thesis_02.Wifi.WifiReceiver;

public class Logger extends AppCompatActivity implements SensorEventListener {

    private String CREATE_TABLE_MAGNETIC = "CREATE TABLE IF NOT EXISTS MAGNETIC (snumber VARCHAR, pos_x VARCHAR, pos_y VARCHAR, absolute VARCHAR, azimuth VARCHAR, pitch VARCHAR, roll VARCHAR)";
    private String CREATE_TABLE_WIFI = "CREATE TABLE IF NOT EXISTS WIFI (snumber VARCHAR, pos_x VARCHAR, pos_y VARCHAR, timestamp VARCHAR, mac_address VARCHAR, rssi VARCHAR)";
    private String CREATE_TABLE_WIFI_MEAN = "CREATE TABLE IF NOT EXISTS WIFI_MEAN (pos_x VARCHAR, pos_y VARCHAR, rssi VARCHAR, deviation VARCHAR, mac_address VARCHAR)";
    private String CREATE_TABLE_IMAGE = "CREATE TABLE IF NOT EXISTS MAP (image BLOB, orientation VARCHAR)";
    private String CREATE_TABLE_TP = "CREATE TABLE IF NOT EXISTS TEST_DATA (snumber VARCHAR, pos_x VARCHAR, pos_y VARCHAR, timestamp VARCHAR, mac_address VARCHAR, rssi VARCHAR)";
    private String CREATE_TABLE_TP_MEAN = "CREATE TABLE IF NOT EXISTS TEST_DATA_MEAN (pos_x VARCHAR, pos_y VARCHAR, rssi VARCHAR, deviation VARCHAR, mac_address VARCHAR)";
    private String DBNAME = "";

    private String TABLE = "wifi";
    private String TABLE_MEAN = "wifi_mean";

    private ArrayList<String> queries = new ArrayList<>();

    private static int RESULT_LOAD_IMAGE = 1;

    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private Sensor mAccelerometer;

    private SimpleWifiManager wifi;
    private WifiReceiver receiverWifi;

    //database object
    Databases _Databases = new Databases();

    //Wifi_mean object
    WifiMean _WifiMean = new WifiMean();

    //list to store wifi stuff
    List<ScanResult> wifiList;

    //vars necessary to get timestamps
    Date date = new Date();
    double timestamp = 0;

    //number of samples per position
    int count = 10;

    //check if it is in add mode
    boolean isClicked = false;

    //true if collected test points
    boolean test_points = false;

    boolean finished = false;

    boolean listening_to_magnetic = false;

    //Point that stores the actual position read from the touch input
    Position _Position;

    //UI Elements
    ZoomImageView zoomImageView;
    Button addButton;
    Button deleteButton;
    Button finish;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private int mInterval = 1000;

    private Handler mHandler;

    private float magnetic_x;
    private float magnetic_y;
    private float magnetic_z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);

        zoomImageView = (ZoomImageView) findViewById(R.id.zoomimageView);
        addButton = (Button) findViewById(R.id.button3);
        deleteButton = (Button) findViewById(R.id.button4);
        finish = (Button) findViewById(R.id.button5);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        addButton.setEnabled(false);
        deleteButton.setEnabled(false);
        finish.setEnabled(false);

        initImage();
        setupWifi();
        finish.setText("Next Step");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DBNAME.equals("")) {
                    PopDialog();
                } else {
                    isClicked = true;
                    Map _map = Map.getInstance();

                    if (_map.isPoint()) {
                        //Toast.makeText(getApplicationContext(), _map.getTouched_height() + "," + _map.getTouched_width(), Toast.LENGTH_SHORT).show();
                        _Position = new Position(_map.getTouched_width(), _map.getTouched_height());
                        Toast.makeText(getApplicationContext(), "Hold the phone while recording", Toast.LENGTH_LONG).show();
                        count = 0;
                        mHandler = new Handler();
                        startRepeatingTask();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select a position to scan in the map", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _Databases.delete_all_db(Logger.this, DBNAME);
                Map _map = Map.getInstance();
                zoomImageView.setImageBitmap(_map.getCleanMap());
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(finished){
                    PopDialogFinal();
                }
                else{
                    PopDialogTestPoints();
                    finished = true;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor == mMagnetometer){
            magnetic_x = event.values[0];
            magnetic_y = event.values[1];
            magnetic_z = event.values[2];
        }

        if(listening_to_magnetic) {
            //Orientation
            if (event.sensor == mAccelerometer) {
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
                float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

                mCurrentDegree = -azimuthInDegress;
            }

            if (mCurrentDegree < 0) {
                listening_to_magnetic = false;

                Map _map = Map.getInstance();
                _Databases.addDBMap(Logger.this, DBNAME, _map.getCleanMap(), mCurrentDegree);

                Toast.makeText(Logger.this, "" + mCurrentDegree, Toast.LENGTH_SHORT).show();

                Toast.makeText(Logger.this, "Mapping activity completed successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Logger.this, MainActivity.class);
                Logger.this.finish();
                Logger.this.startActivity(intent);
            }
        }
    }

    public void setupWifi(){
        wifi = new SimpleWifiManager(Logger.this);
        receiverWifi = new SimpleWifiReceiver();
        wifi.startScan(receiverWifi, "10");
    }

    public void initImage(){
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public ArrayList<String> buildQueries(){
        queries.add(CREATE_TABLE_MAGNETIC);
        queries.add(CREATE_TABLE_WIFI);
        queries.add(CREATE_TABLE_WIFI_MEAN);
        queries.add(CREATE_TABLE_IMAGE);
        queries.add(CREATE_TABLE_TP);
        queries.add(CREATE_TABLE_TP_MEAN);

        return queries;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Map _map = Map.getInstance();
                _map.setBaseImage(bitmap);
                _map.setCleanMap(bitmap);
                PopDialog();
                zoomImageView.setImageBitmap(_map.getBaseImage());
            }
            catch(IOException e){
                Toast.makeText(getApplicationContext(), "Image not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void PopDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a name for your map");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map _map = Map.getInstance();
                DBNAME = input.getText().toString();
                _Databases.createDB(Logger.this, DBNAME, buildQueries());
                Toast.makeText(Logger.this, "DB created", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void PopDialogTestPoints(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to scan a set of test points to later improve the location results?");

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (test_points){
                    Toast.makeText(getApplicationContext(), "Map Created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Logger.this, MainActivity.class);
                    Logger.this.finish();
                    Logger.this.startActivity(intent);
                    dialog.cancel();

                    test_points =false;
                }
                else{
                    Map _map = Map.getInstance();

                    _map.setCurImage(null);
                    _map.setBaseImage(_map.getCleanMap());
                    zoomImageView.setImageBitmap(_map.getCleanMap());

                    TABLE = "TEST_DATA";
                    TABLE_MEAN = "TEST_DATA_MEAN";

                    test_points = true;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PopDialogFinal();
            }
        });

        builder.show();
    }

    public void PopDialogFinal(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Turn to the top of your map and press READY");

        // Set up the buttons
        builder.setPositiveButton("READY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listening_to_magnetic = true;
            }
        });

        builder.show();
    }

    public class SimpleWifiReceiver extends WifiReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            try {
                wifiList = wifi.getScanResults();
                date = new Date();
                timestamp = date.getTime();

                //enable buttons
                addButton.setEnabled(true);
                deleteButton.setEnabled(true);
                finish.setEnabled(true);

            } catch (RuntimeException e) {
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //TODO: switch this id
        if (id == 16908332) {
            Intent intent = new Intent(Logger.this, MainActivity.class);
            this.finish();
            Logger.this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mStatusChecker, mInterval);
            //Check if samples less than 10
            if(count < 10){
                if(count == 0){
                    _WifiMean = new WifiMean();
                }
                MagneticReading _MagneticReading = new MagneticReading(_Position.getX(), _Position.getY(), magnetic_x, magnetic_y, magnetic_z);
                WifiReading _WifiReading = new WifiReading(_Position.getX(), _Position.getY(), timestamp, wifiList);
                _Databases.add_magnetic(Logger.this, DBNAME, _MagneticReading);
                _WifiMean = _Databases.add_wifi(Logger.this, DBNAME, TABLE, _WifiReading, _WifiMean);
                count++;
            }
            else if (count == 10){

                _Databases.addWifiMean(Logger.this, DBNAME, TABLE_MEAN, _Position.getX(), _Position.getY(), _WifiMean);

                zoomImageView.updateImageViewFix();
                count++;

                stopRepeatingTask();
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
