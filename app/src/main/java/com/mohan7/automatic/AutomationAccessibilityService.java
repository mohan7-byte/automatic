package com.mohan7.automatic;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.os.SystemClock;
import android.content.Intent;

public class AutomationAccessibilityService extends AccessibilityService {
    private long lastUp, lastDown; private boolean upConsumed, downConsumed;
    @Override protected void onServiceConnected(){ super.onServiceConnected(); }
    @Override public boolean onKeyEvent(KeyEvent e){
        if(e.getAction()!=KeyEvent.ACTION_DOWN) return false;
        long now=SystemClock.uptimeMillis(); int code=e.getKeyCode();
        AutomationService s=new AutomationService();
        if(code==KeyEvent.KEYCODE_VOLUME_UP){
            if(e.getRepeatCount()>0) return true;
            if(now-lastUp<450 && !upConsumed){ upConsumed=true; startService(new Intent(this,AutomationService.class).putExtra("action","hotspot")); lastUp=0; return true; }
            lastUp=now; upConsumed=false; getMainExecutor().execute(()->checkUpHold(now)); return true;
        }
        if(code==KeyEvent.KEYCODE_VOLUME_DOWN){
            if(e.getRepeatCount()>0) return true;
            if(now-lastDown<450&&!downConsumed){ downConsumed=true; startService(new Intent(this,AutomationService.class).putExtra("action","callme")); lastDown=0; return true; }
            lastDown=now; downConsumed=false; getMainExecutor().execute(()->checkDownHold(now)); return true;
        }
        return false;
    }
    private void checkUpHold(long started){ getMainExecutor().execute(()->{ try{Thread.sleep(700);}catch(InterruptedException ignored){} if(lastUp==started&&!upConsumed) startService(new Intent(this,AutomationService.class).putExtra("action","data")); }); }
    private void checkDownHold(long started){ getMainExecutor().execute(()->{ try{Thread.sleep(700);}catch(InterruptedException ignored){} if(lastDown==started&&!downConsumed) startService(new Intent(this,AutomationService.class).putExtra("action","dad")); }); }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}
}
