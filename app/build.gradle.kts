plugins {
    id("com.android.application")
}

android {
    namespace = "com.goovat.poweralarm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.goovat.poweralarm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}


dependencies {
    implementation("androidx.activity:activity-ktx:1.10.1")
}
