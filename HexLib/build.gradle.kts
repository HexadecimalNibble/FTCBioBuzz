import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
//  alias(libs.plugins.android.library)
//  alias(libs.plugins.kotlin.android)
  id("dev.frozenmilk.android-library") version "11.1.0-1.1.2"
}

android.namespace = "org.hexnibble.hexlib"

//android {
//  namespace = "org.hexnibble.hexlib"
//  //noinspection GradleDependency
//  compileSdk = 34
//
//  defaultConfig {
//    minSdk = 24
//    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//  }
//
//  buildTypes {
//    getByName("release") {
//      isMinifyEnabled = false
//      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//    }
//  }
//  compileOptions {
//    sourceCompatibility = JavaVersion.VERSION_17
//    targetCompatibility = JavaVersion.VERSION_17
//  }
//  kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_17
//
//  testOptions {
//    unitTests {
//      isReturnDefaultValues = true
//      all {
//        it.useJUnitPlatform()
//      }
//    }
//  }
//}

ftc {
  kotlin()
  sdk {
    compileOnly(RobotCore)
    compileOnly(Hardware)
  }
  pedro {
    implementation(core)
    implementation(ftc)
    implementation(telemetry)
  }
}


dependencies {
//  implementation(libs.bundles.ftc.core)
  implementation(libs.bundles.common)
}