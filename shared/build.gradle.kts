plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
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
