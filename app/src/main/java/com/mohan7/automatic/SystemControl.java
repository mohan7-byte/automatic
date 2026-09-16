package com.mohan7.automatic;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

final class SystemControl {
    private SystemControl() {}

    static void vibrate(android.os.Vibrator v, long[] pattern) {
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
        else v.vibrate(pattern, -1);
    }

    static boolean isHotspotOn(Context c) {
        try {
            android.net.wifi.WifiManager wm = (android.net.wifi.WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method m = wm.getClass().getMethod("getWifiApState");
            int state = (Integer) m.invoke(wm);
            return state == 13 || state == 12;
        } catch (Throwable ignored) { return false; }
    }

    static boolean setHotspot(Context c, boolean on) {
        // 1) ADB/root-like shell when the process has that privilege (for example via su/Shizuku-style execution).
        String rootCommand = on
                ? "cmd connectivity tether start wifi"
                : "cmd connectivity tether stop wifi";
        if (shell(new String[]{"su -c \"" + rootCommand + "\"", rootCommand})) return true;

        // 2) The same private API family used by legacy automation helpers on Android 10 / older target SDKs.
        if (legacyTether(c, on)) return true;

        // 3) Last fallback: if the device grants WRITE_SETTINGS, try the legacy tether setting path.
        try {
            if (Settings.System.canWrite(c)) {
                Settings.System.putInt(c.getContentResolver(), "wifi_ap_enabled", on ? 1 : 0);
                Settings.Global.putInt(c.getContentResolver(), "wifi_ap_enabled", on ? 1 : 0);
                return isHotspotOn(c) == on;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private static boolean legacyTether(Context c, boolean on) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (on) {
                final Object callback = new ConnectivityManager.OnStartTetheringCallback() {
                    @Override public void onTetheringStarted() { }
                    @Override public void onTetheringFailed() { }
                };
                Method m = cm.getClass().getDeclaredMethod(
                        "startTethering", int.class, boolean.class,
                        ConnectivityManager.OnStartTetheringCallback.class, Handler.class);
                m.setAccessible(true);
                m.invoke(cm, ConnectivityManager.TETHERING_WIFI, false, callback, new Handler(Looper.getMainLooper()));
                return true;
            }
            Method stop = cm.getClass().getDeclaredMethod("stopTethering", int.class);
            stop.setAccessible(true);
            stop.invoke(cm, ConnectivityManager.TETHERING_WIFI);
            return true;
        } catch (Throwable ignored) { return false; }
    }

    static boolean isMobileDataOn(Context c) {
        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            Method m = tm.getClass().getMethod("isDataEnabled");
            return (Boolean) m.invoke(tm);
        } catch (Throwable ignored) {
            try { return Settings.Global.getInt(c.getContentResolver(), "mobile_data", 1) != 0; }
            catch (Throwable ignored2) { return false; }
        }
    }

    static boolean setMobileData(Context c, boolean on) {
        String state = on ? "enable" : "disable";
        if (shell(new String[]{"su -c \"cmd phone data " + state + "\"", "cmd phone data " + state, "svc data " + state})) return true;

        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            Method m = tm.getClass().getMethod("setDataEnabled", boolean.class);
            m.invoke(tm, on);
            if (isMobileDataOn(c) == on) return true;
        } catch (Throwable ignored) { }

        // Xiaomi/older Android fallback used by settings/automation helpers. This requires a privileged
        // settings grant and is deliberately attempted only after the stronger paths above.
        try {
            if (Settings.System.canWrite(c)) {
                Settings.Global.putInt(c.getContentResolver(), "mobile_data", on ? 1 : 0);
                Settings.Global.putInt(c.getContentResolver(), "mobile_data0", on ? 1 : 0);
                if (isMobileDataOn(c) == on) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    static void call(Context c, String number) {
        if (number == null || number.trim().isEmpty()) return;
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number.trim())));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { c.startActivity(i); } catch (Exception ignored) {}
    }

    static boolean shell(String[] attempts) {
        for (String command : attempts) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", command});
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("exit\n"); os.flush();
                int result = p.waitFor();
                if (result == 0) return true;
            } catch (Throwable ignored) { }
        }
        return false;
    }

    static int battery(Context c) {
        Intent i = c.registerReceiver(null, new IntentFilterCompat().filter());
        if (i == null) return -1;
        int level = i.getIntExtra("level", -1), scale = i.getIntExtra("scale", -1);
        return scale > 0 ? Math.round(level * 100f / scale) : -1;
    }

    private static final class IntentFilterCompat {
        android.content.IntentFilter filter() { return new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED); }
    }
}
