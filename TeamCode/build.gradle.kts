plugins {
  id("dev.frozenmilk.teamcode") version "11.1.0-1.1.2"
}

ftc {
  kotlin()
  sdk.TeamCode()
  dairy {
    implementation(Sloth)

    ftControl {
      implementation(fullpanels)
    }

    implementation(MercurialFTC)
  }
}

dependencies {
  implementation(project(":HexLib"))
  implementation(libs.bundles.common)
  implementation(libs.kotlin.reflect)
}