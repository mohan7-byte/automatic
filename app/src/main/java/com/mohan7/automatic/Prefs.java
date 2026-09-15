package com.mohan7.automatic;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private static final String FILE = "automatic";
    private final SharedPreferences p;
    Prefs(Context c) { p = c.getSharedPreferences(FILE, Context.MODE_PRIVATE); }
    String dad() { return p.getString("dad", ""); }
    String myNumber() { return p.getString("mine", ""); }
    String commandNumber() { return p.getString("cmd", myNumber()); }
    String night() { return p.getString("night", "23:00"); }
    String morning() { return p.getString("morning", "06:00"); }
    boolean shake() { return p.getBoolean("shake", true); }
    void save(String dad, String mine, String cmd, String night, String morning, boolean shake) {
        p.edit().putString("dad", dad).putString("mine", mine).putString("cmd", cmd)
            .putString("night", night).putString("morning", morning).putBoolean("shake", shake).apply();
    }
    boolean batterySent(int pct) { return p.getBoolean("battery_" + pct, false); }
    void markBatterySent(int pct) { p.edit().putBoolean("battery_" + pct, true).apply(); }
    void clearBatterySent() { p.edit().remove("battery_15").remove("battery_10").remove("battery_5").remove("battery_2").apply(); }
}
