package com.mohan7.automatic;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.os.SystemClock;
import android.content.Intent;

public class AutomationAccessibilityService extends AccessibilityService {
    private long upDownAt=0, downDownAt=0;
    @Override public boolean onKeyEvent(KeyEvent e){
        if(e.getKeyCode()!=KeyEvent.KEYCODE_VOLUME_UP && e.getKeyCode()!=KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        final int key=e.getKeyCode(); final long now=SystemClock.uptimeMillis();
        if(e.getAction()==KeyEvent.ACTION_DOWN){
            if(e.getRepeatCount()==0){
                if(key==KeyEvent.KEYCODE_VOLUME_UP){
                    if(upDownAt>0 && now-upDownAt<500){ upDownAt=0; dispatch("hotspot"); }
                    else { upDownAt=now; getMainExecutor().execute(()->holdCheck(true,now)); }
                } else {
                    if(downDownAt>0 && now-downDownAt<500){ downDownAt=0; dispatch("callme"); }
                    else { downDownAt=now; getMainExecutor().execute(()->holdCheck(false,now)); }
                }
            }
            return true;
        }
        if(e.getAction()==KeyEvent.ACTION_UP){ if(key==KeyEvent.KEYCODE_VOLUME_UP) upDownAt=0; else downDownAt=0; return true; }
        return true;
    }
    private void holdCheck(boolean up,long started){ try{Thread.sleep(700);}catch(InterruptedException ignored){} if(up ? upDownAt==started : downDownAt==started){ if(up)dispatch("data"); else dispatch("dad"); } }
    private void dispatch(String a){ startService(new Intent(this,AutomationService.class).putExtra("action",a)); }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}
}
