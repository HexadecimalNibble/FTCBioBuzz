plugins {
  id("dev.frozenmilk.teamcode") version "11.1.0-1.1.2"
}

ftc {
  kotlin()
  sdk.TeamCode()
}

dependencies {
  implementation(project(":HexLib"))
  implementation(libs.bundles.pedro)

  implementation(libs.kotlin.reflect)
  implementation(libs.mercurial)
  implementation(libs.sloth)
  implementation(libs.dairy.fullpanels)
}