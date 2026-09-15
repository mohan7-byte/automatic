package com.mohan7.automatic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.io.DataOutputStream;
import java.util.Locale;

final class SystemControl {
    private SystemControl() {}

    static void vibrate(android.os.Vibrator v, long[] pattern) {
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
        else v.vibrate(pattern, -1);
    }

    static boolean setHotspot(Context c, boolean on) {
        // Android 10 does not expose a public third-party setter. Prefer an ADB/root shell fallback
        // when available, then return false so callers can report that a device-specific path is needed.
        String[] cmds = on
                ? new String[]{"svc wifi enable", "cmd connectivity tether start wifi"}
                : new String[]{"cmd connectivity tether stop wifi"};
        return shell(cmds);
    }

    static boolean setMobileData(boolean on) {
        String state = on ? "enable" : "disable";
        return shell(new String[]{"cmd phone data " + state, "svc data " + (on ? "enable" : "disable")});
    }

    static boolean isAirplane(Context c) { return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1; }

    static void call(Context c, String number) {
        if (number == null || number.trim().isEmpty()) return;
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number.trim())));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { c.startActivity(i); } catch (Exception ignored) {}
    }

    static boolean shell(String[] attempts) {
        for (String command : attempts) {
            try {
                Process p = Runtime.getRuntime().exec("sh");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes(command + "\nexit\n"); os.flush();
                int result = p.waitFor();
                if (result == 0) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    static int battery(Context c) {
        android.content.Intent i = c.registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int level = i.getIntExtra("level", -1), scale = i.getIntExtra("scale", -1);
        return scale > 0 ? Math.round(level * 100f / scale) : -1;
    }
}
