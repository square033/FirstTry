plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.firsttry"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.firsttry"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // ✅ 괄호 사용
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation(files("libs/minewBeaconAdmin.jar"))


    implementation("org.greenrobot:eventbus:3.0.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation ("org.apache.commons:commons-math3:3.6.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}