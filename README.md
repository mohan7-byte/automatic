# Automatic — Redmi 8A Headless Controller

Personal-use Android 10 helper for a Redmi 8A with a broken / ghost-touch display.

## Design rule
The automation engine never intentionally turns the display on. Setup is done once through scrcpy; after that, triggers run headlessly.

## Features
- Volume Up double press → hotspot toggle
- Volume Up hold (~700 ms) → mobile data toggle
- Volume Down double press → call configured second number
- Volume Down hold (~700 ms) → call Dad
- Shake → hotspot toggle
- Night / morning schedule configurable from the setup screen
- Authorized SMS commands: `onhotspot`, `#hotspot on`, `offhotspot`, `ondata`, `offdata`, `calldad`, `callme`, `status`
- Battery SMS alerts at 15%, 10%, 5%, and 2%; no automatic shutdown from battery alerts
- Persistent foreground service, boot restart, no WebView

## Important Android / MIUI limitation
Android 10 does not provide an unrestricted public third-party API for silently switching tethering or mobile data. This build includes shell command fallbacks (`cmd connectivity tether ...`, `cmd phone data ...`, `svc data ...`) but their success depends on the privileges available on the device. Ordinary app permissions alone are not equivalent to root / privileged system permission.

For a personal Redmi 8A, use ADB and device-specific privileges as needed. Accessibility is used only for hardware-key event capture; it is not used to wake the display or automate touch input.

## Install / setup
1. Build the project with Android Studio / Gradle.
2. Install the APK on the Redmi 8A.
3. Use scrcpy for the one-time setup screen.
4. Grant SMS, call, vibration, and notification permissions when requested.
5. Enable Automatic under Settings → Accessibility → Installed services so the volume buttons can be captured.
6. Disable MIUI battery optimization / enable Auto-start for Automatic so the foreground service is not aggressively stopped.
7. Configure Dad's number, your other number, authorized SMS number, night time, morning time, and shake mode.
8. Verify each feature while the display is OFF before relying on it.

## SMS commands
Only the configured authorized SMS number is accepted.

- `onhotspot` or `#hotspot on`
- `offhotspot`
- `ondata`
- `offdata`
- `calldad`
- `callme`
- `status`

The app sends a reply for remote commands. `status` reports battery and configured schedule.

## Haptic feedback
- Hotspot ON success → one long vibration
- Hotspot OFF → two short vibrations
- Data action → one short vibration
- Calls → no haptic feedback
- Scheduled/SMS battery actions do not wake the display

## Build target
- Java / native Android Views
- minSdk 26
- targetSdk 28
