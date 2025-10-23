import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
    id("maven-publish")
    id("signing")
}




apply(plugin = "maven-publish")
apply(plugin = "signing")


tasks.withType<PublishToMavenRepository> {
    val isMac = getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}


extra["packageNameSpace"] = "io.github.firebase_firestore"
extra["groupId"] = "io.github.the-best-is-best"
extra["artifactId"] = "kfirebase-firestore"
extra["version"] = "2.1.0"
extra["packageName"] = "KFirebaseFirestore"
extra["packageUrl"] = "https://github.com/the-best-is-best/kmmFirebaseFireStore"
extra["packageDescription"] = "KFirebaseFirestore is a Kotlin Multiplatform library that provides a unified, idiomatic Kotlin interface for interacting with Firebase Firestore across Android, iOS, and other supported platforms. It simplifies data access, real-time updates, and Firestore queries using Kotlin coroutines and flows."
extra["system"] = "GITHUB"
extra["issueUrl"] = "https://github.com/the-best-is-best/kmmFirebaseFireStore/issues"
extra["connectionGit"] = "https://github.com/the-best-is-best/kmmFirebaseFireStore.git"

extra["developerName"] = "Michelle Raouf"
extra["developerNameId"] = "MichelleRaouf"
extra["developerEmail"] = "eng.michelle.raouf@gmail.com"


mavenPublishing {
    coordinates(
        extra["groupId"].toString(),
        extra["artifactId"].toString(),
        extra["version"].toString()
    )

    publishToMavenCentral(true)
    signAllPublications()

    pom {
        name.set(extra["packageName"].toString())
        description.set(extra["packageDescription"].toString())
        url.set(extra["packageUrl"].toString())
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        issueManagement {
            system.set(extra["system"].toString())
            url.set(extra["issueUrl"].toString())
        }
        scm {
            connection.set(extra["connectionGit"].toString())
            url.set(extra["packageUrl"].toString())
        }
        developers {
            developer {
                id.set(extra["developerNameId"].toString())
                name.set(extra["developerName"].toString())
                email.set(extra["developerEmail"].toString())
            }
        }
    }

}


signing {
    useGpgCmd()
    sign(publishing.publications)
}

kotlin {

// Target declarations - add or remove as needed below. These define
// which platforms this KMP module supports.
// See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.firebase_firestore"
        compileSdk = 36
        minSdk = 23

    }

// For iOS targets, this is also where you should
// configure native binary output. For more information, see:
// https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

// A step-by-step guide on how to include this library in an XCode
// project can be found here:
// https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "FirebaseFirestoreKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
        tvosArm64(),
        tvosX64(),
        tvosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = xcfName
            isStatic = true

        }

        target.compilations.getByName("main") {
            val firCore by cinterops.creating {
                defFile("/Users/michelleraouf/Desktop/kmm/kmmFirebaseFireStore/FirebaseFirestore/interop/firFirstore.def")
                packageName = "io.github.native.kfirebase.firestore"
            }

        }

    }


// Source set declarations.
// Declaring a target automatically creates a source set with the same name. By default, the
// Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
// common to share sources between related targets.
// See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kfirebase.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.


                implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.13.0"))

                // Firestore dependency
                implementation(libs.firebase.firestore.ktx)
            }
        }



        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}

abstract class GenerateDefFilesTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val interopDir: DirectoryProperty

    @TaskAction
    fun generate() {
        // Ensure the directory exists
        interopDir.get().asFile.mkdirs()

        // Constants
        val firebaseFirstoreHeaders =
            "FirebaseFirestore.h"

        // Map targets to their respective paths
//        val targetToPath = mapOf(
//            "iosX64" to "ios-arm64_x86_64-simulator",
//            "iosArm64" to "ios-arm64",
//            "iosSimulatorArm64" to "ios-arm64_x86_64-simulator",
//            "macosX64" to "macos-arm64_x86_64",
//            "macosArm64" to "macos-arm64_x86_64",
//            "tvosArm64" to "tvos-arm64",
//            "tvosX64" to "tvos-arm64_x86_64-simulator",
//            "tvosSimulatorArm64" to "tvos-arm64_x86_64-simulator",
//            "watchosSimulatorArm64" to "watchos-arm64_x86_64-simulator",
//            "watchosX64" to "watchos-arm64_arm64_32",
//            "watchosArm32" to "watchos-arm64_arm64_32",
//            "watchosArm64" to "watchos-arm64_arm64_32",
//        )

        // Helper function to generate header paths
        fun headerPath(): String {
            return interopDir.dir("src/$firebaseFirstoreHeaders")
                .get().asFile.absolutePath
        }

        val defFile = File(interopDir.get().asFile, "firFirstore.def")

        // Generate the content for the .def file
        val content = """
                language = Objective-C
                package = ${packageName.get()}
                headers = ${headerPath()}
            """.trimIndent()

        // Write content to the .def file
        defFile.writeText(content)
    }
}
// Register the task within the Gradle build
tasks.register<GenerateDefFilesTask>("generateDefFiles") {
    packageName.set("io.github.native.kfirebase.firestore")
    interopDir.set(project.layout.projectDirectory.dir("interop"))
}

tasks.named("build") {
    dependsOn(tasks.named("generateDefFiles"))
}
