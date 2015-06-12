/**
 * Created by valentin on 05/06/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.uottawa.sasyan.magneticdb.HeatActivity;
import com.uottawa.sasyan.magneticdb.SettingsActivity;

public class Settings {
    private Context context;
    private String folder, dateFormat, fileName, show, session;
    private int showType, interType, normType;
    private boolean WifiOnly, isRecording;

    public Settings(Context context) {
        this.context = context;
        this.getSettings();
    }

    public boolean getSettings() {
        SharedPreferences preferences = context.getSharedPreferences("settings", 0);
        this.folder = preferences.getString("folder", "/MagneticDB");
        this.session = preferences.getString("session", "Session_1");
        this.dateFormat = preferences.getString("dateFormat", "yyyy-MM-dd_HH-mm-ss");
        this.WifiOnly = preferences.getBoolean("WifiOnly", true);
        this.isRecording = preferences.getBoolean("isRecording", false);
        this.show = preferences.getString("show", "all");
        this.showType = preferences.getInt("showType", 0);
        this.interType = preferences.getInt("interType", 0);
        this.normType = preferences.getInt("normType", 0);
        return true;
    }

    public boolean saveSettings() {
        SharedPreferences preferences = context.getSharedPreferences("settings", 0);
        SharedPreferences.Editor e = preferences.edit();
        e.putString("folder", folder);
        e.putString("session", session);
        e.putString("dateFormat", dateFormat);
        e.putBoolean("WifiOnly", this.WifiOnly);
        e.putBoolean("isRecording", this.isRecording);
        e.putString("show", this.show);
        e.putInt("showType", this.showType);
        e.putInt("interType", this.interType);
        e.putInt("normType", this.normType);
        e.commit();
        return true;
    }

    public void showSettings() {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public boolean isRecording() {
        return isRecording;
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

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        saveSettings();
    }
    public void setFolder(String folder) {
        this.folder = folder;
        saveSettings();
    }
    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
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

    public String getFolderSession() {
        return getFolder() + "/" + getSession();
    }

    public String getFolderPictures() {
        return getFolderSession() + "/" + "pictures";
    }
}
