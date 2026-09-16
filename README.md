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

## Why this version uses privileged/legacy methods
MacroDroid documents that its Universal Helper is used on Android 13 and below specifically because Android gradually restricted direct connectivity control. For Xiaomi devices running Android 11 or lower, MacroDroid says the Universal Helper should work without the newer Xiaomi connectivity helper. Its mobile-data action requires the ADB-hack/privileged route, and its hotspot action uses private/legacy mechanisms with alternative methods. citeturn311728search0turn760946search1turn760946search4

This app therefore uses the same **class of strategy** rather than pretending an ordinary `WifiManager`/`TelephonyManager` call is enough:

1. Try privileged shell commands (`cmd connectivity tether ...`, `cmd phone data ...`, `svc data ...`) when the device exposes a shell/root/Shizuku-equivalent execution path.
2. Try legacy/private Android connectivity APIs available to the old-target build.
3. Try the privileged Settings database fallback when `WRITE_SETTINGS` is actually granted.
4. Read back the state before reporting success.

Android 10 itself restricts public connectivity APIs, and `setWifiApEnabled()` is hidden/deprecated; the framework tethering APIs are privileged. citeturn760946search5turn581602search0turn229204search0

## One-time setup
1. Install the debug/release APK on the Redmi 8A and use scrcpy while setting it up.
2. Grant SMS, call, notification and vibration permissions.
3. Enable the **Automatic** accessibility service so volume-key events are captured without touching the display.
4. In Automatic, enter Dad's number, your other number, authorized SMS number, night time and morning time.
5. Grant **Allow system settings access** from the app's setup screen. This is a special access, not the same as a normal runtime permission.
6. Disable MIUI battery optimization for Automatic and enable Auto-start.
7. If using an ADB/Shizuku/root execution path on this personal device, enable that execution path as described by your chosen tool. The app automatically attempts the privileged shell commands first.
8. After setup, keep the physical display off. Verify the volume, shake, schedule and SMS paths with the display off.

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
At **15%, 10%, 5%, and 2%**, the app silently sends the configured warning SMS. It does not automatically shut down hotspot or mobile data.

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
- intended for the Redmi 8A / Android 10 use case
