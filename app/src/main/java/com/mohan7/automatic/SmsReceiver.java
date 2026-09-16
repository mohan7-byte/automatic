package com.mohan7.automatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(i.getAction())) return;
        Prefs p=new Prefs(c); Bundle b=i.getExtras(); if(b==null)return;
        Object[] pdus=(Object[])b.get("pdus"); String format=b.getString("format"); if(pdus==null)return;
        for(Object x:pdus){
            SmsMessage m;
            if(Build.VERSION.SDK_INT>=23) m=SmsMessage.createFromPdu((byte[])x,format); else m=SmsMessage.createFromPdu((byte[])x);
            String sender=m.getOriginatingAddress(); String body=m.getMessageBody(); if(sender==null||body==null)continue;
            if(!sameSender(sender,p.commandNumber()))continue;
            String cmd=body.trim().toLowerCase(Locale.US).replace("#","").replaceAll("\\s+"," ");
            Intent s=new Intent(c,AutomationService.class);
            if(cmd.matches("onhotspot|hotspot on")) s.putExtra("action","hotspot_on");
            else if(cmd.matches("offhotspot|hotspot off")) s.putExtra("action","hotspot_off");
            else if(cmd.matches("ondata|data on")) s.putExtra("action","data_on");
            else if(cmd.matches("offdata|data off")) s.putExtra("action","data_off");
            else if(cmd.equals("calldad")) s.putExtra("action","dad");
            else if(cmd.equals("callme")) s.putExtra("action","callme");
            else if(cmd.equals("status")) s.putExtra("action","status");
            else continue;
            s.putExtra("reply", sender);
            if(Build.VERSION.SDK_INT>=26) c.startForegroundService(s); else c.startService(s);
        }
    }
    private boolean sameSender(String a,String b){
        if(b==null||b.isEmpty())return false;
        String n1=a.replaceAll("\\D",""); String n2=b.replaceAll("\\D","");
        return n1.equals(n2)||(n1.length()>10&&n2.endsWith(n1.substring(n1.length()-10)))||(n2.length()>10&&n1.endsWith(n2.substring(n2.length()-10)));
    }
}
