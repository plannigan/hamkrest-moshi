import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka") version "0.10.1"
}

group = "com.hypercubetools"
version = "0.1.0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(Deps.hamkrest)
    implementation(Deps.Moshi.moshi)

    testImplementation(Deps.Moshi.reflection)
    testImplementation(Deps.Spek.api)  {
        exclude(Groups.kotlin)
    }
    testRuntimeOnly(Deps.Spek.engine)
    // spek requires kotlin-reflect
    testRuntimeOnly(Deps.kotlinReflect)
    "kaptTest"(Deps.Moshi.codeGen)
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = Versions.targetJvm
    }
}

testWithJunit()
// Values are artificially low because inline function are not currently reported accurately
// https://github.com/jacoco/jacoco/issues/654
coverWithJacoco(minInstructionCoverage = .7, minBranchCoverage = .45)


val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka"
}

val packageJavadoc by tasks.registering(Jar::class) {
    dependsOn("dokka")
    archiveClassifier.set("javadoc")
    from(dokka.outputDirectory)
}

val POM_ARTIFACT_ID: String by project

publishAs(POM_ARTIFACT_ID, tasks.kotlinSourcesJar, packageJavadoc)