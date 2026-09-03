plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

// Gradle's Windows test worker cannot load classes from this workspace's Korean parent
// path. Keep only the pure-domain build output in Gradle's ASCII-safe user cache; the APK
// still builds into app/build as usual.
layout.buildDirectory.set(
    file("${System.getProperty("user.home")}/.gradle/temi-classroom-delivery/domain"),
)

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}
