import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    iosArm64 {
        configureCInterop()
    }
    iosSimulatorArm64 {
        configureCInterop()
    }
    iosX64 {
        configureCInterop()
    }

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

        val iosMain by creating {
            dependsOn(commonMain)
        }

        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)
        iosX64Main.get().dependsOn(iosMain)
    }
}

fun KotlinNativeTarget.configureCInterop() {
    compilations.getByName("main") {
        cinterops {
            val objc by creating {
                definitionFile.set(project.file("src/main/objc/MicYouAudioBridge.def"))
                includeDirs.headerFilterOnly(project.file("src/main/objc"))
            }
        }
    }
}

compose.resources {
    publicResClass = false
    generateResClass = never
}
