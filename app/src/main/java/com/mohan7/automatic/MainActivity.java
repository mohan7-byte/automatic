package com.mohan7.automatic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    private static final int SHIZUKU_REQUEST = 701;
    private EditText dad, mine, command, night, morning;
    private CheckBox shake;

    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,24,28,24);
        TextView title = new TextView(this); title.setText("Automatic • Redmi 8A"); title.setTextSize(24); root.addView(title);
        TextView note = new TextView(this); note.setText("One-time setup uses scrcpy. After saving, automation never intentionally wakes the display."); root.addView(note);
        dad = field(root, "Dad's number"); mine = field(root, "Your other number"); command = field(root, "Authorized SMS number");
        night = field(root, "Night OFF time (HH:mm)"); morning = field(root, "Morning ON time (HH:mm)");
        shake = new CheckBox(this); shake.setText("Enable shake → hotspot toggle"); root.addView(shake);

        Button save = new Button(this); save.setText("Save & Start"); save.setOnClickListener(v -> { save(); request(); }); root.addView(save);
        Button shizuku = new Button(this); shizuku.setText("Grant ADB shell control (Shizuku)"); shizuku.setOnClickListener(v -> requestShizuku()); root.addView(shizuku);
        Button access = new Button(this); access.setText("Enable Accessibility / volume buttons"); access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(access);
        Button write = new Button(this); write.setText("Allow system settings access"); write.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()))); }
            catch (Exception ignored) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        }); root.addView(write);
        Button start = new Button(this); start.setText("Start headless service"); start.setOnClickListener(v -> startAutomationService()); root.addView(start);
        setContentView(root); load();
    }

    private EditText field(LinearLayout r, String hint) { EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(); r.addView(e); return e; }
    private void load() { Prefs p=new Prefs(this); dad.setText(p.dad()); mine.setText(p.myNumber()); command.setText(p.commandNumber()); night.setText(p.night()); morning.setText(p.morning()); shake.setChecked(p.shake()); }
    private void save() { new Prefs(this).save(dad.getText().toString().trim(), mine.getText().toString().trim(), command.getText().toString().trim(), night.getText().toString().trim(), morning.getText().toString().trim(), shake.isChecked()); startAutomationService(); }
    private void startAutomationService() { Intent i=new Intent(this, AutomationService.class); if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i); }
    private void request() { if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.CALL_PHONE}, 99); }

    private void requestShizuku() {
        try {
            if (!Shizuku.pingBinder()) { Toast.makeText(this, "Start Shizuku first, then press this again", Toast.LENGTH_LONG).show(); return; }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ADB shell control is already granted", Toast.LENGTH_SHORT).show(); return;
            }
            Shizuku.requestPermission(SHIZUKU_REQUEST);
        } catch (Throwable t) {
            Toast.makeText(this, "Shizuku is not available: " + t.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }
}
