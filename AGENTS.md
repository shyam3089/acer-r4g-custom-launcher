# AGENTS.md

- Keep the launcher native Kotlin with Android Views and RecyclerView; do not add Compose or WebView.
- Preserve the single-process, offline-first design. Do not add analytics, ads, remote feeds, or background services.
- Optimize for Android 14, 32-bit ARMv7, 2 GB RAM, D-pad input, and a 1920x1080 UI.
- HOME must render from local state. Package discovery and icon work must stay off the main thread where practical.
- Removing an app from Home means unpinning only. Never uninstall, disable, or alter another package.
- Keep package visibility narrow and permissions minimal.
- Run focused unit tests and `assembleDebug` after relevant changes. Do not deploy to a device unless explicitly asked.
