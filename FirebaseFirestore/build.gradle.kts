plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {

// Target declarations - add or remove as needed below. These define
// which platforms this KMP module supports.
// See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.firebase_firestore"
        compileSdk = 35
        minSdk = 21

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
            val defFileName = when (target.name) {
                "iosX64" -> "iosX64.def"
                "iosArm64" -> "iosArm64.def"
                "iosSimulatorArm64" -> "iosSimulatorArm64.def"
                "macosX64" -> "macosX64.def"
                "macosArm64" -> "macosArm64.def"
                "tvosX64" -> "tvosX64.def"
                "tvosArm64" -> "tvosArm64.def"
                "tvosSimulatorArm64" -> "tvosSimulatorArm64.def"

                else -> throw IllegalStateException("Unsupported target: ${target.name}")
            }

            val defFile = project.file("interop/$defFileName")
            if (defFile.exists()) {
                cinterops.create("FirebseFirestore") {
                    defFile(defFile)
                    packageName = "io.github.native.kfirebase.firestore"
                }
            } else {
                logger.warn("Def file not found for target ${target.name}: ${defFile.absolutePath}")
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
                // Add KMP dependencies here
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
        val firebaseMessagingHeaders =
            "FirebaseFirestore.framework/Headers/FirebaseFirestore-umbrella.h"

        // Map targets to their respective paths
        val targetToPath = mapOf(
            "iosX64" to "ios-arm64_x86_64-simulator",
            "iosArm64" to "ios-arm64",
            "iosSimulatorArm64" to "ios-arm64_x86_64-simulator",
            "macosX64" to "macos-arm64_x86_64",
            "macosArm64" to "macos-arm64_x86_64",
            "tvosArm64" to "tvos-arm64",
            "tvosX64" to "tvos-arm64_x86_64-simulator",
            "tvosSimulatorArm64" to "tvos-arm64_x86_64-simulator",
            "watchosSimulatorArm64" to "watchos-arm64_x86_64-simulator",
            "watchosX64" to "watchos-arm64_arm64_32",
            "watchosArm32" to "watchos-arm64_arm64_32",
            "watchosArm64" to "watchos-arm64_arm64_32",
        )

        // Helper function to generate header paths
        fun headerPath(target: String): String {
            return interopDir.dir("src/${targetToPath[target]}/$firebaseMessagingHeaders")
                .get().asFile.absolutePath
        }

        // Generate headerPaths dynamically
        val headerPaths = targetToPath.mapValues { (target, _) ->
            headerPath(target)
        }

        // List of targets derived from targetToPath keys
        val iosTargets = targetToPath.keys.toList()

        // Loop through the targets and create the .def files
        iosTargets.forEach { target ->
            val headerPath = headerPaths[target] ?: return@forEach
            val defFile = File(interopDir.get().asFile, "$target.def")

            // Generate the content for the .def file
            val content = """
                language = Objective-C
                package = ${packageName.get()}
                headers = $headerPath
            """.trimIndent()

            // Write content to the .def file
            defFile.writeText(content)
            println("Generated: ${defFile.absolutePath} with headers = $headerPath")
        }
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
