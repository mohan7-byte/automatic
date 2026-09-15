package com.mohan7.automatic;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) { if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()) || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(i.getAction())) c.startService(new Intent(c, AutomationService.class)); }
}
