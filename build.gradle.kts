import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import ru.vyarus.gradle.plugin.python.PythonExtension.Scope
import ru.vyarus.gradle.plugin.python.task.PythonTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    `maven-publish`
    alias(libs.plugins.nexus)
    signing
    alias(libs.plugins.sigstore)
    jacoco
    alias(libs.plugins.jacocolog)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.use.python)
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

dokka {
    dokkaPublications.html {
        outputDirectory = layout.buildDirectory.dir("dokka/html")
    }
    dokkaPublications.javadoc {
        outputDirectory = layout.buildDirectory.dir("dokka/javadoc")
    }
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGeneratePublicationJavadoc)
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val POM_ARTIFACT_ID: String by project
val POM_NAME: String by project
val POM_DESCRIPTION: String by project
val POM_URL: String by project

val POM_LICENCE_NAME: String by project
val POM_LICENCE_URL: String by project
val POM_LICENCE_DIST: String by project

val POM_SCM_URL: String by project
val POM_SCM_CONNECTION: String by project
val POM_SCM_DEV_CONNECTION: String by project

val POM_DEVELOPER_ID: String by project
val POM_DEVELOPER_NAME: String by project


nexusPublishing {
    repositories {
        sonatype()
    }
}

publishing {
    publications {
        create<MavenPublication>("core") {
            from(components["java"])

            artifact(tasks.kotlinSourcesJar.get())
            artifact(dokkaJavadocJar.get())

            groupId = group.toString()
            artifactId = POM_ARTIFACT_ID
            version = version.toString()

            pom {
                name.set(POM_NAME)
                description.set(POM_DESCRIPTION)
                url.set(POM_URL)

                licenses {
                    license {
                        name.set(POM_LICENCE_NAME)
                        url.set(POM_LICENCE_URL)
                        distribution.set(POM_LICENCE_DIST)
                    }
                }

                scm {
                    url.set(POM_SCM_URL)
                    connection.set(POM_SCM_CONNECTION)
                    developerConnection.set(POM_SCM_DEV_CONNECTION)
                }

                developers {
                    developer {
                        id.set(POM_DEVELOPER_ID)
                        name.set(POM_DEVELOPER_NAME)
                    }
                }
            }
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["core"])
}


python {
    scope = Scope.VIRTUALENV
    minPythonVersion = "3.9"
}

tasks.register<PythonTask>("hyperBumpIt") {
    description = "Bump the version number"
    module = "hyper_bump_it"
    command = project.properties["args"] ?: ""
}
