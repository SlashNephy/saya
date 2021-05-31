import blue.starry.scriptextender.env

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.expediagroup.graphql") version "4.1.1"
    id("blue.starry.scriptextender") version "0.0.2"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("com.adarshr.test-logger") version "3.0.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object Versions {
    const val Ktor = "1.6.0"
    const val GraphQLKtor = "4.1.1"
    const val kaml = "0.34.0"
    const val Penicillin = "6.2.0"
    const val CommonsCodec = "1.15"
    const val Jsoup = "1.13.1"
    const val Guava = "30.1.1-jre"

    const val KotlinLogging = "2.0.6"
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
    const val GraphQLKtor = "com.expediagroup:graphql-kotlin-ktor-client:${Versions.GraphQLKtor}"

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
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlin.io.path.ExperimentalPathApi",
        "kotlin.time.ExperimentalTime",
        "kotlin.ExperimentalStdlibApi",
        "kotlinx.coroutines.FlowPreview",
        "io.ktor.util.KtorExperimentalAPI"
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
    implementation(Libraries.GraphQLKtor)

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
                jvmTarget = JavaVersion.VERSION_1_8.toString()
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

val ANNICT_TOKEN by env

graphql {
    client {
        endpoint = "https://api.annict.com/graphql"
        packageName = "blue.starry.saya.services.annict.generated"
        if (ANNICT_TOKEN.isPresent) {
            headers = mapOf("Authorization" to "bearer ${ANNICT_TOKEN.value}")
        }
        queryFileDirectory = projectDir.resolve("src/main/graphql/annict").toString()
        clientType = com.expediagroup.graphql.plugin.gradle.config.GraphQLClientType.KTOR
    }
}

tasks.named("graphqlGenerateClient") {
    if (!ANNICT_TOKEN.isPresent) {
        projectDir.resolve("src/main/graphql/annict/schema.graphql").copyTo(buildDir.resolve("schema.graphql"), true)
    }
}

tasks.named("graphqlIntrospectSchema") {
    onlyIf { ANNICT_TOKEN.isPresent }
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
