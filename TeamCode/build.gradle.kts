plugins {
  id("dev.frozenmilk.teamcode") version "11.1.0-1.1.2"
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
  implementation(project(":HexLib"))
  implementation(libs.bundles.pedro)

  implementation(libs.kotlin.reflect)
  implementation(libs.mercurial.ftc)
  implementation(libs.dairy.fullpanels)
  implementation(libs.nextftc.control)

  implementation(libs.blaze.ftc)
  implementation(libs.blaze.ftc.pedro)
}