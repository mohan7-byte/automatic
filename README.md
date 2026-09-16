# Automatic — Redmi 8A Headless Controller

Personal-use Android 10 controller for a Redmi 8A with a broken / ghost-touch display.

## Core rule
The app is **strictly headless after setup**. It does not wake the display to perform a task and does not use touch automation. Setup is done once through scrcpy.

## Controls
- Volume Up double-press → toggle hotspot
- Volume Up long-press (~700 ms) → toggle mobile data
- Volume Down double-press → call the configured second number
- Volume Down long-press (~700 ms) → call Dad
- Shake → toggle hotspot
- Configurable night OFF / morning ON schedule for hotspot + mobile data
- SMS remote commands from one authorized number
- Battery SMS alerts at 15%, 10%, 5%, and 2%; alerts do **not** switch anything off

## Connectivity control strategy
MacroDroid documents that its Universal Helper is used on Android 13 and below because older-target applications can retain access to restricted connectivity operations. For Xiaomi devices on Android 11 or lower, MacroDroid says the Universal Helper should work; its mobile-data action uses the ADB-hack/privileged route, and its hotspot action uses private/legacy mechanisms. citeturn381909search0turn381909search6turn381909search11

Automatic uses the same class of approach:

1. **Shizuku shell/ADB identity** — runs `cmd phone data enable/disable`, `svc data enable/disable`, and `cmd connectivity tether start/stop wifi` as shell/root when Shizuku is available. Shizuku's API is specifically designed to let normal apps execute with ADB/root identity; on non-rooted Android 10 it must be started with ADB and restarted after reboot. citeturn312941search1turn312941search0
2. **Root/SU path** — tries the same commands through `su` when the phone is rooted.
3. **Legacy Android 10 private API** — invokes the hidden `WifiManager.setWifiApEnabled` path from an API-28-targeted build, matching the old-target-helper technique documented by MacroDroid. citeturn381909search0turn381909search11
4. **ADB-granted settings fallback** — uses the system settings database when the user has explicitly granted the special settings access.
5. **Read-back verification** — hotspot/data state is queried before the app reports ON/OFF success.

## One-time setup through scrcpy
1. Install the APK on the Redmi 8A.
2. Install and start **Shizuku** on the phone. On Android 10, start Shizuku from your PC with ADB; Shizuku documents that Android versions before 11 require a computer for this startup and that non-root Shizuku must be restarted after each reboot. citeturn312941search1
3. Open Automatic → **Grant ADB shell control (Shizuku)** and approve Automatic in Shizuku.
4. Grant SMS/call/notification/vibration permissions.
5. Enable **Automatic** in Accessibility so hardware volume keys are captured without touch.
6. Grant **Allow system settings access**.
7. Optionally grant the development-only secure-settings permission from the PC:
   `adb shell pm grant com.mohan7.automatic android.permission.WRITE_SECURE_SETTINGS`
8. Disable MIUI battery optimization for Automatic and enable Auto-start.
9. Configure Dad's number, your other number, authorized SMS number, night time, morning time, and shake mode.
10. After setup, keep the physical display off. Verify the volume, shake, schedule and SMS paths with the display off.

### Shizuku startup on Redmi 8A / Android 10
Connect the phone over USB with ADB debugging enabled, then start Shizuku using the current startup instructions shown by the Shizuku app. Do **not** rely on an old wireless-debugging-only procedure: Android 10 does not have the Android 11+ built-in wireless debugging flow. LADB's Android 10 documentation also describes the older `adb tcpip 5555` approach as a temporary alternative that must be repeated after reboot. citeturn312941search1turn381909search4

## SMS commands
Only the configured authorized SMS sender is accepted:

- `onhotspot`
- `#hotspot on`
- `offhotspot`
- `ondata`
- `offdata`
- `calldad`
- `callme`
- `status`

Remote commands return a small SMS result. `status` reads the current hotspot/data state rather than relying on an in-memory guess.

## Battery alerts
At **15%, 10%, 5%, and 2%**, the app silently sends the warning SMS to your configured **other number**. It does not automatically shut down hotspot or mobile data.

## Haptics
- Hotspot ON → 1 long vibration
- Hotspot OFF → 2 short vibrations
- Mobile data action → 1 short vibration
- Calls → no vibration
- Scheduled and battery-triggered actions do not wake the display

## Build
- Native Java Android Views, no WebView
- minSdk 26
- targetSdk 28
- version 1.1.0
- intended for the Redmi 8A / Android 10 use case
