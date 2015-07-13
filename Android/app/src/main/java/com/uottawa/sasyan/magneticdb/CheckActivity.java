package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class CheckActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ListView listSensors = (ListView)findViewById(R.id.listSensors);

        // Check the sensor :
        String[] sensor_names = getResources().getStringArray(R.array.sensor_name_array);
        int[] sensor_types = getResources().getIntArray(R.array.sensor_type_array);
        ArrayList<String> sensors_checked = new ArrayList<>();
        ArrayAdapter<String> adapter;
        boolean ok = true;

        for (int i = 0; i < sensor_types.length; i++) {
            if (mSensorManager.getDefaultSensor(sensor_types[i]) != null) {
                sensors_checked.add(sensor_names[i] + getString(R.string.colon) + getString(R.string.yes));
            } else {
                sensors_checked.add(sensor_names[i] + getString(R.string.colon) + getString(R.string.no));
                ok = false;
            }
        }

        // Show result:
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensors_checked);
        listSensors.setAdapter(adapter);
        TextView checked = (TextView) findViewById(R.id.checked);
        if (ok) {
            checked.setText(getString(R.string.done_checked));
            checked.setTextColor(Color.GREEN);
        } else {
            checked.setText(getString(R.string.error_checked));
            checked.setTextColor(Color.RED);
        }

        // Add button function:
                ((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
