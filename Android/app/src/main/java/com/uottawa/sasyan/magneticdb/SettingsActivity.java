package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.uottawa.sasyan.magneticdb.Class.DirectoryChooserDialog;
import com.uottawa.sasyan.magneticdb.Class.Settings;


public class SettingsActivity extends Activity {
    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        findViewById(R.id.pickFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DirectoryChooserDialog DCD = new DirectoryChooserDialog(SettingsActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        ((EditText)findViewById(R.id.text_folder)).setText(chosenDir);
                    }
                });
                DCD.setNewFolderEnabled(true);
                String temp = ((EditText)findViewById(R.id.text_folder)).getText().toString();
                if (temp != null) {
                    DCD.chooseDirectory(temp);
                } else {
                    DCD.chooseDirectory();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        // read setting in the composent:
        ((EditText)findViewById(R.id.text_folder)).setText(settings.getFolder());
        ((EditText)findViewById(R.id.text_date)).setText(settings.getDateFormat());
        ((EditText)findViewById(R.id.text_session)).setText(settings.getSession());
        ((Switch) findViewById(R.id.spotting)).setChecked(settings.getSpotting());
        ((EditText)findViewById(R.id.text_timeGPS)).setText(String.valueOf(settings.getTimeGPS()));
        ((EditText)findViewById(R.id.text_timeSpot)).setText(String.valueOf(settings.getTimeSpot()));
        ((EditText)findViewById(R.id.text_sizeAverage)).setText(String.valueOf(settings.getSizeAverage()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_save) {
            settings.setFolder(((EditText) findViewById(R.id.text_folder)).getText().toString());
            settings.setDateFormat(((EditText) findViewById(R.id.text_date)).getText().toString());
            settings.setSession(((EditText) findViewById(R.id.text_session)).getText().toString());
            settings.setSpotting(((Switch) findViewById(R.id.spotting)).isChecked());
            settings.setTimeGPS(((EditText) findViewById(R.id.text_timeGPS)).getText().toString());
            settings.setTimeSpot(((EditText) findViewById(R.id.text_timeSpot)).getText().toString());
            settings.setSizeAverage(((EditText) findViewById(R.id.text_sizeAverage)).getText().toString());
            this.finish_();
            return true;
        }

        if (id == R.id.menu_back) {
            this.finish_();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void finish_() {
        setResult(MainActivity.REQUEST_CODE_SETTINGS);
        finish();
    }
}
