plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(project(":shared"))
            }
        }

        val iosMain by getting {
            dependencies {
                // iOS-specific dependencies
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.micyou.ios.resources"
    generateResClass = always
}
