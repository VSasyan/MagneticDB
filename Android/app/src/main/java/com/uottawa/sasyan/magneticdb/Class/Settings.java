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
    private String folderJSON, dateFormat, fileName, show, folderPicture;
    private int showType, interType, normType;
    private boolean WifiOnly, isRecording;

    public Settings(Context context) {
        this.context = context;
        this.getSettings();
    }

    public boolean getSettings() {
        SharedPreferences preferences = context.getSharedPreferences("settings", 0);
        this.folderJSON = preferences.getString("folderJSON", "");
        this.folderPicture = preferences.getString("folderPicture", "");
        this.fileName = preferences.getString("fileName", "MagneticDB");
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
        e.putString("folderJSON", folderJSON);
        e.putString("folderPicture", folderPicture);
        e.putString("fileName", fileName);
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
    public String getFileName() {
        return fileName;
    }
    public String getFolderJSON() {
        return folderJSON;
    }
    public String getFolderPicture() {
        return folderPicture;
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

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        saveSettings();
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
        saveSettings();
    }
    public void setFolderJSON(String folderJSON) {
        this.folderJSON = folderJSON;
        saveSettings();
    }
    public void setFolderPicture(String folderPicture) {
        this.folderPicture = folderPicture;
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
}
