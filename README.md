# Namma-HomeStay: AI-Powered Hospitality Portal 🌿
### *Bridging the Digital Divide for Rural Homestays via Generative AI*

---

## 📋 Table of Contents
1. [Project Overview](#-project-overview)
2. [Problem Statement](#-problem-statement)
3. [Key Features](#-key-features)
4. [Technical Stack](#-technical-stack)
5. [Architecture & Design Patterns](#-architecture--design-patterns)
6. [Folder Structure](#-folder-structure)
7. [Setup & Installation](#-setup--installation)
8. [Usage Guide](#-usage-guide)
9. [Development Roadmap](#-development-roadmap)
10. [Contact & Credits](#-contact--credits)

---

## 📖 Project Overview
**Namma-HomeStay** is an industrial-grade Android application designed to empower non-tech-savvy homestay owners in rural and coastal regions. By leveraging **Google Gemini AI**, the platform automates professional content creation, allowing hosts to compete in the global tourism market with zero marketing expertise.

**Target Audience:** Rural farmers, coastal homeowners, and eco-conscious travelers seeking authentic experiences.

---

## 🎯 Problem Statement
Rural hospitality providers often deliver world-class experiences but suffer from:
*   **The Content Gap:** Inability to write poetic, professional, or English-language descriptions.
*   **Operational Complexity:** Modern booking platforms are too complex for users with low digital literacy.
*   **Dynamic Information:** Difficulty in publishing real-time updates like "Today's Special Menu" or "Local Secret Spots."

Namma-HomeStay solves this by providing a "Simplified Host Portal" where AI handles the complexity of marketing.

---

## ✨ Key Features

### 🤖 1. GenAI "Magic Wand" (Content Automation)
*   **Technology:** Google Gemini 1.5 Flash.
*   **Function:** Transforms rough, fragmented notes (e.g., "beach near, fish food, quiet") into professional, SEO-friendly storytelling descriptions.
*   **Impact:** Empowers hosts to have a professional digital presence instantly.

### 👥 2. Multi-Role Ecosystem
*   **Host Mode:** Dedicated dashboard for property management, daily menu updates, and guest inquiry tracking.
*   **Traveler Mode:** A discovery portal where users can find authentic stays, view real-time menus, and read verified reviews.

### 🥘 3. Real-time Daily Menu & Operations
*   Allows hosts to update their kitchen offerings in under 60 seconds.
*   Uses **Firebase Firestore** for instant synchronization across all traveler devices.

### 📍 4. Local Guide (Hidden Gems)
*   A curated section where hosts share "Secret Spots" (hidden waterfalls, private viewpoints) not available on standard maps, adding unique value to the traveler’s journey.

---

## 🛠 Technical Stack
| Category | Technology | Detail |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | Modern, type-safe programming |
| **UI Framework** | **Jetpack Compose** | Material 3 Declarative UI |
| **AI Engine** | **Google Gemini** | Generative AI SDK (1.5 Flash) |
| **Backend** | **Firebase** | Auth, Firestore, Storage |
| **Media Hosting** | **Cloudinary** | Industrial Image Optimization |
| **Networking** | **Coroutines & Flow** | Asynchronous Reactive Programming |
| **Image Loading** | **Coil** | Efficient Image Caching |

---

## 🏗 Architecture & Design Patterns
The project adheres to **Clean Architecture** and **MVVM (Model-View-ViewModel)** to ensure a high "Evidence-Based" score:
*   **Separation of Concerns:** UI logic is decoupled from business logic.
*   **Repository Pattern:** Abstracts data sources (Firestore vs. Local Fake Data) for better testability.
*   **App Container Pattern:** Manual Dependency Injection for managing service lifecycles.

---

## 📂 Folder Structure
```text
Namma-HomeStay/
├── app/
│   ├── src/main/java/com/namma/homestay/
│   │   ├── data/                 # Data Layer
│   │   │   ├── model/            # Domain Models (HomeProfile, Menu, etc.)
│   │   │   ├── firestore/        # Firebase Implementation
│   │   │   └── repo/             # Repository Abstractions
│   │   ├── ui/                   # Presentation Layer
│   │   │   ├── screens/          # Compose Screen Functions
│   │   │   ├── components/       # Reusable UI Widgets
│   │   │   ├── navigation/       # NavGraph & Route Definitions
│   │   │   └── theme/            # Material 3 Styling (Color, Type)
│   │   └── MainActivity.kt       # Application Entry Point
│   ├── src/main/res/             # Android Resources (Layouts, Drawables)
│   └── build.gradle.kts          # App-level Build Configuration
├── build.gradle.kts              # Project-level Build Configuration
└── local.properties              # Private API Key Storage (Excluded from Git)
```

---

## 🚀 Setup & Installation

### 1. Prerequisites
*   Android Studio Jellyfish | 2023.3.1 or newer.
*   JDK 17+.
*   A Google AI Studio API Key.

### 2. Cloning the Project
```bash
git clone https://github.com/shodhanshetty12/Namma-HomeStay.git
cd Namma-HomeStay
```

### 3. API Configuration
Open `local.properties` in the root folder and add:
```properties
GEMINI_API_KEY=your_key_here
```

### 4. Build and Run
1. Sync project with Gradle files.
2. Select an Emulator or Physical Device.
3. Click **Run** or use:
```bash
./gradlew installDebug
```

---

## 📈 Usage Guide
1. **Login:** Use Host Mode to create your profile.
2. **AI Magic:** Enter keywords in the description field and tap the ✨ icon.
3. **Menu Update:** Go to the Menu tab to post today's availability.
4. **Switch Roles:** Use the settings menu to view the app as a Traveler.

---

## 🛣 Development Roadmap
*   [ ] **Voice-to-Text:** Support for local dialects (Tulu/Kannada) for menu updates.
*   [ ] **UPI Integration:** Enable direct advance bookings via payment gateway.
*   [ ] **Offline Mode:** Cache property details for areas with poor connectivity.

---

## 🤝 Contact & Credits
**Developer:** Shodhan Shetty  
**GitHub:** [shodhanshetty12](https://github.com/shodhanshetty12)  
**Project Category:** Android App Development (GenAI Integrated)

*Developed as a capstone project demonstrating the integration of Generative AI in Hospitality.*
