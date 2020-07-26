package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import java.io.IOException

internal const val UNKNOWN_TYPE_NAME = "unknown type"
internal const val ARRAY_UNKNOWN_TYPE_NAME = "array of $UNKNOWN_TYPE_NAME"

inline fun <reified TOut> deserializesTo(value: TOut, moshi: Moshi = Moshi.Builder().build()): Matcher<String?> = deserializesTo(equalTo(value), moshi)

inline fun <reified TOut> deserializesTo(match: Matcher<TOut>? = null, moshi: Moshi = Moshi.Builder().build()): Matcher<String?> =
        JsonDeserializeMatcher(TOut::class.java, moshi, match)

// Implemented outside of inline function so test coverage is reported correctly
// https://github.com/jacoco/jacoco/issues/654
class JsonDeserializeMatcher<TOut>(
        private val targetClass: Class<TOut>,
        private val moshi: Moshi,
        private val match: Matcher<TOut>?) : Matcher<String?> {
    override fun invoke(actual: String?): MatchResult {
        if (actual == null) {
            return MatchResult.Mismatch("actual was null")
        }
        val adapter: JsonAdapter<TOut>
        try {
            adapter = moshi.adapter(targetClass).failOnUnknown()
        } catch (ex: IllegalArgumentException) {
            return MatchResult.Mismatch("Moshi adapter could not be created: $ex")
        }
        return try {
            val deserialized = adapter.fromJson(actual) ?: return MatchResult.Mismatch("deserialized result was null")
            // Smart-cast for nullable lambda property
            // https://youtrack.jetbrains.com/issue/KT-4113
            val theMatcher = match
            if (theMatcher == null) {
                MatchResult.Match
            } else {
                theMatcher(deserialized)
            }
        } catch (ex: JsonDataException) {
            MatchResult.Mismatch("failed to deserialize: $ex")
        } catch (ex: IOException) {
            MatchResult.Mismatch("failed to deserialize: $ex")
        }
    }

    override val description get() = "deserializes to ${if (match == null) targetName else describe(match)}"
    override val negatedDescription: String get() = "is not $description"
    private val targetName = tweakClassSimpleName(targetClass.simpleName)
}

inline fun <reified TIn> serializesTo(value: String, moshi: Moshi = Moshi.Builder().build()): Matcher<TIn> = serializesTo(equalTo(value), moshi)

inline fun <reified TIn> serializesTo(match: Matcher<String>? = null, moshi: Moshi = Moshi.Builder().build()): Matcher<TIn> =
        JsonSerializeMatcher(TIn::class.java, moshi, match)

class JsonSerializeMatcher<TIn>(
        private val targetClass: Class<TIn>,
        private val moshi: Moshi,
        private val match: Matcher<String>?) : Matcher<TIn> {
    override fun invoke(actual: TIn): MatchResult {
        val adapter: JsonAdapter<TIn>
        try {
            adapter = moshi.adapter(targetClass).failOnUnknown()
        } catch (ex: IllegalArgumentException) {
            return MatchResult.Mismatch("Moshi adapter could not be created: $ex")
        }
        val serialized = adapter.toJson(actual)
        // Smart-cast for nullable lambda property
        // https://youtrack.jetbrains.com/issue/KT-4113
        val theMatcher = match
        return if (theMatcher == null) {
            MatchResult.Match
        } else {
            theMatcher(serialized)
        }
    }

    override val description get() = "deserializes to ${if (match == null) targetName else describe(match)}"
    override val negatedDescription: String get() = "is not $description"
    private val targetName = tweakClassSimpleName(targetClass.simpleName)
}

// Details on what simpleName can return
// https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/Class.html#getSimpleName()
internal fun tweakClassSimpleName(name: String) = when(name) {
    "" -> UNKNOWN_TYPE_NAME
    "[]" -> ARRAY_UNKNOWN_TYPE_NAME
    else -> name
}
