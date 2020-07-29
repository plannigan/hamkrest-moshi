import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    jacoco
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
