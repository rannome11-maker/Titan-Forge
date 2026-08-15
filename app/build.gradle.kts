plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.dangerdan.titanforge"
    compileSdk = 35
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        applicationId = "com.dangerdan.titanforge"
        minSdk = 26
        targetSdk = 35
        versionName = "0.4.0"
        versionCode = 5
        buildConfigField("String", "SUPABASE_URL", "\"https://tkclbwoqqzmneroesmjq.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_mSBzDdbVbYmDn5JQyODHaA_YQNjI_ox\"")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
