import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
    kotlin("jvm") version "1.8.0"
    application
}

group = "me.deonisos"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    }
}

dependencies {
    antlr("org.antlr:antlr4:4.10.1")
    implementation("org.antlr:antlr4-runtime:4.10.1")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    dependsOn("generateGrammarSource")
    sourceSets["main"].kotlin {
        srcDir("generated-src/antlr/main/")
    }

}

application {
    mainClass.set("MainKt")
}