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

inline fun <reified TIn> serializesTo(value: String, moshi: Moshi = Moshi.Builder().build()): Matcher<TIn> = serializesTo(equalTo(value), moshi)

inline fun <reified TIn> serializesTo(match: Matcher<String>? = null, moshi: Moshi = Moshi.Builder().build()): Matcher<TIn> =
        JsonSerializeMatcher(TIn::class.java, moshi, match)

class JsonDeserializeMatcher<TOut>(
        targetClass: Class<TOut>,
        moshi: Moshi,
        match: Matcher<TOut>?) : BaseMatcher<TOut, TOut, String?>("deserialize", targetClass, moshi, match) {
    override fun invoke(actual: String?): MatchResult {
        if (actual == null) {
            return MatchResult.Mismatch("actual was null")
        }
        val adapter: JsonAdapter<TOut>
        try {
            adapter = retrieveAdapter(targetClass)
        } catch (ex: IllegalArgumentException) {
            return adapterFailure(ex)
        }
        val deserialized: TOut
        try {
            deserialized = adapter.fromJson(actual) ?: return MatchResult.Mismatch("deserialized result was null")
        } catch (ex: JsonDataException) {
            return MatchResult.Mismatch("failed to deserialize: $ex")
        } catch (ex: IOException) {
            return MatchResult.Mismatch("failed to deserialize: $ex")
        }
        return verify(deserialized)
    }
}

class JsonSerializeMatcher<TIn>(
        targetClass: Class<TIn>,
        moshi: Moshi,
        match: Matcher<String>?) : BaseMatcher<TIn, String, TIn>("serialize", targetClass, moshi, match) {
    override fun invoke(actual: TIn): MatchResult {
        val adapter: JsonAdapter<TIn>
        try {
            adapter = retrieveAdapter(targetClass)
        } catch (ex: IllegalArgumentException) {
            return adapterFailure(ex)
        }
        val serialized = adapter.toJson(actual)
        return verify(serialized)
    }
}

sealed class BaseMatcher<TTarget, TMatchArg, TMatch>(
        private val actionDescription: String,
        protected val targetClass: Class<TTarget>,
        private val moshi: Moshi,
        private val match: Matcher<TMatchArg>?) : Matcher<TMatch> {

    protected fun verify(value: TMatchArg) : MatchResult {
        // Smart-cast for nullable lambda property
        // https://youtrack.jetbrains.com/issue/KT-4113
        val theMatcher = match
        return if (theMatcher == null) {
            MatchResult.Match
        } else {
            theMatcher(value)
        }
    }

    protected fun <T> retrieveAdapter(targetClass: Class<T>): JsonAdapter<T> = moshi.adapter(targetClass).failOnUnknown()
    protected fun adapterFailure(ex: IllegalArgumentException) =
            MatchResult.Mismatch("Moshi adapter could not be created: $ex")

    override val description get() =
        "$actionDescription to ${if (match == null) tweakClassSimpleName(targetClass.simpleName) else describe(match)}"
    override val negatedDescription: String get() = "is not $description"
}

// Details on what simpleName can return
// https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/Class.html#getSimpleName()
internal fun tweakClassSimpleName(name: String) = when(name) {
    "" -> UNKNOWN_TYPE_NAME
    "[]" -> ARRAY_UNKNOWN_TYPE_NAME
    else -> name
}
