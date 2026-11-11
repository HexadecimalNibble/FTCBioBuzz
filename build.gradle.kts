plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
}

allprojects {
  repositories {
    google()
    mavenCentral()
    mavenLocal()

    maven {
      url = uri("https://mymaven.bylazar.com/releases")
    }

    maven {
      url = uri("https://repo.dairy.foundation/releases")
    }

    maven {
      url = uri("https://maven.anygeneric.dev/")
    }
  }
}