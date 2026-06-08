repositories {
    mavenLocal()
    mavenCentral()
    google() // Needed for androidx
    maven { url = uri("https://mymaven.bylazar.com/releases") }
    gradlePluginPortal()
    maven { url = uri("https://repo.dairy.foundation/releases") }
}

dependencies {
    add("implementation", "org.firstinspires.ftc:Inspection:11.1.0")
    add("implementation", "org.firstinspires.ftc:Blocks:11.1.0")
    //noinspection Aligned16KB
    add("implementation", "org.firstinspires.ftc:RobotCore:11.1.0")
    add("implementation", "org.firstinspires.ftc:RobotServer:11.1.0")
    add("implementation", "org.firstinspires.ftc:OnBotJava:11.1.0")
    add("implementation", "org.firstinspires.ftc:Hardware:11.1.0")
    add("implementation", "org.firstinspires.ftc:FtcCommon:11.1.0")
    add("implementation", "org.firstinspires.ftc:Vision:11.1.0")
    add("implementation", "androidx.appcompat:appcompat:1.2.0")

    add("implementation", "com.bylazar:fullpanels:1.0.12")
    add("implementation", "com.pedropathing:ftc:2.1.2")
    add("implementation", "com.pedropathing:telemetry:1.0.0")
    add("implementation", "dev.nextftc:control:1.0.0")

//    add("testImplementation", "junit:junit:6.1.0")
    add("testImplementation", platform("org.junit:junit-bom:6.1.0"))
    add("testImplementation", "org.junit.jupiter:junit-jupiter")
    add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

    add("testImplementation", "io.mockk:mockk:1.14.9")
    add("testImplementation", "io.mockk:mockk-android:1.14.9")
    add("testImplementation", "io.mockk:mockk-agent:1.14.9")
    add("implementation", "dev.anygeneric:blazeftc:0.1.36")
    add("implementation", "dev.frozenmilk.dairy:Mercurial:2.0.0-beta8")
    add("implementation", "dev.frozenmilk.sinister:Sloth:0.2.4")
    add("implementation", "org.jetbrains.kotlin:kotlin-reflect:2.2.21")
}
