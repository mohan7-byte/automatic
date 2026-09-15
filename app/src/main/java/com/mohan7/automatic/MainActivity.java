package com.mohan7.automatic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private EditText dad, mine, command, night, morning;
    private CheckBox shake;
    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,24,28,24);
        TextView title = new TextView(this); title.setText("Automatic • Redmi 8A"); title.setTextSize(24); root.addView(title);
        TextView note = new TextView(this); note.setText("Setup is done here using scrcpy. Afterward the service is designed to run with the display off."); root.addView(note);
        dad = field(root, "Dad's number"); mine = field(root, "Your other number"); command = field(root, "Authorized SMS number");
        night = field(root, "Night OFF time (HH:mm)"); morning = field(root, "Morning ON time (HH:mm)");
        shake = new CheckBox(this); shake.setText("Enable shake → hotspot toggle"); root.addView(shake);
        Button save = new Button(this); save.setText("Save & Start"); save.setOnClickListener(v -> { save(); request(); }); root.addView(save);
        Button access = new Button(this); access.setText("Enable Accessibility / volume buttons"); access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(access);
        setContentView(root); load();
    }
    private EditText field(LinearLayout r, String hint) { EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(); r.addView(e); return e; }
    private void load() { Prefs p=new Prefs(this); dad.setText(p.dad()); mine.setText(p.myNumber()); command.setText(p.commandNumber()); night.setText(p.night()); morning.setText(p.morning()); shake.setChecked(p.shake()); }
    private void save() { new Prefs(this).save(dad.getText().toString().trim(), mine.getText().toString().trim(), command.getText().toString().trim(), night.getText().toString().trim(), morning.getText().toString().trim(), shake.isChecked()); startService(new Intent(this, AutomationService.class)); }
    private void request() {
        if (android.os.Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.CALL_PHONE}, 99);
    }
}
