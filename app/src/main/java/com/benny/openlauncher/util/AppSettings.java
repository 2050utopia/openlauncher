package com.benny.openlauncher.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.benny.openlauncher.App;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by gregor on 07.05.17.
 */

public class AppSettings {
    private static final String ARRAY_SEPERATOR = "%%";
    private static final String SHARED_PREF_APP = "app";
    private final SharedPreferences prefApp;
    private final Context context;

    private AppSettings(Context context) {
        this.context = context.getApplicationContext();
        prefApp = context.getSharedPreferences(SHARED_PREF_APP, Context.MODE_PRIVATE);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    @SuppressLint("ApplySharedPref")
    public void resetSettings() {
        prefApp.edit().clear().commit();
    }

    public Context getApplicationContext() {
        return context;
    }

    public void clearAppSettings() {
        prefApp.edit().clear().commit();
    }

    public void registerPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        prefApp.registerOnSharedPreferenceChangeListener(value);
    }

    public void unregisterPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        prefApp.unregisterOnSharedPreferenceChangeListener(value);
    }

    // helpers for modifying preferences based on type
    private String getKey(int stringKeyResourceId) {
        return context.getString(stringKeyResourceId);
    }

    public boolean isKeyEqual(String key, int stringKeyRessourceId) {
        return key.equals(getKey(stringKeyRessourceId));
    }

    private void setString(int keyRessourceId, String value) {
        prefApp.edit().putString(context.getString(keyRessourceId), value).apply();
    }

    private void setInt(int keyRessourceId, int value) {
        prefApp.edit().putInt(context.getString(keyRessourceId), value).apply();
    }

    private void setLong(int keyRessourceId, long value) {
        prefApp.edit().putLong(context.getString(keyRessourceId), value).apply();
    }

    private void setBool(int keyRessourceId, boolean value) {
        prefApp.edit().putBoolean(context.getString(keyRessourceId), value).apply();
    }

    private void setStringArray(int keyRessourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append("%%%");
            sb.append(value.toString());
        }
        setString(keyRessourceId, sb.toString().replaceFirst("%%%", ""));
    }

    private String[] getStringArray(int keyRessourceId) {
        String value = prefApp.getString(context.getString(keyRessourceId), "%%%");
        if (value.equals("%%%")) {
            return new String[0];
        }
        return value.split("%%%");
    }

    private String getString(int ressourceId, String defaultValue) {
        return prefApp.getString(context.getString(ressourceId), defaultValue);
    }

    private String getString(int ressourceId, int ressourceIdDefaultValue) {
        return prefApp.getString(context.getString(ressourceId), context.getString(ressourceIdDefaultValue));
    }

    private boolean getBool(int ressourceId, boolean defaultValue) {
        return prefApp.getBoolean(context.getString(ressourceId), defaultValue);
    }

    private int getInt(int ressourceId, int defaultValue) {
        return prefApp.getInt(context.getString(ressourceId), defaultValue);
    }

    private int getIntOfStringPref(int ressourceId, int defaultValue) {
        String strNum = prefApp.getString(context.getString(ressourceId), Integer.toString(defaultValue));
        return Integer.valueOf(strNum);
    }

    private long getLong(int ressourceId, long defaultValue) {
        return prefApp.getLong(context.getString(ressourceId), defaultValue);
    }

    private int getColor(String key, int defaultColor) {
        return prefApp.getInt(key, defaultColor);
    }

    private int getColorRes(@ColorRes int resColorId) {
        return ContextCompat.getColor(context, resColorId);
    }

    private void setDouble(int keyResId, double value) {
        prefApp.edit().putLong(context.getString(keyResId), Double.doubleToRawLongBits(value)).apply();
    }

    private double getDouble(int keyResId, double defaultValue) {
        if (!prefApp.contains(context.getString(keyResId))) {
            return defaultValue;
        }
        return Double.longBitsToDouble(prefApp.getLong(context.getString(keyResId), 0));
    }

    private void setIntList(int keyResId, ArrayList<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPERATOR);
            sb.append(Integer.toString(value));
        }
        setString(keyResId, sb.toString().replaceFirst(ARRAY_SEPERATOR, ""));
    }

    private ArrayList<Integer> getIntList(int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = getString(keyResId, ARRAY_SEPERATOR);
        if (value.equals(ARRAY_SEPERATOR)) {
            return ret;
        }
        for (String s : value.split(ARRAY_SEPERATOR)) {
            ret.add(Integer.parseInt(s));
        }
        return ret;
    }

    // methods that actually modify the preferences
    public int getIconSize() {
        return getInt(R.string.pref_key_icon_size, 52);
    }

    public boolean isAppFirstLaunch() {
        return getBool(R.string.pref_key_first_start, true);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppFirstLaunch(boolean value) {
        // MUST be committed
        prefApp.edit().putBoolean(context.getString
                (R.string.pref_key_first_start), value).commit();
    }

    public int getDesktopMode() {
        return getIntOfStringPref(R.string.pref_key_desktop_style, Desktop.DesktopMode.NORMAL);
    }

    public boolean isDesktopLocked() {
        return getBool(R.string.pref_key_desktop_lock, false);
    }

    public void setDesktopLocked(boolean value) {
        setBool(R.string.pref_key_desktop_lock, value);
    }

    public int getAppDrawerMode() {
        return getIntOfStringPref(R.string.pref_key_drawer_style, AppDrawerController.DrawerMode.VERTICAL);
    }

    public String getIconPack() {
        return getString(R.string.pref_key_icon_pack, "");
    }

    public void setIconPack(String value) {
        setString(R.string.pref_key_icon_pack, value);
    }

    public boolean isDockShowLabel() {
        return getBool(R.string.pref_key_dock_show_label, false);
    }

    public boolean isDrawerShowLabel() {
        return getBool(R.string.pref_key_drawer_show_label, true);
    }

    public int getDrawerColumnCount_Portrait() {
        return getInt(R.string.pref_key_drawer_columns, 5);
    }

    public int getDrawerRowCount_Portrait() {
        return getInt(R.string.pref_key_drawer_rows, 6);
    }

    public int getDrawerColumnCount_Landscape() {
        return getInt(R.string.pref_key_drawer_columns_landscape, 5);
    }

    public int getDrawerRowCount_Landscape() {
        return getInt(R.string.pref_key_drawer_rows_landscape, 3);
    }

    public int getDesktopColumnCount() {
        return getInt(R.string.pref_key_desktop_columns, 5);
    }

    public int getDesktopRowCount() {
        return getInt(R.string.pref_key_desktop_rows, 6);
    }

    public int getDockSize() {
        return getInt(R.string.pref_key_dock_size, 5);
    }

    public boolean isMinibarEnabled() {
        return getBool(R.string.pref_key_minibar_enable, true);
    }

    public void setMinibarEnabled(boolean value) {
        setBool(R.string.pref_key_minibar_enable, value);
    }

    public ArrayList<String> getMinibarArrangement() {
        ArrayList<String> ret = new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key_minibar_data)));
        if (ret.isEmpty()) {
            for (LauncherAction.ActionDisplayItem item : LauncherAction.actionDisplayItems) {
                ret.add("0" + item.label.toString());
            }
            setMinibarArrangement(ret);
        }
        return ret;
    }

    public void setMinibarArrangement(ArrayList<String> value) {
        setStringArray(R.string.pref_key_minibar_data, value.toArray(new String[value.size()]));
    }

    public boolean isDesktopSearchbarEnabled() {
        return getBool(R.string.pref_key_search_bar_enable, true);
    }

    public boolean isDesktopFullscreen() {
        return getBool(R.string.pref_key_desktop_fullscreen, false);
    }

    public boolean isDesktopShowPageIndicator() {
        return getBool(R.string.pref_key_desktop_show_position_indicator, true);
    }

    public boolean isDesktopShowLabel() {
        return getBool(R.string.pref_key_desktop_show_label, true);
    }

    public int getDesktopPageCurrent() {
        return getInt(R.string.pref_key_desktop_current_position, 0);
    }

    public void setDesktopPageCurrent(int value) {
        setInt(R.string.pref_key_desktop_current_position, value);
    }

    public boolean isOpenAppDrawerOnSwipe() {
        return getBool(R.string.pref_key_dock_swipe_up, true);
    }

    public boolean isDrawerUseCard() {
        return getBool(R.string.pref_key_drawer_show_card_view, true);
    }

    public boolean isDrawerRememberPage() {
        return getBool(R.string.pref_key_drawer_remember_position, true);
    }

    public boolean isDrawerShowIndicator() {
        return getBool(R.string.pref_key_drawer_show_position_indicator, true);
    }

    public int getDrawerBackgroundColor() {
        return getInt(R.string.pref_key_drawer_background_color, Color.TRANSPARENT);
    }

    public int getDrawerCardColor() {
        return getInt(R.string.pref_key_drawer_card_color, Color.WHITE);
    }

    public int getDrawerLabelColor() {
        return getInt(R.string.pref_key_drawer_label_color, Color.DKGRAY);
    }

    public int getDestkopFolderColor() {
        return getInt(R.string.pref_key_desktop_folder_color, Color.WHITE);
    }

    public int getDesktopColor() {
        return getInt(R.string.pref_key_desktop_background_color, Color.TRANSPARENT);
    }

    public int getDockColor() {
        return getInt(R.string.pref_key_dock_background_color, Color.TRANSPARENT);
    }

    public boolean isDockEnable() {
        return getBool(R.string.pref_key_dock_enable, true);
    }

    public void setDockEnable(boolean enable) {
        setBool(R.string.pref_key_dock_enable, enable);
    }

    public ArrayList<String> getHiddenAppsList() {
        return new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key_hide_apps)));
    }

    public void setHiddenAppsList(ArrayList<String> value) {
        setStringArray(R.string.pref_key_hide_apps, value.toArray(new String[value.size()]));
    }

    public boolean isAppRestartRequired() {
        return getBool(R.string.pref_key_queue_restart, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppRestartRequired(boolean value) {
        // MUST be committed
        prefApp.edit().putBoolean(context.getString
                (R.string.pref_key_queue_restart), value).commit();
    }
}
