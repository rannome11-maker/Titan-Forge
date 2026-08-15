plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.blackforge.realm"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.blackforge.realm"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes { release { isMinifyEnabled = false; signingConfig = signingConfigs.getByName("debug") } }
    packaging { jniLibs { useLegacyPackaging = true } }
}

dependencies {
    implementation("io.github.aatricks:llmedge:0.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
