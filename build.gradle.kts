plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.maven.publish)

}
