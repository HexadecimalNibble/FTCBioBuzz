import java.util.regex.Pattern

//
// build.gradle in TeamCode
//

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

apply(from = "../build.dependencies.gradle.kts")

android {
    namespace = "org.firstinspires.ftc.teamcode"
    compileSdk = 34

    signingConfigs {
        create("release") {
            val apkStoreFile = System.getenv("APK_SIGNING_STORE_FILE")
            if (apkStoreFile != null) {
                keyAlias = System.getenv("APK_SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("APK_SIGNING_KEY_PASSWORD")
                storeFile = file(System.getenv("APK_SIGNING_STORE_FILE"))
                storePassword = System.getenv("APK_SIGNING_STORE_PASSWORD")
            } else {
                keyAlias = "androiddebugkey"
                keyPassword = "android"
                storeFile = rootProject.file("libs/ftc.debug.keystore")
                storePassword = "android"
            }
        }

        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file("libs/ftc.debug.keystore")
            storePassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.qualcomm.ftcrobotcontroller"
        minSdk = 24
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 28
        
        signingConfig = signingConfigs.getByName("debug")

        val manifestFile = project(":FtcRobotController").file("src/main/AndroidManifest.xml")
        val manifestText = manifestFile.readText()
        
        val vCodePattern = Pattern.compile("versionCode=\"(\\d+(\\.\\d+)*)\"")
        val matcherCode = vCodePattern.matcher(manifestText)
        matcherCode.find()
        val vCode = matcherCode.group(1).toInt()
        
        val vNamePattern = Pattern.compile("versionName=\"(.*)\"")
        val matcherName = vNamePattern.matcher(manifestText)
        matcherName.find()
        val vName = matcherName.group(1)
        
        versionCode = vCode
        versionName = vName
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")

            ndk {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }
        }
        getByName("debug") {
            isDebuggable = true
            isJniDebuggable = true
            ndk {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        jniLibs.useLegacyPackaging = true
        resources.pickFirsts.add("**/*.so")
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    
    ndkVersion = "21.3.6528147"
}

dependencies {
    implementation(project(":FtcRobotController"))
    implementation(project(":HexLib"))
}
