/**
 * Top-level build file for ftc_app project.
 *
 * It is extraordinarily rare that you will ever need to edit this file.
 */

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
}

allprojects {
  repositories {
    mavenCentral()
    google()
    mavenLocal()
    maven { url = uri("https://mymaven.bylazar.com/releases") }
    maven { url = uri("https://repo.dairy.foundation/releases") }
    maven { url = uri("https://maven.anygeneric.dev/") }
  }
}