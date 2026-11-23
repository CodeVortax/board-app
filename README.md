# Board – Touch Pin Board App

This repository contains a **Kotlin + Jetpack Compose** Android app that works like a smart board where you can place coloured pins, erase them, change background styles, and navigate between multiple pages.

## Features (MVP)
- Floating tool panel with colour picker, size slider, **Add Pin** and **Delete All** buttons.
- Page navigation (Back / Forward) – each page keeps its own pins.
- Simple pin data class (`Pin`) – extendable with shape, eraser, ruler, etc.
- Placeholder slots for future features: background gradients/dots, ruler overlay, circuit‑builder widgets.
- GitHub Actions workflow that builds a **debug APK** on every push.

## Project Structure
```
me2008.board/
├─ app/
│  ├─ src/main/java/me2008/board/
│  │   ├─ MainActivity.kt          # UI entry point
│  │   └─ ui/theme/Theme.kt       # Light theme
│  └─ src/main/AndroidManifest.xml
├─ build.gradle.kts                # Root Gradle script
├─ settings.gradle.kts
└─ .github/workflows/debug-build.yml
```

## Getting Started
1. **Initialize a Git repository**
   ```bash
   cd /data/data/com.termux/files/home/me2008.board
   git init
   git add .
   git commit -m "Initial commit – Board app skeleton"
   ```
2. **Create a GitHub repository** (e.g., `me2008/board`).
3. **Add the remote and push**
   ```bash
   git remote add origin git@github.com:me2008/board.git
   git branch -M main
   git push -u origin main
   ```
4. **GitHub Actions** – The workflow located at `.github/workflows/debug-build.yml` will automatically run on each push and produce a `debug.apk` artifact.
5. **Download the APK** from the Actions run page (scroll to *Artifacts*).
6. **Install on a device** (make sure `adb` is installed and the device is connected):
   ```bash
   adb install -r debug.apk
   ```
7. **Launch** – the app appears as **Board** on the device.

## Extending the App
- **Eraser tool** – add a toggle in `ToolPanel` that switches the touch handler to remove pins.
- **Background styles** – create a `BackgroundPicker` composable and store the selected style in a `MutableState`.
- **Ruler overlay** – implement a `RulerOverlay` composable that draws measurement lines on top of the canvas.
- **Circuit builder** – design custom composables for resistors, voltage sources, etc., and store them in a separate list per page.

## License
MIT – feel free to fork and improve.
