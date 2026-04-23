# ✦ KotlinMode - Instagram-Style Social Media Platform

KotlinMode is a high-performance, full-stack social media application inspired by Instagram. It features a modern Android frontend built with Jetpack Compose and a robust Node.js backend.

## 🚀 Key Features

### 🎬 Media & Reels
- **Instagram-Style Reels**: A full-screen vertical video feed with smooth scrolling and auto-play logic.
- **Multi-Media Feed**: Support for both high-quality images and videos in the main home feed.
- **Smart Upload Pipeline**: Integrated Cloudinary pipeline with auto-detection for images vs videos and optimized processing.

### 💬 Social Interactions
- **Real-time Chat**: Instant messaging powered by Socket.io for low-latency communication.
- **Engagement**: Like, comment, and reply functionality on all posts and reels.
- **Follow System**: Complete user following/follower system to build your network.
- **Search**: Discover users and content through an integrated search engine.

### 👤 Profile & Auth
- **Secure Authentication**: JWT-based login and registration system.
- **Dynamic Profiles**: Editable profiles with bio, profile pictures, and a grid view of all user posts.
- **Content Management**: Users can delete their own posts directly from the feed or profile grid.

---

## 🛠 Tech Stack

### Frontend (Android)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI)
- **Navigation**: Compose Navigation with a dynamic Bottom Bar.
- **Video Engine**: AndroidX Media3 (ExoPlayer) for hardware-accelerated video streaming.
- **Image Loading**: Coil for optimized image caching and memory management.
- **Networking**: Retrofit 2 + OkHttp for RESTful API communication.
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern.

### Backend (Node.js)
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB with Mongoose ODM.
- **Real-time**: Socket.io for bi-directional chat communication.
- **Media Storage**: Cloudinary (Cloud-based image and video management).
- **Authentication**: JSON Web Tokens (JWT).
- **Logging**: Custom Winston-based logger for production monitoring.

---

## 📂 Project Structure

### Backend (`/backend`)
- `controllers/`: Business logic for posts, users, and chat.
- `models/`: Mongoose schemas for Users, Posts, and Conversations.
- `routes/`: API endpoint definitions.
- `middleware/`: Authentication and Multer/Cloudinary upload configurations.
- `socket/`: Socket.io event handlers for real-time messaging.

### Android (`/android`)
- `ui/`: Compose screens (Feed, Reels, Profile, Chat, Auth).
- `viewmodel/`: State management for all screens.
- `repository/`: Data layer abstraction.
- `network/`: Retrofit API interface and network models.
- `model/`: Data classes representing backend entities.

---

## ⚙️ Installation & Setup

### 1. Backend Setup
1. Navigate to the `backend` directory.
2. Install dependencies: `npm install`.
3. Create a `.env` file with the following variables:
   - `PORT`, `MONGO_URI`, `JWT_SECRET`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
4. **Push Notifications (Optional)**:
   - Go to [Firebase Console](https://console.firebase.google.com/).
   - Generate a new Private Key for your service account.
   - Save it as `serviceAccountKey.json` in the `backend/` root directory.
5. Start the server: `npm run dev`.

### 2. Android Setup
1. Open the `android` folder in Android Studio.
2. Update the `BASE_URL` in `Constants.kt` to point to your server IP (e.g., `http://192.168.1.XX:5000/api/`).
3. Sync Gradle and run the app on an emulator or physical device.

---

## 📦 Building for Production
To generate a test APK for other devices:
1. Open a terminal in the `android` folder.
2. Run: `.\gradlew assembleDebug`
3. Locate the APK at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🛠 Maintenance & Debugging
- **Backend Logs**: Check the console for Winston logger output.
- **Android Logs**: Filter Logcat by `VideoPlayer`, `ReelsPager`, or `ApiService` for specific feature debugging.
- **Media**: Videos are automatically converted and optimized by Cloudinary upon upload.

---
*Created by the KotlinMode Development Team.*
