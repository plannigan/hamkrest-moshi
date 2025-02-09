import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    jacoco
    alias(libs.plugins.jacocolog)
}

group = "com.hypercubetools"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.hamkrest)
    implementation(libs.moshi)

    testImplementation(libs.moshi.reflection)
    testImplementation(libs.spek.api)  {
        exclude("org.jetbrains.kotlin")
    }
    testRuntimeOnly(libs.spek.engine)
    // spek requires kotlin-reflect
    testRuntimeOnly(libs.kotlin.reflect)
    kspTest(libs.moshi.codeGen)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }

    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            element = "BUNDLE"
            excludes = listOf("com.jacoco.dto.*")
            limit {
                counter = "INSTRUCTION"
                minimum = 0.60.toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = 0.55.toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
