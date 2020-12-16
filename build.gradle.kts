import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "blue.starry"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.21"
}

object ThirdpartyVersion {
    const val Ktor = "1.4.3"
    const val JsonKt = "6.0.0"
    const val Exposed = "0.28.1"
    const val SQLiteJDBC = "3.30.1"

    // logging
    const val KotlinLogging = "2.0.3"
    const val Logback = "1.2.3"
    const val jansi = "1.18"

    // testing
    const val JUnit = "5.7.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/starry-blue-sky/stable")
}

dependencies {
    implementation("io.ktor:ktor-client-cio:${ThirdpartyVersion.Ktor}")
    implementation("blue.starry:jsonkt:${ThirdpartyVersion.JsonKt}")

    // sqlite
    implementation("org.jetbrains.exposed:exposed-core:${ThirdpartyVersion.Exposed}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${ThirdpartyVersion.Exposed}")
    implementation("org.jetbrains.exposed:exposed-java-time:${ThirdpartyVersion.Exposed}")
    implementation("org.xerial:sqlite-jdbc:${ThirdpartyVersion.SQLiteJDBC}")

    // logging
    implementation("io.github.microutils:kotlin-logging:${ThirdpartyVersion.KotlinLogging}")
    implementation("ch.qos.logback:logback-core:${ThirdpartyVersion.Logback}")
    implementation("ch.qos.logback:logback-classic:${ThirdpartyVersion.Logback}")
    implementation("org.fusesource.jansi:jansi:${ThirdpartyVersion.jansi}")

    // testing
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:${ThirdpartyVersion.JUnit}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
