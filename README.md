# CircleUp: Next-Gen Android Messaging App


**Individual Final Year Project for BS Information Technology**
**Session:** 2021-2025

**Developed By:** Sana Mehboob
**Project Supervisor:** Maam Rabia Aslam

---

## Project Description

CircleUp is a secure, feature-rich, real-time messaging application developed as my Final Year Project to address limitations in existing communication platforms. It provides a secure, private, and user-friendly experience by focusing on flexible user identification, robust offline access, unique features like scheduled and temporary messages, and end-to-end encryption.

Key issues addressed by CircleUp include:
*   Overcoming mandatory phone number requirements for registration.
*   Implementing advanced features like message scheduling.
*   Providing reliable offline access to chat history.
*   Ensuring a high level of security and privacy through encryption.

CircleUp aims to offer a comprehensive messaging solution that prioritizes accessibility, security, and efficiency for seamless communication.

## Features

*   **Multiple Authentication Options:** Sign up and login via Email/Password and Google Authentication.
*   **Real-time Messaging:** One-to-one private chats and Group chats.
*   **End-to-End Encryption (E2EE):** Messages are encrypted using AES-256 & RSA for secure communication between sender and receiver.
*   **Offline Access:** Access chat history and view messages locally using Room Database when the internet is unavailable. Messages sync automatically when online.
*   **Scheduled Messages:** Compose messages to be sent automatically at a later date and time.
*   **Temporary Chat Rooms:** Create temporary chat rooms with messages that disappear after a set expiry time.
*   **Friend Request & Contact System:** Send, accept, and manage friend requests before initiating chats.
*   **User Profile & Account Management:** View and update user profiles, manage your account settings, and delete your account.
*   **Push Notifications:** Receive real-time notifications for new messages and friend requests via Firebase Cloud Messaging (FCM).
*   **Admin Panel (Note):** An Admin Panel was part of the original project scope for user management (enable/disable/delete accounts). **The code for the Admin Panel module is maintained in a separate project and is not included in this repository.** This repository contains the user-facing Android application.

## Technologies Used

*   **Programming Language:** Kotlin (with Java components)
*   **Frontend:** Android SDK (Java & XML)
*   **Backend:** Google Firebase (Realtime Database, Authentication, Storage, Cloud Functions)
*   **Local Database:** Room Database (Jetpack)
*   **Networking & Sync:** Firebase Realtime Database, WorkManager
*   **Push Notifications:** Firebase Cloud Messaging (FCM), OneSignal
*   **Encryption:** Bouncy Castle / `javax.crypto` (for AES-256 & RSA)
*   **Other Libraries:** Base64 (for image handling), Material Components, etc.
*   **Development Environment:** Android Studio 
*   **Build Tool:** Gradle 

## System Architecture

The CircleUp application follows a **Two-Tier Architecture**:

1.  **Presentation & Logic Tier (Frontend):** The Android application handling UI, user interactions (chat, friend requests, groups), and local logic.
2.  **Data Layer (Backend & Local Storage):** Primarily uses **Firebase Realtime Database** for real-time cloud storage, synchronization, and authentication. **Room Database** is used for efficient local storage of messages for offline access.

A **Security Layer** ensures End-to-End Encryption for messages and utilizes Firebase Security Rules for access control.

The application communicates with Firebase for real-time data sync and authentication, and manages offline data persistence locally with Room DB.

## Setup and Installation

To get the CircleUp project up and running on your local machine:

1.  **Prerequisites:**
    *   Android Studio installed.
    *   Git installed.
    *   An Android device or emulator (Android 8.1 Oreo or above, with at least 4GB RAM recommended for emulator).
    *   A Google account to create a Firebase project.

2.  **Clone the repository:**
    ```bash
    git clone https://github.com/sana00199/CircleUp.git
    ```
    Navigate into the cloned directory:
    ```bash
    cd CircleUp
    ```

3.  **Open the project in Android Studio:**
    *   Open Android Studio.
    *   Select `File` -> `Open` and navigate to the `CircleUp` folder you just cloned.

4.  **Set up Firebase:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/).
    *   Create a new Firebase project (or use an existing one).
    *   Add an Android app to your Firebase project. Ensure the Android package name matches the one in your project (`com.sana.circleup`).
    *   Download the `google-services.json` configuration file.
    *   Place the `google-services.json` file into the `app` directory of your Android Studio project (`E:/CircleUp/app/`).
    *   In the Firebase Console, enable the Authentication methods you want to support (Email/Password, Google Sign-In).
    *   Set up the Firebase Realtime Database. Configure security rules as needed (refer to documentation for details).
    *   Set up Firebase Cloud Messaging (FCM).

5.  **Add Service Account Key (Crucial Security Step):**
    *   The `serviceAccountKey.json` file containing Firebase Service Account credentials is **NOT** included in this public repository for security reasons.
    *   Go to your Firebase Console (`Project settings` -> `Service accounts`).
    *   Click "Generate new private key". A JSON file will download.
    *   Rename the downloaded file to `serviceAccountKey.json` (if it's not already named that).
    *   Place this `serviceAccountKey.json` file in the `app/src/main/assets/` directory of your **local** project (`E:/CircleUp/app/src/main/assets/`).
    *   **Ensure this file is NOT committed to your Git repository.** (It should already be in the `.gitignore` file, but double-check).

6.  **Sync and Build:**
    *   Sync your project with Gradle files (`File` -> `Sync Project with Gradle Files`).
    *   Build the project (`Build` -> `Make Project`).

7.  **Run the App:**
    *   Connect a physical Android device or start a Virtual Device (Emulator).
    *   Select the target device from the dropdown in the toolbar.
    *   Click the Run button (green play icon).

## App Flow & Usage


*   **Login/Registration:** Users can create new accounts with Email/Password or Google Sign-In. Existing users can log in. The app verifies credentials with Firebase Authentication.
*   **Finding Friends:** Users can search for other users and send friend requests.
*   **Handling Requests:** Users receive and can accept or reject incoming friend requests. Accepted requests add the user to the contacts list.
*   **Messaging:** Users can initiate one-to-one chats with contacts or participate in group chats. Messages are encrypted end-to-end. Offline messages are supported via Room Database.
*   **Scheduled Messages:** Users can compose messages and set a time for them to be sent automatically.
*   **Temporary Chat Rooms:** Create temporary chat rooms with disappearing messages.
*   **User Profile:** Users can view and update their profile information (username, status).
*   **Navigation:** The app includes a navigation drawer for accessing features like Create Group, Scheduled Messages, Drafts, Temporary Chat Room, Settings, Logout, and Delete Account.


## Acknowledgements

I would like to express my gratitude to Allah Almighty, my supportive family, my project supervisor **Maam Rabia Aslam**, and the department faculty for their invaluable guidance and support throughout this Final Year Project.

## About the Author

Sana Mehboob - Punjab University, BSIT 2021-2025]


