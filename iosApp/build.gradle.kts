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
                implementation(compose.components.resources)
                implementation(project(":shared"))
            }
        }

        // Create iosMain as a intermediate source set for all iOS targets
        val iosMain by creating {
            dependsOn(commonMain)
        }

        // Link each iOS target to iosMain
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)
        iosX64Main.get().dependsOn(iosMain)
    }
}

compose.resources {
    publicResClass = false
    generateResClass = never
}
