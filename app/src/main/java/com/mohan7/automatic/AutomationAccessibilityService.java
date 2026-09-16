package com.mohan7.automatic;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;

public class AutomationAccessibilityService extends AccessibilityService {
    private static final long DOUBLE_WINDOW_MS = 500L;
    private static final long HOLD_MS = 700L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastUpDown = 0L, lastDownDown = 0L;
    private boolean upHeld = false, downHeld = false;
    private boolean upLongFired = false, downLongFired = false;

    private final Runnable upHold = new Runnable() {
        @Override public void run() {
            if (upHeld && lastUpDown > 0 && !upLongFired) {
                upLongFired = true;
                dispatch("data");
            }
        }
    };
    private final Runnable downHold = new Runnable() {
        @Override public void run() {
            if (downHeld && lastDownDown > 0 && !downLongFired) {
                downLongFired = true;
                dispatch("dad");
            }
        }
    };

    @Override public boolean onKeyEvent(KeyEvent e) {
        final int key = e.getKeyCode();
        if (key != KeyEvent.KEYCODE_VOLUME_UP && key != KeyEvent.KEYCODE_VOLUME_DOWN) return false;

        final long now = SystemClock.uptimeMillis();
        final boolean isUp = key == KeyEvent.KEYCODE_VOLUME_UP;

        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) {
            if (isUp) {
                if (lastUpDown > 0 && now - lastUpDown <= DOUBLE_WINDOW_MS && !upLongFired) {
                    handler.removeCallbacks(upHold);
                    upHeld = false;
                    lastUpDown = 0L;
                    dispatch("hotspot");
                } else {
                    lastUpDown = now;
                    upHeld = true;
                    upLongFired = false;
                    handler.postDelayed(upHold, HOLD_MS);
                }
            } else {
                if (lastDownDown > 0 && now - lastDownDown <= DOUBLE_WINDOW_MS && !downLongFired) {
                    handler.removeCallbacks(downHold);
                    downHeld = false;
                    lastDownDown = 0L;
                    dispatch("callme");
                } else {
                    lastDownDown = now;
                    downHeld = true;
                    downLongFired = false;
                    handler.postDelayed(downHold, HOLD_MS);
                }
            }
            return true;
        }

        if (e.getAction() == KeyEvent.ACTION_UP) {
            if (isUp) {
                upHeld = false;
                if (!upLongFired) {
                    handler.postDelayed(() -> {
                        if (!upHeld && lastUpDown != 0 && SystemClock.uptimeMillis() - lastUpDown > DOUBLE_WINDOW_MS) lastUpDown = 0L;
                    }, DOUBLE_WINDOW_MS + 20L);
                }
            } else {
                downHeld = false;
                if (!downLongFired) {
                    handler.postDelayed(() -> {
                        if (!downHeld && lastDownDown != 0 && SystemClock.uptimeMillis() - lastDownDown > DOUBLE_WINDOW_MS) lastDownDown = 0L;
                    }, DOUBLE_WINDOW_MS + 20L);
                }
            }
            return true;
        }

        return true;
    }

    private void dispatch(String action) {
        Intent i = new Intent(this, AutomationService.class).putExtra("action", action);
        if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent e) { }
    @Override public void onInterrupt() { }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
