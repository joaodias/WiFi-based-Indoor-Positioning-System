package joaodias.thesis_02;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    //Button to create a new map
    Button button;

    //Button to navigate in an already created map
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);

        // Listener for the create map button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), Logger.class);
                startActivity(myIntent);
                finish();
            }
        });

        // Listener for the navigate map button
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Assign the path to the local databases folder 
                String DBPATH = "/data/data/joaodias.thesis_02/databases/";

                // Create new ArrayList to store the existent databases
                ArrayList<String> Dbs = new ArrayList<String>();

                List<File> files = getListFiles(new File(DBPATH));

                if (files.size() == 0 && files.get(0).toString().equals(null) || files.get(0).toString().equals(DBPATH + ".db3")) {
                    Toast.makeText(getApplicationContext(), "There are no available Databases. Please create a map.", Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < files.size(); i++) {
                        String[] parts;
                        String[] parts1;
                        parts = files.get(i).toString().split(DBPATH);
                        if (!parts[1].equals(".db3")) {

                            // Retrieve the name of the databases
                            parts1 = parts[1].split(".db3");
                            Dbs.add(parts1[0]);
                        }
                    }

                    // Go to the database selection activity and pass the ArrayList
                    // with the name of the databases
                    Intent myIntent = new Intent(getApplicationContext(), SelectDB.class);
                    myIntent.putStringArrayListExtra("Databases", Dbs);
                    startActivity(myIntent);
                    finish();
                }
            }
        });
    }

    /**
     * Retrieves the databases in the specified folder
     * 
     * @param parentDir [description]
     * @return a list of the existent databases
     */
    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".db3")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
