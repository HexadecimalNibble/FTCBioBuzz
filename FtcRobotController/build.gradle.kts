//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//plugins {
//  alias(libs.plugins.android.library)
//}
//
//android {
//  namespace = "com.qualcomm.ftcrobotcontroller"
//  //noinspection GradleDependency
//  compileSdk = 34
//
//  defaultConfig {
//    minSdk = 24
//    buildConfigField(
//      "String",
//      "APP_BUILD_TIME",
//      "\"${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT).format(Date())}\""
//    )
//  }
//
//  buildFeatures {
//    buildConfig = true
//  }
//
//  compileOptions {
//    sourceCompatibility = JavaVersion.VERSION_17
//    targetCompatibility = JavaVersion.VERSION_17
//  }
//}
//
//dependencies {
//  implementation(libs.bundles.ftc.core)
//  implementation(libs.androidx.appcompat)
//}
