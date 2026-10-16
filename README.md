# SteelCraft Pro - Android App

Workshop management and customer portal for Steel Railing, Aluminium Fabrication, UPVC, Wooden Balcony, and Interior Works.

---

## 📱 How to Download & Build APK on GitHub (गिटहब से APK कैसे डाउनलोड और बिल्ड करें)

### Method 1: Automatic Build on Push (ऑटोमैटिक बिल्ड)
1. Whenever you push code to GitHub (`main` or `master` branch), GitHub Actions will **automatically start building the APK**.
2. Go to your GitHub repository in your browser.
3. Click on the **Actions** tab at the top.
4. Select the latest workflow run (e.g. `Build Android APK`).
5. Scroll down to the **Artifacts** section at the bottom of the page.
6. Click on **`SteelCraft-Pro-Debug-APK`** to download the ZIP file containing your ready-to-install `.apk`!

---

### Method 2: Manual 1-Click Build (मैनुअल 1-क्लिक बिल्ड)
1. Go to the **Actions** tab in your GitHub repository.
2. In the left sidebar, click on **Build Android APK**.
3. Click the **Run workflow** button on the right side.
4. Select branch (`main`) and click the green **Run workflow** button.
5. In ~2–3 minutes, the build will complete and your APK will be available under **Artifacts**.

---

### Method 3: Tagged Release (वर्जन रिलीज़)
If you create a git tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`), GitHub Actions will automatically publish a **GitHub Release** with the `.apk` directly attached for direct 1-click mobile download without unzipping!

---

## 🛠 Local Build Commands (लोकल बिल्ड कमांड्स)

To build the APK locally:

```bash
# Debug APK:
./gradlew :app:assembleDebug

# Generated APK location:
# app/build/outputs/apk/debug/app-debug.apk
```
