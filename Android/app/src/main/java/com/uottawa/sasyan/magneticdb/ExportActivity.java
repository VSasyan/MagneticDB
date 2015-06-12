package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.uottawa.sasyan.magneticdb.Class.CustomProcessDialog;
import com.uottawa.sasyan.magneticdb.Class.DirectoryChooserDialog;
import com.uottawa.sasyan.magneticdb.Class.Settings;


public class ExportActivity extends Activity {
    String data;
    Settings settings;
    File sessionDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        settings = new Settings(this);

        // Recuperation of position:
        Intent intent = getIntent();
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("extraJSON"));
            data = json.getString("post");
        } catch (org.json.JSONException e) {
            Toast.makeText(this, String.format(getString(R.string.impossible_getJSON), e.toString()), Toast.LENGTH_SHORT).show();
        }

        // Show exportation parameters:
        ((TextView)findViewById(R.id.title_exportationFolder)).setText(settings.getFolderSession());
        ((TextView)findViewById(R.id.title_fileName)).setText("measurements.json");

        // Create the folder if nots exists:
        sessionDir = Environment.getExternalStoragePublicDirectory(settings.getFolderSession());
        if (!sessionDir.exists() && !sessionDir.mkdirs()) {
            Toast.makeText(this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            return;
        }

        ((Button)findViewById(R.id.exportData)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
            }
        });

        ((Button)findViewById(R.id.sendDataMail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataMail();
            }
        });

    }

    public void exportData() {
        // 1 - File path:
        String filePath = sessionDir.getPath() + File.separator + "measurements.json";

        // 2 - Save file:
        boolean error = false;
        try {
            FileOutputStream output = new FileOutputStream(filePath, false);
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

    public void sendDataMail() {
        // Get all the files to send:
        ArrayList list = listAllFile(sessionDir);

        // Send them :
        Intent emailIntent = createEmailIntent("", settings.getSession() + " Export", "", list);
        startActivity(Intent.createChooser(emailIntent, "Send a Message:"));
    }

    private ArrayList listAllFile(File folder) {
        ArrayList list = new ArrayList();

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File f : files) {
                ArrayList l = listAllFile(f);
                list.addAll(l);
            }
        } else {
            list.add(Uri.fromFile(folder));
        }
        return list;
    }

    public static Intent createEmailIntent(String email, String subject, String content, ArrayList uris) {
        Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mailIntent.setType("text/plain");
        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mailIntent.putExtra(Intent.EXTRA_TEXT, content);
        if(uris != null) {
            mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        return mailIntent;
    }

}
