/**
 * Created by valentin on 5/26/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.util.Log;

import org.json.JSONObject;

public class Measurement {
    Vector absMagneticField, relMagneticField, relGravity, absLinearAcce, relLinearAcce;
    GPS gps;
    String type;
    int id;

    public Measurement(int id, String type, GPS gps, Vector absMagneticField, Vector relMagneticField, Vector relGravity, Vector absLinearAcce, Vector relLinearAcce) {
        this.id = id;
        this.type = type;
        this.gps = gps;
        this.absMagneticField = absMagneticField;
        this.relMagneticField = relMagneticField;
        this.relGravity = relGravity;
        this.absLinearAcce = absLinearAcce;
        this.relLinearAcce = relLinearAcce;
    }

    public String toString() {
        return "{" + String.valueOf(id) + "," + type + "," + gps.toString() + "," + absMagneticField.toString() + "," + relMagneticField.toString() + "," + relGravity.toString() +
               "," + absLinearAcce.toString() + "," + relLinearAcce.toString() + "}";
    }

    public JSONObject toJSON() {
        JSONObject mesure = new JSONObject();
        try {
            mesure.put("id", this.getId());
            mesure.put("type", this.getType());
            mesure.put("gps", this.getGps().toJSON());
            mesure.put("absMagneticField", this.getAbsMagneticField().toJSON());
            mesure.put("relMagneticField", this.getRelMagneticField().toJSON());
            mesure.put("relGravity", this.getRelGravity().toJSON());
            mesure.put("absLinearAcce", this.getAbsLinearAcce().toJSON());
            mesure.put("relLinearAcce", this.getRelLinearAcce().toJSON());
        } catch (org.json.JSONException e) {
            // TODO : handle exception
            Log.e("Measurement.toJSON", e.toString());
        }
        return mesure;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public GPS getGps() {
        return gps;
    }

    public Vector getAbsMagneticField() {
        return absMagneticField;
    }

    public Vector getRelMagneticField() {
        return relMagneticField;
    }

    public Vector getRelGravity() {
        return relGravity;
    }

    public Vector getAbsLinearAcce() {
        return absLinearAcce;
    }

    public Vector getRelLinearAcce() {
        return relLinearAcce;
    }
}
