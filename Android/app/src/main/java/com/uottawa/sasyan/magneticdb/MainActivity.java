/**
 * Created by valentin on 5/26/2015.
 */

package com.uottawa.sasyan.magneticdb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.WorkSource;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.uottawa.sasyan.magneticdb.Class.ConvertInt;
import com.uottawa.sasyan.magneticdb.Class.DirectoryChooserDialog;
import com.uottawa.sasyan.magneticdb.Class.Measurement;
import com.uottawa.sasyan.magneticdb.Class.GPS;
import com.uottawa.sasyan.magneticdb.Class.Settings;
import com.uottawa.sasyan.magneticdb.Class.Vector;
import com.uottawa.sasyan.magneticdb.Helper.HelperMeasurement;

import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class MainActivity extends FragmentActivity implements SensorEventListener, LocationListener {
    // Elements :
    TextView mf_x, mf_y, mf_z, amf_x, amf_y, amf_z, la_x, la_y, la_z, ala_x, ala_y, ala_z,
            ac_x, ac_y, ac_z, gps_lat, gps_lon, gps_alt, gps_acc;
    Spinner spinner;
    ListView list;
    GoogleMap map;
    EditText id;

    // Utilitaires :
    Timer spotTimer;
    long lastSave = 0;
    Settings settings;
    Cursor model;
    SensorManager sensorManager;
    Sensor sensorMagneticField, sensorGravity, sensorLinearAcce;
    float[] float_magneticField = null, float_gravity = null, float_linearAcce = null;
    LocationManager lm;
    PowerManager.WakeLock wakeLock;
    MeasurementAdapter adapter = null;
    HelperMeasurement helper;
    Vector absMagneticField = new Vector(0,0,0), relMagneticField = new Vector(0,0,0),
            relGravity = new Vector(0,0,0), relLinearAcce = new Vector(0,0,0),
            absLinearAcce = new Vector(0,0,0);
    List<Vector> listAbsLinearAcce = new ArrayList<Vector>();
    GPS gps = new GPS(0,0,0,0);

    /********************************************************************/
    /*** Contro / Destro ************************************************/
    /********************************************************************/
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);

        helper = new HelperMeasurement(this);

        // Set sensor :
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorLinearAcce = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // Set GPS (or Wifi if there is not GPS) :
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // Recuperation of UI TextView :
        mf_x = (TextView)findViewById(R.id.mf_x);
        mf_y = (TextView)findViewById(R.id.mf_y);
        mf_z = (TextView)findViewById(R.id.mf_z);
        amf_x = (TextView)findViewById(R.id.amf_x);
        amf_y = (TextView)findViewById(R.id.amf_y);
        amf_z = (TextView)findViewById(R.id.amf_z);
        la_x = (TextView)findViewById(R.id.la_x);
        la_y = (TextView)findViewById(R.id.la_y);
        la_z = (TextView)findViewById(R.id.la_z);
        ala_x = (TextView)findViewById(R.id.ala_x);
        ala_y = (TextView)findViewById(R.id.ala_y);
        ala_z = (TextView)findViewById(R.id.ala_z);
        ac_x = (TextView)findViewById(R.id.ac_x);
        ac_y = (TextView)findViewById(R.id.ac_y);
        ac_z = (TextView)findViewById(R.id.ac_z);
        gps_lat = (TextView)findViewById(R.id.gps_lat);
        gps_lon = (TextView)findViewById(R.id.gps_lon);
        gps_alt = (TextView)findViewById(R.id.gps_alt);
        gps_acc = (TextView)findViewById(R.id.gps_acc);
        list = (ListView)findViewById(R.id.listCompassGPS);
        id = (EditText) findViewById(R.id.text_id);

        // Add link between adapter and the list:
        model = helper.getAll();
        startManagingCursor(model);
        adapter = new MeasurementAdapter(model);
        list.setAdapter(adapter);

        // Populate the spinner:
        spinner = (Spinner)findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.type_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Load the maps:
        if (initializeMap()) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Add click function:
        ((Button)findViewById(R.id.button_plus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.getText().toString().equals("")) {
                    id.setText("1");
                } else {
                    id.setText(String.valueOf(1 + Integer.parseInt(id.getText().toString())));
                }
            }
        });

        // Initialize timer :
        spotTimer = new Timer();

        majAffichage("");
    }

    @Override
    protected void onPause() {
        if (this.settings.getRecording().equals("false")) {
            // Unlistener sensor :
            sensorManager.unregisterListener(this, sensorMagneticField);
            sensorManager.unregisterListener(this, sensorLinearAcce);
            sensorManager.unregisterListener(this, sensorGravity);

            //UnUpdate GPS ;
            lm.removeUpdates(this);

            // Application ;ust be still worcking with the locked screen:
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            PowerManager mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"WakeLockMagneticDB");
            wakeLock.acquire();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        // Release the wakeLock:
        if (wakeLock != null) {wakeLock.release();}

        // Listener sensor :
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorLinearAcce, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_GAME);
        // SENSOR_DELAY_FASTEST,  SENSOR_DELAY_GAME, SENSOR_DELAY_UI, SENSOR_DELAY_NORMAL

        // Update GPS (or Wifi if there is not GPS) :
        if (this.settings.isWifiOnly()) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, settings.getTimeGPS(), 0, this);
        } else {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, settings.getTimeGPS(), 0, this);
        }

        initializeMap();

        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Unlistener sensor :
        sensorManager.unregisterListener(this, sensorMagneticField);
        sensorManager.unregisterListener(this, sensorLinearAcce);
        sensorManager.unregisterListener(this, sensorGravity);

        //UnUpdate GPS ;
        lm.removeUpdates(this);

        helper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (this.settings.getRecording().equals("false")) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_record).setIcon(R.drawable.ic_stop);
        } else if (this.settings.getRecording().equals("true")) {
            menu.findItem(R.id.menu_play).setVisible(false);
            menu.findItem(R.id.menu_record).setIcon(R.drawable.ic_play);
        } else { // "all"
            menu.findItem(R.id.menu_spot).setVisible(false);
            menu.findItem(R.id.menu_record).setIcon(R.drawable.ic_spot);
        }
        menu.findItem(R.id.menu_gps).setVisible(!this.settings.isWifiOnly());
        menu.findItem(R.id.menu_wifi).setVisible(this.settings.isWifiOnly());
        if (this.settings.getShow().equals("maps")) {
            menu.findItem(R.id.menu_maps).setVisible(false);
            menu.findItem(R.id.menu_view).setIcon(R.drawable.ic_maps).setTitle(R.string.menu_maps);
        } else if (this.settings.getShow().equals("list")) {
            menu.findItem(R.id.menu_list).setVisible(false);
            menu.findItem(R.id.menu_view).setIcon(R.drawable.ic_list).setTitle(R.string.menu_list);
        } else { // "all"
            menu.findItem(R.id.menu_all).setVisible(false);
            menu.findItem(R.id.menu_view).setIcon(R.drawable.ic_all).setTitle(R.string.menu_all);
        }
        //menu.findItem(R.id.menu_del).setVisible(this.settings.getShow().equals("list"));
        //menu.findItem(R.id.menu_export).setVisible(this.settings.getShow().equals("list"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        JSONObject obj;
        Intent intent;

        switch (item.getItemId()) {

            case R.id.menu_stop:
                this.settings.setRecording("false");
                invalidateOptionsMenu();
                Toast.makeText(this, R.string.recordingStop, Toast.LENGTH_SHORT).show();
                unsetSpot();
                return true;

            case R.id.menu_play:
                this.settings.setRecording("true");
                invalidateOptionsMenu();
                Toast.makeText(this, R.string.recordingPlay, Toast.LENGTH_SHORT).show();
                unsetSpot();
                return true;

            case R.id.menu_spot:
                this.settings.setRecording("forced");
                invalidateOptionsMenu();
                Toast.makeText(this, R.string.recordingForced, Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_gps:
                this.settings.setWifiOnly(true);
                invalidateOptionsMenu();
                Toast.makeText(this, R.string.use_wifiOnly, Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_wifi:
                this.settings.setWifiOnly(false);
                invalidateOptionsMenu();
                Toast.makeText(this, R.string.use_gps, Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_picture:
                // Create a JSON containing lat and lon :
                obj = new JSONObject();
                try {
                    JSONObject lonLat = new JSONObject();
                    lonLat.put("lat", gps.getLat());
                    lonLat.put("lon", gps.getLon());
                    obj.put("latLon", lonLat);
                } catch (org.json.JSONException e) {
                    Toast.makeText(this, String.format(getString(R.string.impossible_setLatlon), e.toString()), Toast.LENGTH_SHORT).show();
                }

                // Show the picture activity :
                intent = new Intent(MainActivity.this, PictureActivity.class);
                intent.putExtra("extraJSON", obj.toString());
                startActivity(intent);
                return true;

            case R.id.menu_all:
                majAffichage("all");
                return true;

            case R.id.menu_maps:
                majAffichage("maps");
                return true;

            case R.id.menu_list:
                majAffichage("list");
                return true;

            case R.id.menu_del:
                helper.deleteAll();
                model.requery();
                map.clear();
                return true;

            case R.id.menu_export:
                // Create a JSON containing lat and lon :
                obj = new JSONObject();
                try {
                    JSONObject post = new JSONObject();
                    obj.put("post", helper.getAllJSON());
                } catch (org.json.JSONException e) {
                    Toast.makeText(this, String.format(getString(R.string.impossible_setJSON), e.toString()), Toast.LENGTH_SHORT).show();
                }

                // Show the sync activity :
                intent = new Intent(MainActivity.this, ExportActivity.class);
                intent.putExtra("extraJSON", obj.toString());
                startActivity(intent);
                return true;

            case R.id.menu_heat:
                // Show the head activity (nothing to intent) :
                intent = new Intent(MainActivity.this, HeatActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_settings:
                settings.showSettings();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /********************************************************************/
    /*** Preferences Functions ******************************************/
    /********************************************************************/
    private void majAffichage(String str) {
        if (!str.equals("")) {
            this.settings.setShow(str);
            invalidateOptionsMenu();
        }
        // We keep All :
        findViewById(R.id.ll_haut).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_list).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_form).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_maps).setVisibility(View.VISIBLE);
        // And if needed we hide some element :
        if (this.settings.getShow().equals("maps")) {
            // We keep only the map :
            findViewById(R.id.ll_haut).setVisibility(View.GONE);
        } else if (this.settings.getShow().equals("list")) {
            // We keep only the list :
            findViewById(R.id.ll_form).setVisibility(View.GONE);
            findViewById(R.id.ll_maps).setVisibility(View.GONE);
        }
    }

    /********************************************************************/
    /*** Map's Functions ************************************************/
    /********************************************************************/
    private boolean initializeMap() {
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // check if map is created successfully or not
            if (map == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public Marker addMarker(String title, String snippet, LatLng position) {
        Marker m = map.addMarker(new MarkerOptions().position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lt_blue)));
        return m;
    }

    /********************************************************************/
    /*** SensorEventListener ********************************************/
    /********************************************************************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Gravity :
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            relGravity.update(event.values.clone());
            this.showRelGravity();
            float_gravity = event.values.clone();
        }

        // Relative magnetic field :
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            relMagneticField.update(event.values.clone(), 1000);
            this.showRelMagneticField();
            float_magneticField = event.values.clone();
        }

        // Calculate absolute magnetic field :
        if (float_magneticField != null && float_gravity != null) {
            // We have relative compass and relative accelerometer, we can calculate absolute compass :
            float[] R = new float[16];
            float[] I = new float[16];
            float[] Rinv =  new float[16];
            SensorManager.getRotationMatrix(R, I, float_gravity, float_magneticField);
            android.opengl.Matrix.invertM(Rinv, 0, R, 0);
            float[] aCompass = new float[4];
            float[] rCompass = new float[4];
            rCompass[0] = float_magneticField[0];
            rCompass[1] = float_magneticField[1];
            rCompass[2] = float_magneticField[2];
            rCompass[3] = 0;
            android.opengl.Matrix.multiplyMV(aCompass, 0, Rinv, 0, rCompass, 0);
            absMagneticField.update(aCompass, 1000);
            this.showAbsMagneticField();
        }

        // Relative linear acceleration :
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            relLinearAcce.update(event.values.clone());
            this.showRelLinearAcce();
            float_linearAcce = event.values.clone();
        }

        // Calculate absolute linear acceleration :
        if (float_linearAcce != null && float_magneticField != null && float_gravity != null) {
            // We have relative linear acceleration and relative accelerometer, we can calculate absolute linear acceleration :
            float[] R = new float[16];
            float[] I = new float[16];
            float[] Rinv =  new float[16];
            SensorManager.getRotationMatrix(R, I, float_gravity, float_magneticField);
            android.opengl.Matrix.invertM(Rinv, 0, R, 0);
            float[] aLinearAcce = new float[4];
            float[] rLinearAcce = new float[4];
            rLinearAcce[0] = float_linearAcce[0];
            rLinearAcce[1] = float_linearAcce[1];
            rLinearAcce[2] = float_linearAcce[2];
            rLinearAcce[3] = 0;
            android.opengl.Matrix.multiplyMV(aLinearAcce, 0, Rinv, 0, rLinearAcce, 0);
            //this.addAbsLinearAcce(new Vector(aLinearAcce));
            this.absLinearAcce.update(aLinearAcce);
            this.showAbsLinearAcce();
        }
    }

    /********************************************************************/
    /*** Filtering and stocking function ********************************/
    /********************************************************************/
    private void addAbsLinearAcce(Vector v) {
        this.listAbsLinearAcce.add(v);
        if (this.listAbsLinearAcce.size() > 8) {
            // we delete the first element :
            this.listAbsLinearAcce.remove(0);
        }
        // We calculate a mean :
        this.absLinearAcce.beMeanOf(this.listAbsLinearAcce);
    }

    /********************************************************************/
    /*** LocationListener ***********************************************/
    /********************************************************************/
    @Override
    public void onProviderEnabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String msg = String.format(getResources().getString(R.string.provider_disabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.gps.update(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
        this.showGPS();

        // We save the result ;
        if (!this.settings.getRecording().equals("false")) {this.saveResult(false);}
        resetSpot();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
        Toast.makeText(this, newStatus, Toast.LENGTH_SHORT).show();
    }

    /********************************************************************/
    /*** Affichage ******************************************************/
    /********************************************************************/
    public void showRelMagneticField() {
        mf_x.setText(String.format(getString(R.string.title_x), this.relMagneticField.getStringX()));
        mf_y.setText(String.format(getString(R.string.title_y), this.relMagneticField.getStringY()));
        mf_z.setText(String.format(getString(R.string.title_z), this.relMagneticField.getStringZ()));
    }

    public void showAbsMagneticField() {
        amf_x.setText(String.format(getString(R.string.title_x), this.absMagneticField.getStringX()));
        amf_y.setText(String.format(getString(R.string.title_y), this.absMagneticField.getStringY()));
        amf_z.setText(String.format(getString(R.string.title_z), this.absMagneticField.getStringZ()));
    }

    public void showRelLinearAcce() {
        la_x.setText(String.format(getString(R.string.title_x), this.relLinearAcce.getStringX()));
        la_y.setText(String.format(getString(R.string.title_y), this.relLinearAcce.getStringY()));
        la_z.setText(String.format(getString(R.string.title_z), this.relLinearAcce.getStringZ()));
    }

    public void showAbsLinearAcce() {
        ala_x.setText(String.format(getString(R.string.title_x), this.absLinearAcce.getStringX()));
        ala_y.setText(String.format(getString(R.string.title_y), this.absLinearAcce.getStringY()));
        ala_z.setText(String.format(getString(R.string.title_z), this.absLinearAcce.getStringZ()));
    }

    public void showRelGravity() {
        ac_x.setText(String.format(getString(R.string.title_x), this.relGravity.getStringX()));
        ac_y.setText(String.format(getString(R.string.title_y), this.relGravity.getStringY()));
        ac_z.setText(String.format(getString(R.string.title_z), this.relGravity.getStringZ()));
    }

    public void showGPS() {
        gps_lat.setText(String.format(getString(R.string.title_lat), this.gps.getStringLat()));
        gps_lon.setText(String.format(getString(R.string.title_lon), this.gps.getStringLon()));
        gps_alt.setText(String.format(getString(R.string.title_alt), this.gps.getStringAlt()));
        gps_acc.setText(String.format(getString(R.string.title_acc), this.gps.getStringAcc()));
    }

    /********************************************************************/
    /*** Sauvegarde ******************************************************/
    /********************************************************************/
    public void saveResult(boolean spot) {
        if (!settings.getRecording().equals("false")) {
            // Check if we cam spot if necessary:
            long temp = System.currentTimeMillis();
            if (spot & (temp - lastSave < settings.getTimeSpot())) {
                return;
            }
            lastSave = temp;

            // Then save data:
            ConvertInt ID = new ConvertInt(id.getText().toString(), 0);
            Measurement result = new Measurement(
                    ID.getValue(),
                    spinner.getSelectedItem().toString(),
                    this.gps,
                    this.absMagneticField,
                    this.relMagneticField,
                    this.relGravity,
                    this.absLinearAcce,
                    this.relLinearAcce
            );
            if (spot) {
                Toast.makeText(this, settings.getRecording()+" Spot: " + result.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
            }
            helper.insert(result);
            model.requery();
        } else if (spot) {
            unsetSpot();
        }
    }

    /********************************************************************/
    /*** Adapter for the CompassGPS *************************************/
    /********************************************************************/
    class MeasurementAdapter extends CursorAdapter {

        MeasurementAdapter(Cursor c) {
            super(MainActivity.this, c);
        }

        @Override
        public void bindView(View row, Context ctxt, Cursor c) {
            MeasurementHolder holder = (MeasurementHolder)row.getTag();
            holder.populateFrom(c, helper);
        }

        @Override
        public View newView(Context ctxt, Cursor c, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.fragment_list_row, parent, false);
            MeasurementHolder holder = new MeasurementHolder(row);
            row.setTag(holder);
            return(row);
        }
    }

    /********************************************************************/
    /*** Holder for the CompassGPS **************************************/
    /********************************************************************/
    class MeasurementHolder {
        // Ajout dans la liste :
        private TextView abs_mf = null;
        private TextView gps = null;

        MeasurementHolder(View row) {
            abs_mf = (TextView)row.findViewById(R.id.abs_mf);
            gps = (TextView)row.findViewById(R.id.gps);
        }

        void populateFrom(Cursor c, HelperMeasurement helper) {
            Vector absMF = helper.getAbsMagneticField(c);
            GPS g = helper.getGPS(c);
            abs_mf.setText(absMF.toString());
            gps.setText(g.toString());

            addMarker(absMF.toString(), "", g.toLatLng());
        }

    }

    /********************************************************************/
    /*** Spot's function ************************************************/
    /********************************************************************/
    private void setSpot() {
        TimerTask spotTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveResult(true);
                    }
                });
            }
        };
        spotTimer.schedule(spotTask, settings.getTimeSpot(), settings.getTimeSpot());
    }

    private void unsetSpot() {
        if (spotTimer != null) {spotTimer.purge();}
    }

    private void resetSpot() {
        unsetSpot();
        if (settings.getRecording().equals("forced")) {setSpot();}
    }

}
