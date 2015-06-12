/**
 * Created by valentin on 5/26/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.uottawa.sasyan.magneticdb.R;

import java.util.List;


public class Vector {
    float x, y, z;

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }

    public Vector(String str) {
        this.update(str);
    }

    public Vector(float[] ar) {
        this.update(ar);
    }

    public void update(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void update(float x, float y, float z, int A) {
        this.x = x*A;
        this.y = y*A;
        this.z = z*A;
    }

    public void update(Vector v) {
        this.update(v.getX(), v.getY(), v.getZ());
    }

    public void update(float[] ar) {
        this.update(ar[0], ar[1], ar[2]);
    }

    public void update(float[] ar, int A) {
        this.update(ar[0], ar[1], ar[2], A);
    }

    public void update(String str) {
        try {
            JSONObject vect = new JSONObject(str);
            this.update((float)vect.getDouble("x"), (float)vect.getDouble("y"), (float)vect.getDouble("z"));
        } catch (org.json.JSONException e) {
            // TODO : handle exception
            Log.e("Vector.updateFromJSON", e.toString());
        }
    }

    public void beMeanOf(List<Vector> list) {
        int N = list.size(), M = 0;
//        Log.w("N", String.valueOf(N));
        if (N > 0) {
            float x = 0, y = 0, z = 0;
            for (int i = 0; i < N; i++) {
                //if (list.get(i).getNorme() > 0.2) {
                    x += list.get(i).getX();
                    y += list.get(i).getY();
                    z += list.get(i).getZ();
                    M += 1;
                //}
            }
            this.update(x / Math.max(M, 1), y / Math.max(M, 1), z / Math.max(M, 1));
            //Log.w("x,y,z cor ", String.valueOf(this.getX()) + ", " + String.valueOf(this.getY()) + ", " + String.valueOf(this.getZ()));
            //Log.w("x,y,z raw ", String.valueOf(list.get(N - 1).getX()) + ", " + String.valueOf(list.get(N - 1).getY()) + ", " + String.valueOf(list.get(N - 1).getZ()));
            Log.w("N = " + String.valueOf(N) + ", m/o : ", String.valueOf(this.getNorme()/list.get(N - 1).getNorme()));
        }
    }

    public String toString() {
        return "[" + this.getStringX() + "," + this.getStringY() + "," + this.getStringZ() + "]";
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("x", this.getX());
            obj.put("y", this.getY());
            obj.put("z", this.getZ());
        } catch (org.json.JSONException e) {
            // TODO : handle exception
            Log.e("Vector.toJSON", e.toString());
        }
        return obj;
    }

    public LatLng toLatLng() {
        return new LatLng(this.getX(), this.getY());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getNorme() {
        return (float)Math.sqrt(getX()*getX() + getY()*getY() + getZ() * getZ());
    }

    public String getStringX() {
        return String.format("%.2f", this.getX());
    }

    public String getStringY() {
        return String.format("%.2f", this.getY());
    }

    public String getStringZ() {
        return String.format("%.2f", this.getZ());
    }
}
