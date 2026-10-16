# SteelCraft Pro - Android Application

Workshop management & customer showroom for Steel Railing, Aluminium Fabrication, UPVC, Wooden Balcony, and Interior Works.

---

## 🛠 GitHub Actions APK Build Fix (गिटहब पर बिल्ड एरर क्यों आ रहा था और कैसे ठीक हुआ)

### क्या समस्या थी (Root Causes Fixed):
1. **Java Version Mismatch (सबसे बड़ा कारण)**:
   - ऐप में **Android Gradle Plugin (AGP) 9.1.1** और **Gradle 9.3.1** इस्तेमाल हो रहा है, जिसके लिए **Java 21 (JDK 21)** अनिवार्य है।
   - पुराने वर्कफ़्लो में Java 17 सेट था, जिससे GitHub Actions पर एरर आ रहा था: `Android Gradle plugin requires Java 21 to run. You are currently using Java 17.`
   - इसे अब **Java 21 (Temurin JDK 21)** पर अपडेट कर दिया गया है।

2. **Branch Triggering (ब्रांच सपोर्ट)**:
   - पहले वर्कफ़्लो सिर्फ `main` और `master` पर ट्रिगर होता था। अब यह किसी भी ब्रांच पर कोड पुश करने पर अपने आप बिल्ड शुरू कर देगा (`branches: ['**']`)।

3. **Missing Keystore & Environment Files (कीस्टोर और .env एरर)**:
   - वर्कफ़्लो में ऑटोमैटिक `debug.keystore` जेनरेशन और `.env` फ़ाइल सेटअप जोड़ दिया गया है, जिससे गिटहब रनर पर कभी भी मिसिंग फ़ाइल का एरर नहीं आएगा।

4. **Gradle Wrapper Permissions**:
   - `gradlew` के लिए `chmod +x` और मिसिंग `gradle-wrapper.jar` के लिए ऑटोमैटिक फ़ॉलबैक जोड़ दिया गया है।

---

## 📱 How to Download APK from GitHub (गिटहब से APK कैसे डाउनलोड करें)

### तरीका 1: अपने आप बिल्ड (Automatic on Push)
1. जब भी आप AI Studio से **Push to GitHub** करते हैं या कोड GitHub पर पुश करते हैं:
2. अपने GitHub रिपॉजिटरी में जाएँ।
3. ऊपर दिए गए **Actions** टैब पर क्लिक करें।
4. आपको सबसे ऊपर **"Build & Package Android APK"** रन होता दिखेगा (हरा टिक ✅ लगने तक 2 मिनट प्रतीक्षा करें)।
5. उस रन पर क्लिक करें और पेज के नीचे **Artifacts** सेक्शन में जाएँ।
6. **`SteelCraft-Pro-APK`** पर क्लिक करें। एक ZIP डाउनलोड होगी, जिसे अनज़िप करने पर आपको तुरंत इंस्टॉल होने वाली `SteelCraft-Pro-debug.apk` मिल जाएगी!

---

### तरीका 2: मैनुअल 1-क्लिक रन (Manual Workflow Dispatch)
1. GitHub रिपॉजिटरी में **Actions** टैब खोलें।
2. बायीं (Left) तरफ **"Build Android APK"** पर क्लिक करें।
3. दायीं (Right) तरफ **"Run workflow"** बटन पर क्लिक करें।
4. अपनी ब्रांच चुनकर हरे रंग के **"Run workflow"** बटन को दबाएं।
5. 2-3 मिनट में बिल्ड सफल होने के बाद **Artifacts** में जाकर APK डाउनलोड कर लें।

---

### तरीका 3: डायरेक्ट डाउनलोड रिलीज (Git Tag Release)
यदि आप गिटहब पर टैग पुश करते हैं (उदाहरण: `v1.0.0`):
- GitHub Actions अपने आप **Releases** सेक्शन में डायरेक्ट `.apk` फ़ाइल अटैच कर देगा, जिसे आप बिना अनज़िप किए सीधे मोबाइल पर डाउनलोड और इंस्टॉल कर सकते हैं!

---

## 💻 Local Testing (लोकल कमांड्स)

यदि आप अपने कंप्यूटर पर बिल्ड करना चाहते हैं:
```bash
./gradlew :app:assembleDebug
```
तैयार APK यहाँ मिलेगी:
`app/build/outputs/apk/debug/app-debug.apk`
