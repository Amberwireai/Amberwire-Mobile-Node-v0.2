# Amberwire Mobile Node Sensor Protocol v0.2

Fresh Android implementation for Amberwire OS. Package identity remains `xyz.amberwire.mobilenode.pixel`.

## Included

- One-time Amberwire enrollment and encrypted credential storage
- Foreground mobile sensor route
- GNSS location, battery, connectivity and Android sensor inventory
- Accelerometer motion-event detection and sensor-fusion metadata
- SQLite offline queue with retrying WorkManager upload
- HTTPS-only batched delivery to `/functions/mobile-node-ingest`
- On-device retention of high-frequency raw readings

## Build

Open the repository in Android Studio, or push it to GitHub and run **Build Amberwire Mobile Node v0.2**. The workflow artifact contains the debug APK.

Use a new Android telemetry-only enrollment code in Amberwire OS. Android 13+ requests notification permission; background location should be granted from Android Settings only when continuous route collection is required.
