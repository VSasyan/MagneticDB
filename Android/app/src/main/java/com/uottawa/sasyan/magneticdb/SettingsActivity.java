package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.uottawa.sasyan.magneticdb.Class.DirectoryChooserDialog;
import com.uottawa.sasyan.magneticdb.Class.Settings;


public class SettingsActivity extends Activity {
    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        findViewById(R.id.pickFolderPicture).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DirectoryChooserDialog DCD = new DirectoryChooserDialog(SettingsActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        ((EditText)findViewById(R.id.text_folderPicture)).setText(chosenDir);
                    }
                });
                DCD.setNewFolderEnabled(true);
                String temp = ((EditText)findViewById(R.id.text_folderPicture)).getText().toString();
                if (temp != null) {
                    DCD.chooseDirectory(temp);
                } else {
                    DCD.chooseDirectory();
                }
            }
        });

        findViewById(R.id.pickFolderJSON).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DirectoryChooserDialog DCD = new DirectoryChooserDialog(SettingsActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        ((EditText)findViewById(R.id.text_folderJSON)).setText(chosenDir);
                    }
                });
                DCD.setNewFolderEnabled(true);
                String temp = ((EditText)findViewById(R.id.text_folderJSON)).getText().toString();
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
        ((EditText)findViewById(R.id.text_folderPicture)).setText(settings.getFolderPicture());
        ((EditText)findViewById(R.id.text_date)).setText(settings.getDateFormat());
        ((EditText)findViewById(R.id.text_folderJSON)).setText(settings.getFolderJSON());
        ((EditText)findViewById(R.id.text_name)).setText(settings.getFileName());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_save) {
            settings.setFolderPicture(((EditText) findViewById(R.id.text_folderPicture)).getText().toString());
            settings.setDateFormat(((EditText) findViewById(R.id.text_date)).getText().toString());
            settings.setFolderJSON(((EditText) findViewById(R.id.text_folderJSON)).getText().toString());
            settings.setFileName(((EditText) findViewById(R.id.text_name)).getText().toString());
            this.finish();
            return true;
        }

        if (id == R.id.menu_back) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
