import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
//    id("com.google.firebase.crashlytics")
}

android {
    namespace = "org.company.app.androidApp"
    compileSdk = 36

    packaging {
        resources {
            pickFirsts += "**/*.cvr"
        }
    }


    defaultConfig {
        minSdk = 24
        targetSdk = 36

        applicationId = "org.company.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}



kotlin {

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(libs.androidx.activityCompose)
    implementation(project(":FirebaseFirestore"))


//
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.lifecycle.lifecycle.runtime.compose)


}
