plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.5.20"
    id("blue.starry.scriptextender") version "0.0.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("com.adarshr.test-logger") version "3.2.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object Versions {
    const val Ktor = "1.6.1"
    const val kaml = "0.34.0"
    const val Penicillin = "6.2.0"
    const val CommonsCodec = "1.15"
    const val Jsoup = "1.13.1"
    const val Guava = "30.1.1-jre"

    const val KotlinLogging = "2.0.8"
    const val Logback = "1.2.3"
    const val jansi = "2.3.2"

    const val JUnit = "5.7.1"
}

object Libraries {
    const val KtorServerCIO = "io.ktor:ktor-server-cio:${Versions.Ktor}"
    const val KtorWebSockets = "io.ktor:ktor-websockets:${Versions.Ktor}"
    const val KtorSerialization = "io.ktor:ktor-serialization:${Versions.Ktor}"
    const val KtorClientCIO = "io.ktor:ktor-client-cio:${Versions.Ktor}"
    const val KtorClientApache = "io.ktor:ktor-client-apache:${Versions.Ktor}"
    const val KtorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.Ktor}"
    const val KtorClientLogging = "io.ktor:ktor-client-logging:${Versions.Ktor}"

    const val kaml = "com.charleskorn.kaml:kaml:${Versions.kaml}"
    const val Penicillin = "blue.starry:penicillin:${Versions.Penicillin}"
    const val CommonsCodec = "commons-codec:commons-codec:${Versions.CommonsCodec}"
    const val Jsoup = "org.jsoup:jsoup:${Versions.Jsoup}"
    const val Guava = "com.google.guava:guava:${Versions.Guava}"

    const val KotlinLogging = "io.github.microutils:kotlin-logging:${Versions.KotlinLogging}"
    const val LogbackCore = "ch.qos.logback:logback-core:${Versions.Logback}"
    const val LogbackClassic = "ch.qos.logback:logback-classic:${Versions.Logback}"
    const val Jansi = "org.fusesource.jansi:jansi:${Versions.jansi}"
    const val JUnitJupiter = "org.junit.jupiter:junit-jupiter:${Versions.JUnit}"

    val ExperimentalAnnotations = setOf(
        // Duration#toJavaDuration()
        "kotlin.time.ExperimentalTime",
        // buildList {}
        "kotlin.ExperimentalStdlibApi",
        // flowOf()
        "kotlinx.coroutines.FlowPreview",
        // CoroutineScope#produce {}
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        // GlobalScope
        "kotlinx.coroutines.DelicateCoroutinesApi"
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libraries.KtorServerCIO)
    implementation(Libraries.KtorWebSockets)
    implementation(Libraries.KtorSerialization)
    implementation(Libraries.KtorClientCIO)
    implementation(Libraries.KtorClientApache)
    implementation(Libraries.KtorClientSerialization)
    implementation(Libraries.KtorClientLogging)

    implementation(Libraries.kaml)
    implementation(Libraries.Penicillin)
    implementation(Libraries.CommonsCodec)
    implementation(Libraries.Jsoup)
    implementation(Libraries.Guava)

    implementation(Libraries.KotlinLogging)
    implementation(Libraries.LogbackCore)
    implementation(Libraries.LogbackClassic)
    implementation(Libraries.Jansi)

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(Libraries.JUnitJupiter)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
                apiVersion = "1.5"
                languageVersion = "1.5"
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true

        Libraries.ExperimentalAnnotations.forEach {
            languageSettings.useExperimentalAnnotation(it)
        }
    }
}

/*
 * Tests
 */

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    ignoreFailures.set(true)
}

buildtimetracker {
    reporters {
        register("summary") {
            options["ordered"] = "true"
            options["barstyle"] = "ascii"
            options["shortenTaskNames"] = "false"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        events("passed", "failed")
    }

    testlogger {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "blue.starry.saya.MainKt"
    classpath(configurations.runtimeClasspath, tasks.jar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.saya.MainKt")
    }
}
