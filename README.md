[![Kotlin](https://img.shields.io/badge/kotlin-2.1.10-blue.svg)](http://kotlinlang.org)
[![CI](https://github.com/plannigan/hamkrest-moshi/actions/workflows/main.yaml/badge.svg?branch=main)](https://github.com/plannigan/hamkrest-moshi/actions/workflows/main.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/com.hypercubetools/hamkrest-moshi)][maven]
[![codecov](https://codecov.io/gh/plannigan/hamkrest-moshi/branch/main/graph/badge.svg)](https://codecov.io/gh/plannigan/hamkrest-moshi)

# hamkrest-moshi

[Hamkrest][hamkrest] is a framework for writing matcher objects allowing 'match' rules to be defined declaratively. 

`hamkrest-moshi` provides matchers that can be used to assert [Moshi][moshi] can serialize and deserialize JSON content.

## Usage

`hamkrest-moshi` exposes two function:

* `deserializesTo()` - Verify that a JSON string can be converted to a specific type
* `serializesTo()` - Verify that a value can be converted to a JSON string

Examples:

`hamkrest-moshi` works best when using [Moshi's code generation][moshi-codegen] functionality.

```kotlin
@JsonClass(generateAdapter = true)
data class Bar(val key: String, val count: Int)

val someBar = Bar(key = "bar-123", count = 50)

// Verify Moshi can convert to and from a specific type
assertThat(someBar, serializesTo<Bar>())
assertThat("""{"key":"bar-123","count":50}""", deserializesTo<Bar>())

// Verify the converted value is equal to a specific value
assertThat(someBar, serializesTo<Bar>("""{"key":"bar-123","count":50}"""))
assertThat("""{"key":"bar-123","count":50}""", deserializesTo(someBar))

// Use a custom matcher on the converted value
assertThat(someBar, serializesTo<Bar>(containsSubstring("bar-123")))
assertThat("""{"key":"bar-123","count":50}""", deserializesTo(has(Bar::key, !isEmptyString)))
```

When using [Moshi's code generation][moshi-codegen] functionality, a `Moshi` instance that was configured to use
reflection must be passed in.

```kotlin
data class Foo(val id: Int, val name: String)
val someFoo = Foo(42, "Fooius")

val reflectionMoshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
assertThat("""{"id":42,"name":"Fooius"}""", deserializesTo(someFoo), moshi = reflectionMoshi)
```

[maven]: https://central.sonatype.com/artifact/com.hypercubetools/ktor-moshi/
[hamkrest]: https://github.com/npryce/hamkrest
[moshi]: https://github.com/square/moshi/
[moshi-codegen]: https://github.com/square/moshi/#codegen
[moshi-reflection]: https://github.com/square/moshi/#reflection
