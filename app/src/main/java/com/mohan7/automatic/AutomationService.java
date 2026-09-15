package com.mohan7.automatic;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.telephony.SmsManager;
import java.util.*;

public class AutomationService extends Service implements SensorEventListener {
    private Handler h; private SensorManager sm; private long lastShake;
    private final Runnable tick = new Runnable(){ public void run(){ runScheduleAndBattery(); if(h!=null) h.postDelayed(this,30000); }};
    @Override public void onCreate(){ super.onCreate(); h=new Handler(Looper.getMainLooper()); startForeground(7,notification("Automatic running (screen can stay off)")); sm=(SensorManager)getSystemService(SENSOR_SERVICE); if(sm!=null){Sensor s=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); if(s!=null)sm.registerListener(this,s,SensorManager.SENSOR_DELAY_NORMAL);} h.post(tick); }
    private Notification notification(String text){ if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel("automatic","Automatic",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);} Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"automatic"):new Notification.Builder(this); return b.setContentTitle("Automatic").setContentText(text).setSmallIcon(android.R.drawable.ic_popup_sync).setOngoing(true).build(); }
    private void runScheduleAndBattery(){ Prefs p=new Prefs(this); Calendar c=Calendar.getInstance(); String now=String.format(Locale.US,"%02d:%02d",c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE)); if(now.equals(p.night())){SystemControl.setHotspot(this,false);SystemControl.setMobileData(false);} if(now.equals(p.morning())){SystemControl.setHotspot(this,true);SystemControl.setMobileData(true);} int pct=SystemControl.battery(this); int[] marks={15,10,5,2}; for(int m:marks)if(pct==m&&!p.batterySent(m)){sms(p.commandNumber(),"⚠️ Redmi 8A Alert: Battery at "+m+"%. Hotspot is still running.");p.markBatterySent(m);} if(pct>=30)p.clearBatterySent(); }
    static void sms(String to,String body){if(to==null||to.trim().isEmpty())return;try{SmsManager.getDefault().sendTextMessage(to,null,body,null,null);}catch(Exception ignored){}}
    @Override public int onStartCommand(Intent i,int flags,int id){String a=i==null?null:i.getStringExtra("action");if(a!=null)doAction(a,i.getStringExtra("reply"));return START_STICKY;}
    private void doAction(String a,String reply){ Prefs p=new Prefs(this); boolean ok=false; if("hotspot".equals(a)||"hotspot_on".equals(a))ok=SystemControl.setHotspot(this,true); else if("hotspot_off".equals(a))ok=SystemControl.setHotspot(this,false); else if("data".equals(a)||"data_on".equals(a))ok=SystemControl.setMobileData(true); else if("data_off".equals(a))ok=SystemControl.setMobileData(false); else if("dad".equals(a)){SystemControl.call(this,p.dad());return;} else if("callme".equals(a)){SystemControl.call(this,p.myNumber());return;} else if("status".equals(a)){int b=SystemControl.battery(this);sms(reply,"Redmi 8A: Battery "+b+"% | Schedule: "+p.night()+" - "+p.morning());return;}
        android.os.Vibrator v=(android.os.Vibrator)getSystemService(VIBRATOR_SERVICE); if(a.startsWith("hotspot")){SystemControl.vibrate(v,ok&& !"hotspot_off".equals(a)?new long[]{0,700}:new long[]{0,120,120,120});} else if(a.startsWith("data")){SystemControl.vibrate(v,new long[]{0,120});} if(reply!=null)sms(reply,"Automatic: "+a+" "+(ok?"OK":"requested")); }
    public void toggleHotspot(){doAction("hotspot",null);} public void toggleData(){doAction("data",null);}
    @Override public void onSensorChanged(SensorEvent e){Prefs p=new Prefs(this);if(!p.shake()||e.sensor.getType()!=Sensor.TYPE_ACCELEROMETER)return;float x=e.values[0],y=e.values[1],z=e.values[2];float g=(float)Math.sqrt(x*x+y*y+z*z);if(g>18&&SystemClock.elapsedRealtime()-lastShake>1200){lastShake=SystemClock.elapsedRealtime();toggleHotspot();}}
    @Override public void onAccuracyChanged(Sensor s,int a){}
    @Override public void onDestroy(){if(sm!=null)sm.unregisterListener(this);if(h!=null)h.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
