package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.uottawa.sasyan.magneticdb.Class.CustomProcessDialog;
import com.uottawa.sasyan.magneticdb.Class.DirectoryChooserDialog;
import com.uottawa.sasyan.magneticdb.Class.Settings;


public class ExportActivity extends Activity {
    String data;
    Button exportData, pickFolder;
    Settings settings;
    EditText folderJsonET, fileNameET, dateFormatET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        settings = new Settings(this);

        // Get elements:
        folderJsonET = (EditText)findViewById(R.id.text_folderJSON);
        fileNameET = (EditText)findViewById(R.id.text_name);
        dateFormatET = (EditText)findViewById(R.id.text_date);
        folderJsonET.setText(settings.getFolderJSON());
        fileNameET.setText(settings.getFileName());
        dateFormatET.setText(settings.getDateFormat());

        // Recuperation of position:
        Intent intent = getIntent();
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("extraJSON"));
            data = json.getString("post");
        } catch (org.json.JSONException e) {
            Toast.makeText(this, String.format(getString(R.string.impossible_getJSON), e.toString()), Toast.LENGTH_SHORT).show();
        }

        exportData = (Button)findViewById(R.id.exportData);
        exportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ExportData :

                // 1 - File name:
                settings.setFolderJSON(folderJsonET.getText().toString());
                settings.setFileName(fileNameET.getText().toString());
                settings.setDateFormat(dateFormatET.getText().toString());
                settings.saveSettings();

                String date = ".json";
                if (!settings.getDateFormat().equals("")) {
                    SimpleDateFormat format = new SimpleDateFormat(settings.getDateFormat());
                    date = "_" + format.format(new Date()) + ".json";
                }
                String fileSrc = settings.getFolderJSON() + "/" + settings.getFileName() + date;

                // 2 - Save file:
                boolean error = false;
                try {
                    FileOutputStream output = new FileOutputStream(fileSrc, false);
                    output.write(data.getBytes());
                } catch (java.io.IOException e) {
                    Log.e("File not saved", "Error: " + e.toString());
                    error = true;
                }

                // 3 - OK ?
                if (error) {
                    Toast.makeText(getApplicationContext(), R.string.error_dataNotExported, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.done_dataExported, Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickFolder = (Button)findViewById(R.id.pickFolder);
        pickFolder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DirectoryChooserDialog DCD = new DirectoryChooserDialog(ExportActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        settings.setFolderJSON(chosenDir);
                        folderJsonET.setText(settings.getFolderJSON());
                    }
                });
                DCD.setNewFolderEnabled(true);
                if (settings.getFolderJSON() != null) {DCD.chooseDirectory(settings.getFolderJSON());} else {DCD.chooseDirectory();}
            }
        });

    }

}
