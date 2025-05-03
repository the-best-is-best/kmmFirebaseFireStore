
<h1 align="center">KFirebaseFirestore</h1><br>
<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=21" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/iOS-13%2B-blue.svg?logo=apple" alt="iOS 13+ Badge" />

<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

<br>

### KFirebaseFirestore is a Kotlin Multiplatform library that provides a unified, idiomatic Kotlin interface for interacting with Firebase Firestore across Android, iOS, and other supported platforms. It simplifies data access, real-time updates, and Firestore queries using Kotlin coroutines and flows

---

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/kfirebase-firestore)](https://central.sonatype.com/artifact/io.github.the-best-is-best/kfirebase-firestore)

KFirebaseFirestore is available on `mavenCentral()`.

---

## Installation

```kotlin
implementation("io.github.the-best-is-best:kfirebase-firestore:1.1.0")
```

### androidMain

```kotlin
KAndroidFirebaseCore.initialize(this)
```

### iOS (Using Swift Package Manager - SPM)

Make sure to add Firebase as a dependency using Swift Package Manager (SPM).

1. Open your Xcode project.
2. Go to **File > Add Packages...**
3. Use this URL: `https://github.com/firebase/firebase-ios-sdk`
4. Add `FirebaseFirestore` to your iOS app target.

---

## Usage

```kotlin
val firestore = KFirebaseFirestore()

val result = firestore.addDocument(
    collection = "users",
    documentId = "user123",
    data = mapOf("name" to "Alice", "age" to 30)
)
```

---

## API

```kotlin
package io.github.firebase_firestore

expect class KFirebaseFirestore() {

    suspend fun addDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ): Result<Boolean>

    suspend fun getDocuments(
        collection: String
    ): Result<List<Map<String, Any?>>>

    suspend fun getDocumentById(
        collection: String,
        documentId: String
    ): Result<Map<String, Any?>>

    fun listenToCollection(
        collection: String,
    ): Flow<Result<List<Map<String, Any?>>>>

    suspend fun queryDocuments(
        collection: String,
        filters: List<Map<String, Comparable<*>>> = emptyList(),
        orderBy: String? = null,
        limit: Long? = null
    ): Result<List<Map<String, Any?>>>

    suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ): Result<Boolean>

    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit>
}
```

---

## License

This project is licensed under the [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0).

---
