# Plata, rebuilt from screenshots

A rebuild of Plata's mobile banking app in Compose Multiplatform. It runs on Android, iOS and the web from one shared codebase. I made it from 123 screenshots, without ever using the real app.

**[Try the web demo](https://bancoplata.netlify.app/)**

## What's in the repo

- `shared/src/commonMain` has all the UI, screens, navigation and the design system.
- `androidApp`, `iosApp` and `shared/src/wasmJsMain` are thin entry points for each platform.
- `shared/src/androidHostTest` has screenshot tests for every screen and flow tests that tap through the app.
- [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) covers the components, tokens and commands. [ARCHITECTURE.md](ARCHITECTURE.md) explains how screens, state and navigation fit together.

## Running it

Run everything from this folder. You need JDK 17 or newer and the Android SDK, even for the web build.

```sh
# Android, on a connected device or emulator
./gradlew :androidApp:installDebug

# iOS: open iosApp/iosApp.xcodeproj in Xcode and run the iosApp scheme

# Web, with live reload
./gradlew :shared:wasmJsBrowserDevelopmentRun

# Web, as a static site ready to host (see netlify.toml)
./build-web.sh
```

The web version needs a browser with WebAssembly garbage collection: Chrome or Edge 119+, Firefox 120+ or Safari 18.2+.

## Note

This is a personal study project. I'm not affiliated with Plata, and the Plata name, logo and brand imagery belong to them. The app talks to no real bank and uses made-up data, and any SMS verification code gets you in.
