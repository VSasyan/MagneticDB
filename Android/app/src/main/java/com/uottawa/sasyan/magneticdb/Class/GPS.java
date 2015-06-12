/**
 * Created by valentin on 5/26/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class GPS {
    double lat, lon, alt, acc;

    public GPS(double lat, double lon, double alt, double acc) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.acc = acc;
    }

    public GPS(String str) {
        this.update(str);
    }

    public void update(double lat, double lon, double alt, double acc) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.acc = acc;
    }

    public void update(String str) {
        try {
            JSONObject vect = new JSONObject(str);
            this.update(vect.getDouble("lat"), vect.getDouble("lon"), vect.getDouble("alt"), vect.getDouble("acc"));
        } catch (org.json.JSONException e) {
            // TODO : handle exeption
            Log.e("GPS.updateFromJSON", e.toString());
        }
    }

    public String toString() {
        return "[" + this.getStringLat() + "," + this.getStringLon() + "," + this.getStringAlt() + "," + this.getStringAcc() + "]";
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("lat", this.getLat());
            obj.put("lon", this.getLon());
            obj.put("alt", this.getAlt());
            obj.put("acc", this.getAcc());
        } catch (org.json.JSONException e) {
            // TODO : handle exception
            Log.e("GPS.toJSON", e.toString());
        }
        return obj;
    }

    public LatLng toLatLng() {
        return new LatLng(this.getLat(), this.getLon());
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    public double getAcc() {
        return acc;
    }

    public String getStringLat() {
        return String.format("%.2f", this.getLat());
    }

    public String getStringLon() {
        return String.format("%.2f", this.getLon());
    }

    public String getStringAlt() {
        return String.format("%.2f", this.getAlt());
    }

    public String getStringAcc() {
        return String.format("%.2f", this.getAcc());
    }

    public String getLatTagGPS() {
        double latitude = Math.abs(this.getLat());
        int num1Lat = (int)Math.floor(latitude);
        int num2Lat = (int)Math.floor((latitude - num1Lat) * 60);
        double num3Lat = (latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;
        return num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000";
    }

    public String getLonTagGPS() {
        double longitude = Math.abs(this.getLon());
        int num1Lon = (int)Math.floor(longitude);
        int num2Lon = (int)Math.floor((longitude - num1Lon) * 60);
        double num3Lon = (longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;
        return num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000";
    }

    public String getLatRefTagGps() {
        if (this.getLat() > 0) {return "N";} else {return "S";}
    }

    public String getLonRefTagGps() {
        if (this.getLon() > 0) {return "E";} else {return "W";}
    }

}
