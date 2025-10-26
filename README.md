# Instagram Clone - Android App

A basic Instagram clone built with Java in Android Studio using Firebase as the backend database.

## Features

- **User Registration**: Create new accounts with email, password, username, and full name
- **User Login**: Authenticate users with email and password
- **User Posts**: Create posts with images and captions
- **Likes System**: Like and unlike posts with timing records
- **Comments System**: Add comments to posts with timing
- **Real-time Updates**: All data is stored in Firebase Firestore

## Firebase Setup

1. Create a Firebase project named "instagram-project-272"
2. Download the `google-services.json` file from your Firebase project
3. Place the `google-services.json` file in the `app/` directory of this project
4. Enable the following Firebase services:
   - Authentication (Email/Password)
   - Firestore Database

## Project Structure

```
app/
├── src/main/java/com/example/instagram_project/
│   ├── models/
│   │   ├── User.java
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   └── Like.java
│   ├── utils/
│   │   ├── FirebaseAuthHelper.java
│   │   └── FirebaseFirestoreHelper.java
│   ├── adapters/
│   │   └── PostAdapter.java
│   ├── MainActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   └── HomeActivity.java
└── src/main/res/
    ├── layout/
    ├── drawable/
    └── values/
```

## Data Models

### User
- userId, email, username, fullName
- profileImageUrl, createdAt, updatedAt

### Post
- postId, userId, username, imageUrl, caption
- likes (array), comments (array), createdAt, updatedAt

### Comment
- commentId, postId, userId, username, text
- createdAt, updatedAt

### Like
- likeId, postId, userId, username, createdAt

## Firebase Collections

- `users`: User profiles and information
- `posts`: User posts with images and captions
- `comments`: Comments on posts
- `likes`: Like records for posts

## Getting Started

1. Clone this repository
2. Open the project in Android Studio
3. Add your `google-services.json` file to the `app/` directory
4. Sync the project with Gradle files
5. Run the app on an emulator or device

## Dependencies

- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Glide (for image loading)
- AndroidX libraries

## Note

This is a basic implementation with placeholder UI elements. The focus is on functionality rather than UI design. You can enhance the UI and add more features as needed.
