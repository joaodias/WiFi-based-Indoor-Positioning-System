package joaodias.thesis_02;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import joaodias.thesis_02.Adapters.DBListAdapter;

public class SelectDB extends AppCompatActivity {

    // ListView widget to present the 
    ListView listView ;

    // ArrayList with the names of the existent databses
    ArrayList<String> Dbs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_db);

        Intent i = getIntent();
        Dbs = i.getStringArrayListExtra("Databases");

        listView = (ListView) findViewById(R.id.listView);

        // Adapter for the ListView widget
        DBListAdapter dbadapter = new DBListAdapter(getApplicationContext(), Dbs);
        listView.setAdapter(dbadapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Get the name of the database and launch the Tracker activity
                // passing the selected database
                String value = (String) listView.getItemAtPosition(i);
                Intent myIntent = new Intent(getApplicationContext(), Tracker.class);
                myIntent.putExtra("DatabaseName", value);
                startActivity(myIntent);
                finish();
            }
        });
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_db, menu);
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
            Intent intent = new Intent(SelectDB.this, MainActivity.class);
            this.finish();
            SelectDB.this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
