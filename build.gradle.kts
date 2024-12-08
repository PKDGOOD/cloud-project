import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.3.1")
    }
}

repositories {
    mavenCentral()
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
dependencies {
    testImplementation(kotlin("test"))

    implementation("aws.sdk.kotlin:ec2:1.2.28")
    implementation("aws.sdk.kotlin:ssm:1.2.28")
    implementation("aws.sdk.kotlin:secretsmanager:1.2.28")
    implementation("aws.smithy.kotlin:http-client-engine-okhttp:0.30.0")
    implementation("aws.smithy.kotlin:http-client-engine-crt:0.30.0")
    implementation("com.google.code.gson:gson:2.10")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}