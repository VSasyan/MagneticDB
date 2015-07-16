package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.uottawa.sasyan.magneticdb.Class.GPS;
import com.uottawa.sasyan.magneticdb.Class.Interpolation;
import com.uottawa.sasyan.magneticdb.Class.Settings;
import com.uottawa.sasyan.magneticdb.Class.Vector;
import com.uottawa.sasyan.magneticdb.Helper.HelperMeasurement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HeatActivity extends Activity {
    // Dimension to show type - 0 : norm, 1 : x, 2 : y, 3 : z
    // Interpolation type -  -1 : raw, 0 : nn, 1 : lin, 2 : inv, 3 : spl
    // Normalization type - 0 : not, 1 : lin
    GoogleMap map;
    HelperMeasurement helper;
    String dataBase;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    Interpolation interpolation;
    Settings s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat);

        s = new Settings(this);

        // Load the maps:
        if (initializeMap()) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }

        // Get JSON of the DB :
        helper = new HelperMeasurement(this);
        //this.dataBase = getResources().getString(R.string.testJSON_1);
        this.dataBase = helper.getAllJSON();

        this.typeData();
        this.showData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_heat, menu);
        switch (this.s.getShowType()) {
            case 1:
                menu.findItem(R.id.menu_x).setVisible(false);
                menu.findItem(R.id.menu_show).setIcon(R.drawable.ic_x).setTitle(R.string.menu_x);
                break;
            case 2:
                menu.findItem(R.id.menu_y).setVisible(false);
                menu.findItem(R.id.menu_show).setIcon(R.drawable.ic_y).setTitle(R.string.menu_y);
                break;
            case 3:
                menu.findItem(R.id.menu_z).setVisible(false);
                menu.findItem(R.id.menu_show).setIcon(R.drawable.ic_z).setTitle(R.string.menu_z);
                break;
            default:
                menu.findItem(R.id.menu_norm).setVisible(false);
                menu.findItem(R.id.menu_show).setIcon(R.drawable.ic_norm).setTitle(R.string.menu_norm);
                break;
        }
        menu.findItem(R.id.menu_spl).setVisible(false);
        switch (this.s.getInterType()) {
            case -1:
                menu.findItem(R.id.menu_raw).setVisible(false);
                menu.findItem(R.id.menu_interpolation).setTitle(R.string.inter_raw);
                menu.findItem(R.id.menu_interpolation).setIcon(R.drawable.ic_raw);
                break;
            case 1:
                menu.findItem(R.id.menu_lin).setVisible(false);
                menu.findItem(R.id.menu_interpolation).setTitle(R.string.inter_lin);
                menu.findItem(R.id.menu_interpolation).setIcon(R.drawable.ic_lin);
                break;
            case 2:
                menu.findItem(R.id.menu_inv).setVisible(false);
                menu.findItem(R.id.menu_interpolation).setTitle(R.string.inter_inv);
                menu.findItem(R.id.menu_interpolation).setIcon(R.drawable.ic_inv);
                break;
            case 3:
                menu.findItem(R.id.menu_spl).setVisible(false);
                menu.findItem(R.id.menu_interpolation).setTitle(R.string.inter_spl);
                menu.findItem(R.id.menu_interpolation).setIcon(R.drawable.ic_spl);
                break;
            default:
                menu.findItem(R.id.menu_nn).setVisible(false);
                menu.findItem(R.id.menu_interpolation).setTitle(R.string.inter_nn);
                menu.findItem(R.id.menu_interpolation).setIcon(R.drawable.ic_nn);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.menu_center:
                this.map.moveCamera(CameraUpdateFactory.newLatLngBounds(interpolation.getBounds(), 1));
                break;

            case R.id.menu_bounds:
                this.interpolation.fitToMap(this.map);
                this.showData();
                break;

            case R.id.menu_norm:
                this.s.setShowType(0);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_x:
                this.s.setShowType(1);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_y:
                this.s.setShowType(2);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_z:
                this.s.setShowType(3);
                this.showData();
                invalidateOptionsMenu();
                break;

            case R.id.menu_raw:
                this.s.setInterType(-1);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_nn:
                this.s.setInterType(0);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_lin:
                this.s.setInterType(1);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_inv:
                this.s.setInterType(2);
                this.showData();
                invalidateOptionsMenu();
                break;
            case R.id.menu_spl:
                this.s.setInterType(3);
                this.showData();
                invalidateOptionsMenu();
                break;

            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /********************************************************************/
    /*** Interpolation's Functions **************************************/
    /********************************************************************/
    private void typeData() {
        // Get the dataList (selon le showType):
        List<Vector> list = this.getXYZ();
        // Send it to the interpolation class:
        if (this.interpolation == null) {
            this.interpolation = new Interpolation(list);
        } else {
            this.interpolation.setList(list);
        }
    }

    private void showData() {
        if (this.mOverlay != null) {this.mOverlay.remove();}
        // Interpolation:
        this.interpolation.setType(this.s.getInterType());
        if (this.interpolation.interpolate(3)) {
            // Show on the map:
            mProvider = new HeatmapTileProvider.Builder().weightedData(this.interpolation.getDataPoint(s.getNormType())).build();
            //mProvider.setRadius(42);
            mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    private List<Vector> getXYZ() {
        Vector mf;
        GPS gps;
        List<Vector> list = new ArrayList<Vector>();

        // Transform the JSON of the DB in a [lat, lon, C] (where C is the norm, x, y or z of the magnetic field)
        try {
            JSONArray tab = new JSONArray(dataBase);
            for (int i = 0; i < tab.length(); i++) {
                // Get the point data in JSON:
                JSONObject ob = new JSONObject(tab.getString(i));
                mf = new Vector(ob.getString("absMagneticField"));
                gps = new GPS(ob.getString("gps"));

                // We had the point :
                switch (this.s.getShowType()) {
                    case 1: // x
                        list.add(new Vector(gps.getLat(), gps.getLon(), mf.getX()));
                        break;
                    case 2: // y
                        list.add(new Vector(gps.getLat(), gps.getLon(), mf.getY()));
                        break;
                    case 3: // z
                        list.add(new Vector(gps.getLat(), gps.getLon(), mf.getZ()));
                        break;
                    default: // norm
                        list.add(new Vector(gps.getLat(), gps.getLon(), mf.getNorme()));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Vector> setXYZ() {
        List<Vector> list = new ArrayList<Vector>();

        list = new ArrayList<Vector>();
        list.add(new Vector(44.42, -75.68, 15000));
        list.add(new Vector(44.52, -75.68, 12000));
        list.add(new Vector(44.62, -75.68, 10000));
        list.add(new Vector(44.82, -75.68, 8800));
        list.add(new Vector(44.92, -75.68, 6400));
        list.add(new Vector(45.12, -75.18, 30000));
        list.add(new Vector(45.25, -75.68, 6500));
        list.add(new Vector(45.42, -75.68, 5000));

        list.add(new Vector(45.42, -74.68, 7000));
        list.add(new Vector(44.42, -74.68, 3000));
        return list;
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
}
