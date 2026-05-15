# Namma-HomeStay (Hospitality Portal) 🌿

**Namma-HomeStay** is an industrial-grade "Simplified Host Portal" designed for small rural and coastal home-stays. It bridges the digital literacy gap for farmers and homemakers by using **GenAI** to create professional marketing content and manage their business with ease.

---

## 🚀 Key Features

### 🤖 GenAI "Magic Wand" (Google Gemini)
Hosts can type simple notes about their home, and with one tap (✨), the integrated **Google Gemini 1.5 Flash AI** rewrites it into a poetic, welcoming, and professional description to attract international travelers.

### 👥 Multi-Role Ecosystem
- **Host Mode:** Secure login via **Firebase Auth**. Manage multiple homestay spots, update "Today's Menu" in under 1 minute, and handle guest inquiries. 
- **Traveler Mode:** A public discovery portal where guests can search for stays, see real-time menus, find "Secret Spots" via the **Local Guide**, and leave public **Ratings & Reviews**.

### 🛠️ Technical implementation
- **UI:** Material 3 with a warm coastal color palette.
- **Database:** Firebase Firestore (Real-time data isolation between hosts).
- **Auth:** Firebase Authentication for secure host accounts.
- **Media:** Cloudinary integration for professional image hosting.
- **AI:** Google Generative AI SDK (Gemini).

---

## 🛠️ Setup Guide for Developers

Since this project uses private API keys for security, follow these steps to run the app:

### 1. Gemini AI Setup (Required for AI Features)
1. Get a free API Key from [Google AI Studio](https://aistudio.google.com/).
2. Open the `local.properties` file in your project root.
3. Add the following line at the bottom:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```

### 2. Firebase Setup (Required for Auth & Database)
1. Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name: `com.namma.homestay`.
3. Download the `google-services.json` file and place it in the `app/` directory.
4. Enable **Firestore Database**, **Authentication** (Email/Password), and **Storage**.

### 3. Build & Run
1. Open in Android Studio.
2. Click **Sync Project with Gradle Files** (Elephant icon).
3. Run on a physical device or emulator.

---

## 🌟 Success Criteria Met (Industrial Standards)
- ✅ **Security:** Host 1 cannot see or edit Host 2's private data.
- ✅ **GenAI Impact:** Directly addresses the PRD goal of increasing Digital Literacy for rural hosts.
- ✅ **UX:** Designed for "non-tech-savvy" users with high-contrast buttons and simple flows.
- ✅ **Social Proof:** Built-in review system to build trust in the eco-tourism market.

---
*Developed as a Top Project for Android App Development using GenAI.*
