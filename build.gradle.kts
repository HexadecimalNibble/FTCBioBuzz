/**
 * Top-level build file for ftc_app project.
 *
 * It is extraordinarily rare that you will ever need to edit this file.
 */

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("com.android.library") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        // Note for FTC Teams: Do not modify this yourself.
        // classpath("com.android.tools.build:gradle:8.7.0")
        // classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    }
}

// This is now required because aapt2 has to be downloaded from the
// google() repository beginning with version 3.2 of the Android Gradle Plugin
allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

repositories {
    mavenCentral()
}
