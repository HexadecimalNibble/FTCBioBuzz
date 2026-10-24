plugins {
  id("dev.frozenmilk.teamcode") version "12.0.0-1.2.3"
}

ftc {
  kotlin()
  sdk.TeamCode()
}

androidComponents {
  finalizeDsl { extension ->
    extension.compileSdk = 34
  }
}

dependencies {
  implementation(libs.kotlin.reflect)

  implementation(libs.blaze.ftc)
  implementation(libs.blaze.ftc.pedro)
//  implementation(libs.ivy)

  implementation(libs.bundles.pedro)
  implementation(libs.bundles.nextftc)
}