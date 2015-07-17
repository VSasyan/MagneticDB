/**
 * Created by valentin on 05/06/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.uottawa.sasyan.magneticdb.MainActivity;
import com.uottawa.sasyan.magneticdb.SettingsActivity;

public class Settings {
    private Activity activity;
    private String folder, dateFormat, show, session;
    private int showType, interType, normType, timeGPS, timeSpot, sizeAverage, minimumAccuracy;
    private boolean WifiOnly, recording, spotting;

    public Settings(Activity activity) {
        this.activity = activity;
        this.getSettings();
    }

    public boolean getSettings() {
        boolean success = false;
        SharedPreferences preferences = activity.getSharedPreferences("settings", 0);
        try {
            success = this.getSettings(preferences);
        } catch (Exception e) {
            Log.e("Loading preferences", e.toString() + "; Preferences cleaned");
            preferences.edit().clear().apply();
            success = this.getSettings(preferences);
        }
        return success;
    }

    private boolean getSettings(SharedPreferences preferences) {
        this.folder = preferences.getString("folder", "/MagneticDB");
        this.session = preferences.getString("session", "Session_1");
        this.dateFormat = preferences.getString("dateFormat", "yyyy-MM-dd_HH-mm-ss");
        this.WifiOnly = preferences.getBoolean("WifiOnly", true);
        this.recording = preferences.getBoolean("recording", false);
        this.spotting = preferences.getBoolean("spotting", true);
        this.show = preferences.getString("show", "all");
        this.showType = preferences.getInt("showType", 0);
        this.interType = preferences.getInt("interType", 0);
        this.normType = preferences.getInt("normType", 0);
        this.timeGPS = preferences.getInt("timeGPS", 3000);
        this.timeSpot = preferences.getInt("timeSpot", 6000);
        this.sizeAverage = preferences.getInt("sizeAverage", 100);
        this.minimumAccuracy = preferences.getInt("minimumAccuracy", 2);
        return true;
    }

    public boolean saveSettings() {
        SharedPreferences preferences = activity.getSharedPreferences("settings", 0);
        SharedPreferences.Editor e = preferences.edit();
        e.putString("folder", folder);
        e.putString("session", session);
        e.putString("dateFormat", dateFormat);
        e.putBoolean("WifiOnly", this.WifiOnly);
        e.putBoolean("recording", this.recording);
        e.putBoolean("spotting", this.spotting);
        e.putString("show", this.show);
        e.putInt("showType", this.showType);
        e.putInt("interType", this.interType);
        e.putInt("normType", this.normType);
        e.putInt("timeGPS", this.timeGPS);
        e.putInt("timeSpot", this.timeSpot);
        e.putInt("sizeAverage", this.sizeAverage);
        e.putInt("minimumAccuracy", this.minimumAccuracy);
        e.commit();
        return true;
    }

    public void showSettings() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_SETTINGS);
    }

    public boolean getRecording() {
        return recording;
    }
    public boolean getSpotting() {
        return spotting;
    }
    public boolean isWifiOnly() {
        return WifiOnly;
    }
    public String getDateFormat() {
        return dateFormat;
    }
    public String getFolder() {
        return folder;
    }
    public String getShow() {
        return show;
    }
    public int getShowType() {
        return showType;
    }
    public int getInterType() {
        return interType;
    }
    public int getNormType() {
        return normType;
    }
    public String getSession() {
        return session;
    }
    public int getTimeGPS() {
        return timeGPS;
    }
    public int getTimeSpot() {
        return timeSpot;
    }
    public int getSizeAverage() {
        return sizeAverage;
    }
    public int getMinimumAccuracy() {
        return minimumAccuracy;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        saveSettings();
    }
    public void setFolder(String folder) {
        this.folder = folder;
        saveSettings();
    }
    public void setRecording(boolean recording) {
        this.recording = recording;
        saveSettings();
    }
    public void setSpotting(boolean spotting) {
        this.spotting = spotting;
        saveSettings();
    }
    public void setShow(String show) {
        this.show = show;
        saveSettings();
    }
    public void setWifiOnly(boolean wifiOnly) {
        WifiOnly = wifiOnly;
        saveSettings();
    }
    public void setShowType(int showType) {
        this.showType = showType;
        saveSettings();
    }
    public void setInterType(int interType) {
        this.interType = interType;
        saveSettings();
    }
    public void setNormType(int normType) {
        this.normType = normType;
        saveSettings();
    }
    public void setSession(String session) {
        this.session = session;
        saveSettings();
    }
    public void setTimeGPS(int timeGPS) {
        this.timeGPS = timeGPS;
        saveSettings();
    }
    public void setTimeSpot(int timeSpot) {
        this.timeSpot = timeSpot;
        saveSettings();
    }
    public void setSizeAverage(int sizeAverage) {
        this.sizeAverage = sizeAverage;
        saveSettings();
    }
    public void setMinimumAccuracy(int minimumAccuracy) {
        this.minimumAccuracy = minimumAccuracy;
        saveSettings();
    }

    public void setTimeGPS(String timeGPS) {
        ConvertInt time = new ConvertInt(timeGPS, 3000);
        setTimeGPS(time.getValue());
    }
    public void setTimeSpot(String timeSpot) {
        ConvertInt time = new ConvertInt(timeSpot, 6000);
        setTimeSpot(time.getValue());
    }
    public void setSizeAverage(String sizeAverage) {
        ConvertInt size = new ConvertInt(sizeAverage, 100);
        setSizeAverage(size.getValue());
    }

    public String getFolderSession() {
        return getFolder() + "/" + getSession();
    }

    public String getFolderPictures() {
        return getFolderSession() + "/" + "pictures";
    }
}
