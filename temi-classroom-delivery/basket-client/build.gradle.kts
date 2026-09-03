plugins {
    id("com.android.library")
}

android {
    namespace = "kr.hs.gwangyang.temidelivery.basket"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Keep JVM test output out of the Korean workspace path for the Windows test worker.
layout.buildDirectory.set(
    file("${System.getProperty("user.home")}/.gradle/temi-classroom-delivery/basket-client"),
)

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
