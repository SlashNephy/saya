group = "blue.starry"

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    // For testing
    id("com.adarshr.test-logger") version "2.0.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object ThirdpartyVersion {
    const val Ktor = "1.5.1"
    const val JsonKt = "6.0.0"
    const val Jsoup = "1.13.1"
    const val AnnictKt = "2.2.0"
    const val Penicillin = "6.0.2"
    const val ApacheCommonsCodec = "1.15"
    const val KtorSwagger = "0.7.0"
    const val Clikt = "3.1.0"

    // logging
    const val KotlinLogging = "2.0.4"
    const val Logback = "1.2.3"
    const val jansi = "1.18"

    // testing
    const val JUnit = "5.7.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/riptakagi/maven")
    maven(url = "https://dl.bintray.com/starry-blue-sky/stable")
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-cio:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-websockets:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-html-builder:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-serialization:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-locations:${ThirdpartyVersion.Ktor}")
    implementation("com.github.nielsfalk:ktor-swagger:${ThirdpartyVersion.KtorSwagger}")

    // Ktor Client
    implementation("io.ktor:ktor-client-cio:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-client-serialization:${ThirdpartyVersion.Ktor}")
    implementation("io.ktor:ktor-client-logging:${ThirdpartyVersion.Ktor}")

    implementation("blue.starry:jsonkt:${ThirdpartyVersion.JsonKt}")
    implementation("jp.annict:annict-kt:${ThirdpartyVersion.AnnictKt}")
    implementation("blue.starry:penicillin:${ThirdpartyVersion.Penicillin}")
    implementation("commons-codec:commons-codec:${ThirdpartyVersion.ApacheCommonsCodec}")

    // HTML parsing
    implementation("org.jsoup:jsoup:${ThirdpartyVersion.Jsoup}")

    // CLI
    implementation("com.github.ajalt.clikt:clikt:${ThirdpartyVersion.Clikt}")

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

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                apiVersion = "1.4"
                languageVersion = "1.4"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true
        languageSettings.apply {
            useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
    }
}

/*
 * Tests
 */

buildtimetracker {
    reporters {
        register("summary") {
            options["ordered"] = "true"
            options["barstyle"] = "ascii"
            options["shortenTaskNames"] = "false"
        }
    }
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.saya.MainKt")
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "blue.starry.saya.MainKt"
    classpath(configurations.runtimeClasspath, tasks.jar)
}
