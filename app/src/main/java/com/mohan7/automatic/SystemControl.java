package com.mohan7.automatic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

final class SystemControl {
    private SystemControl() {}

    static void vibrate(android.os.Vibrator v, long[] pattern) {
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
        else v.vibrate(pattern, -1);
    }

    static boolean isHotspotOn(Context c) {
        try {
            WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method m = wm.getClass().getDeclaredMethod("getWifiApState");
            m.setAccessible(true);
            int state = (Integer) m.invoke(wm);
            return state == 13;
        } catch (Throwable ignored) { return false; }
    }

    static boolean setHotspot(Context c, boolean on) {
        String command = on ? "cmd connectivity tether start wifi" : "cmd connectivity tether stop wifi";

        // ADB/root-capable path. Shizuku runs this as shell or root, which is the same execution
        // identity needed by the Android 10 connectivity shell commands.
        if (shizukuShell(command)) return true;
        if (shell(new String[]{"su -c \"" + command + "\""})) return true;

        // MacroDroid-style legacy Android 10 path: invoke the hidden WifiManager AP method from
        // this old-target (API 28) build. Android's non-SDK restrictions are less restrictive for
        // apps targeting Android 9 and below, which is why the main app intentionally targets 28.
        try {
            WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method m = wm.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            m.setAccessible(true);
            Object result = m.invoke(wm, null, on);
            if (result instanceof Boolean && (Boolean) result) return true;
        } catch (Throwable ignored) { }

        // Xiaomi/legacy settings fallback when the user has explicitly granted special settings access.
        try {
            if (Settings.System.canWrite(c)) {
                Settings.System.putInt(c.getContentResolver(), "wifi_ap_enabled", on ? 1 : 0);
                Settings.Global.putInt(c.getContentResolver(), "wifi_ap_enabled", on ? 1 : 0);
                if (isHotspotOn(c) == on) return true;
            }
        } catch (Throwable ignored) { }
        return isHotspotOn(c) == on;
    }

    static boolean isMobileDataOn(Context c) {
        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            Method m = tm.getClass().getDeclaredMethod("isDataEnabled");
            m.setAccessible(true);
            return (Boolean)m.invoke(tm);
        } catch (Throwable ignored) {
            try { return Settings.Global.getInt(c.getContentResolver(), "mobile_data", 1) != 0; }
            catch (Throwable ignored2) { return false; }
        }
    }

    static boolean setMobileData(Context c, boolean on) {
        String state = on ? "enable" : "disable";
        String[] commands = {"cmd phone data " + state, "svc data " + state};

        for (String command : commands) if (shizukuShell(command)) return true;
        for (String command : commands) if (shell(new String[]{"su -c \"" + command + "\""})) return true;

        // Android 10 private TelephonyManager API fallback.
        try {
            TelephonyManager tm = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
            Method m = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            m.setAccessible(true);
            m.invoke(tm, on);
            if (isMobileDataOn(c) == on) return true;
        } catch (Throwable ignored) { }

        // ADB-granted WRITE_SECURE_SETTINGS / WRITE_SETTINGS fallback used by older automation tools.
        try {
            if (Settings.System.canWrite(c)) {
                Settings.Global.putInt(c.getContentResolver(), "mobile_data", on ? 1 : 0);
                Settings.Global.putInt(c.getContentResolver(), "mobile_data0", on ? 1 : 0);
                if (isMobileDataOn(c) == on) return true;
            }
        } catch (Throwable ignored) { }
        return isMobileDataOn(c) == on;
    }

    private static boolean shizukuShell(String command) {
        try {
            Class<?> cls = Class.forName("rikka.shizuku.Shizuku");
            Object alive = cls.getMethod("pingBinder").invoke(null);
            if (!(alive instanceof Boolean) || !((Boolean)alive)) return false;
            Object perm = cls.getMethod("checkSelfPermission").invoke(null);
            if (!(perm instanceof Integer) || ((Integer)perm) != PackageManager.PERMISSION_GRANTED) return false;
            Method m = cls.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            m.setAccessible(true);
            Object remote = m.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (!(remote instanceof Process)) return false;
            Process p = (Process)remote;
            boolean done = p.waitFor(4, TimeUnit.SECONDS);
            if (!done) { p.destroy(); return false; }
            return p.exitValue() == 0;
        } catch (Throwable ignored) { return false; }
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
        Intent i = c.registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int level = i.getIntExtra("level", -1), scale = i.getIntExtra("scale", -1);
        return scale > 0 ? Math.round(level * 100f / scale) : -1;
    }
}
