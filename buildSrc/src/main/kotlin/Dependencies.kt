// Version information for dependencies

object Versions {
    const val kotlin = "1.3.72"
    const val moshi = "1.9.3"
    const val hamkrest = "1.7.0.3"
    const val spek = "2.0.12"
    const val targetJvm = "1.8"
}
object Groups {
    const val kotlin = "org.jetbrains.kotlin"
    const val moshi = "com.squareup.moshi"
    const val hamkrest = "com.natpryce"
    const val spek = "org.spekframework.spek2"
}

object Deps {
    object Moshi {
        const val moshi = "${Groups.moshi}:moshi:${Versions.moshi}"
        const val codeGen = "${Groups.moshi}:moshi-kotlin-codegen:${Versions.moshi}"
        const val adapters = "${Groups.moshi}:moshi-adapters:${Versions.moshi}"
        const val reflection = "${Groups.moshi}:moshi-kotlin:${Versions.moshi}"
    }
    const val hamkrest = "${Groups.hamkrest}:hamkrest:${Versions.hamkrest}"
    object Spek {
        const val api = "${Groups.spek}:spek-dsl-jvm:${Versions.spek}"
        const val engine = "${Groups.spek}:spek-runner-junit5:${Versions.spek}"
    }
    const val kotlinReflect = "${Groups.kotlin}:kotlin-reflect:${Versions.kotlin}"
}
