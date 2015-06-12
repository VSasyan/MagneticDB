/**
 * Created by valentin on 5/26/2015.
 */

package com.uottawa.sasyan.magneticdb.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.uottawa.sasyan.magneticdb.Class.Measurement;
import com.uottawa.sasyan.magneticdb.Class.Vector;
import com.uottawa.sasyan.magneticdb.Class.GPS;

import org.json.JSONArray;

public class HelperMeasurement extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CompassGPS.db";
    private static final int SCHEMA_VERSION = 7;

    // History of creation :
    private String create_v6 = "CREATE TABLE measurement (_id INTEGER PRIMARY KEY AUTOINCREMENT, gps string, absMagneticField string, relMagneticField string, relGravity string);";
    private String create_v7 = "CREATE TABLE measurement (_id INTEGER PRIMARY KEY AUTOINCREMENT, gps string, absMagneticField string, relMagneticField string, relGravity string, relLinearAcce string, absLinearAcce string);";

    // History of destruction :
    private String destro_v6 = "DROP TABLE measurement;";

    // History of select all :
    private String selAll_v6 = "SELECT _id, gps, absMagneticField, relMagneticField, relGravity FROM measurement;";
    private String selAll_v7 = "SELECT _id, gps, absMagneticField, relMagneticField, relGravity, relLinearAcce, absLinearAcce FROM measurement;";

    // SQL to use:
    private String create = create_v7;
    private String selAll = selAll_v7;

    public HelperMeasurement(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Destruction of the old table :
        if (oldVersion == 6) {
            db.execSQL(destro_v6);
        }
        // Construction of the new table :
        if (newVersion == 7) {
            db.execSQL(create);
        }
    }

    public void deleteAll() {
        getReadableDatabase().execSQL("DELETE FROM measurement;");
    }

    public void insert(Measurement measurement) {
        ContentValues cv = new ContentValues();

        cv.put("gps", measurement.getGps().toJSON().toString());
        cv.put("absMagneticField", measurement.getAbsMagneticField().toJSON().toString());
        cv.put("relMagneticField", measurement.getRelMagneticField().toJSON().toString());
        cv.put("relGravity", measurement.getRelGravity().toJSON().toString());
        cv.put("relLinearAcce", measurement.getRelLinearAcce().toJSON().toString());
        cv.put("absLinearAcce", measurement.getAbsLinearAcce().toJSON().toString());

        getWritableDatabase().insert("measurement", null, cv);
    }

    public Cursor getAll() {
        return getReadableDatabase().rawQuery(selAll, null);
    }

    public String getAllJSON() {
        Cursor c = getAll();
        JSONArray tab = new JSONArray();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Measurement mes = getMeasurement(c);
            tab.put(mes.toJSON());
            c.moveToNext();
        }
        return tab.toString();
    }

    public GPS getGPS(Cursor c) {
        return new GPS(c.getString(1));
    }

    public Vector getAbsMagneticField(Cursor c) {
        return new Vector(c.getString(2));
    }

    public Vector getRelMagneticField(Cursor c) {
        return new Vector(c.getString(3));
    }

    public Vector getRelGravity(Cursor c) {
        return new Vector(c.getString(4));
    }

    public Vector getAbsLinearAcce(Cursor c) {
        return new Vector(c.getString(5));
    }

    public Vector getRelLinearAcce(Cursor c) {
        return new Vector(c.getString(6));
    }

    public Measurement getMeasurement(Cursor c) {
        return new Measurement(getGPS(c), getAbsMagneticField(c), getRelMagneticField(c), getRelGravity(c), getAbsLinearAcce(c), getRelLinearAcce(c));
    }
}
