package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import java.io.IOException

inline fun <reified T> convertsTo(value: T, moshi: Moshi = Moshi.Builder().build()): Matcher<String?> = convertsTo(equalTo(value), moshi)

inline fun <reified T> convertsTo(match: Matcher<T>? = null, moshi: Moshi = Moshi.Builder().build()): Matcher<String?> =
        JsonConversionMatcher(T::class.simpleName, moshi.adapter(T::class.java).failOnUnknown(), match)

// Implemented outside of inline function so test coverage is reported correctly
// https://github.com/jacoco/jacoco/issues/654
class JsonConversionMatcher<T>(private val className: String?, private val adapter: JsonAdapter<T>, private val match: Matcher<T>?) : Matcher<String?> {
    override fun invoke(actual: String?): MatchResult {
        if (actual == null) {
            return MatchResult.Mismatch("actual was null")
        }
        return try {
            val converted = adapter.fromJson(actual) ?: return MatchResult.Mismatch("converted was null")
            // Smart-cast for nullable lambda property
            // https://youtrack.jetbrains.com/issue/KT-4113
            val theMatcher = match
            if (theMatcher == null) {
                MatchResult.Match
            } else {
                theMatcher(converted)
            }
        } catch (ex: JsonDataException) {
            MatchResult.Mismatch("failed to convert: $ex")
        } catch (ex: IOException) {
            MatchResult.Mismatch("failed to convert: $ex")
        }
    }

    override val description get() = "converted to ${if (match == null) className ?: "unknown type" else describe(match)}"
    override val negatedDescription: String get() = "is not $description"
}
