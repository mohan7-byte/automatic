package com.mohan7.automatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()) && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(i.getAction())) return;
        Intent service = new Intent(c, AutomationService.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(service); else c.startService(service);
    }
}
