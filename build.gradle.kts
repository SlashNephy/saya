plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("blue.starry.scriptextender") version "0.0.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-cio:1.6.8")
    implementation("io.ktor:ktor-websockets:1.6.8")
    implementation("io.ktor:ktor-serialization:1.6.8")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-apache:1.6.8")
    implementation("io.ktor:ktor-client-serialization:1.6.8")
    implementation("io.ktor:ktor-client-logging:1.6.8")

    implementation("com.charleskorn.kaml:kaml:0.44.0")
    implementation("blue.starry:penicillin:6.2.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("com.google.guava:guava:31.1-jre")

    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
                apiVersion = "1.6"
                languageVersion = "1.6"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true
            optIn("kotlin.RequiresOptIn")
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.saya.MainKt")
    }
}
