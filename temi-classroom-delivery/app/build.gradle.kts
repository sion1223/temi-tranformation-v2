plugins {
    id("com.android.application")
}

android {
    namespace = "kr.hs.gwangyang.temidelivery"
    compileSdk = 37

    defaultConfig {
        applicationId = "kr.hs.gwangyang.temidelivery"
        minSdk = 23
        // The target robot runs Android 11 (API 30). Keeping targetSdk at 30 avoids
        // introducing newer platform behavior changes on this sideloaded robot app.
        targetSdk = 30
        versionCode = 7
        versionName = "0.4.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        // This is an Android 11 temi appliance app distributed by ADB, not Google Play.
        disable += "ExpiredTargetSdkVersion"
    }
}

// The Windows test worker cannot load JVM test classes from this workspace's
// Korean parent path. Keep an opt-in ASCII build root for local unit tests while
// preserving app/build as the normal APK output directory.
if (providers.gradleProperty("asciiBuild").isPresent) {
    layout.buildDirectory.set(
        file("${System.getProperty("user.home")}/.gradle/temi-classroom-delivery/app"),
    )
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":ai-guide-client"))
    implementation(project(":basket-client"))
    implementation("com.robotemi:sdk:1.138.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
}
