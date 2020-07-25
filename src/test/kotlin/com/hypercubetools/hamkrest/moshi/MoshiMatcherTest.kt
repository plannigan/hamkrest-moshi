package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

const val EXPECTED_VALUE = "a string"
const val JSON_SOURCE = "\"$EXPECTED_VALUE\""
const val OTHER_JSON_SOURCE = "\"NOT EXPECTED VALUE\""

const val SOME_ID = 42
const val SOME_NAME = "Foosius"
val SOME_FOO = Foo(id = SOME_ID, name = SOME_NAME)
const val SOME_FOO_JSON = """{"id":$SOME_ID,"name":"$SOME_NAME"}"""
const val SOME_KEY = "bar-123"
const val SOME_COUNT = 50
val SOME_BAR = Bar(key = SOME_KEY, count = SOME_COUNT)
const val SOME_BAR_JSON = """{"key":"$SOME_KEY","count":$SOME_COUNT}"""


val INVALID_VALUES__DESCRIPTION = listOf(
              null to "no value",
              "null" to "null literal",
              "aa{" to "not json",
              "{}" to "json, but not value"
      )


object JsonDeserializeMatcherTest : Spek({
  describe("deserializesTo") {
    describe("no match argument") {
      for ((value, description) in INVALID_VALUES__DESCRIPTION) {
        it("mismatch - $description") {
          val matcher = deserializesTo<String>()

          assertMismatch(matcher(value))
        }
      }
      it("valid value - primitive") {
        val matcher = deserializesTo<String>()

        assertMatch(matcher(JSON_SOURCE))
      }
      it("valid value - moshi codegen") {
        val matcher = deserializesTo<Bar>()

        assertMatch(matcher(SOME_BAR_JSON))
      }
      it("valid value - moshi reflection") {
        val matcher = deserializesTo<Foo>(moshi = makeMoshiReflection())

        assertMatch(matcher(SOME_FOO_JSON))
      }
    }

    describe("match argument") {
      it("valid value and sub-matcher also matches") {
        val matcherWithSubMatcher = deserializesTo(Matcher(String::isNotEmpty))

        assertMatch(matcherWithSubMatcher(JSON_SOURCE))
      }
      it("valid value and sub-matcher also matches - moshi codegen") {
        val matcher = deserializesTo(has(Bar::key, !isEmptyString))

        assertMatch(matcher(SOME_BAR_JSON))
      }
      it("valid value and sub-matcher also matches - moshi reflection") {
        val matcher = deserializesTo(
                match = has(Foo::name, !isEmptyString),
                moshi = makeMoshiReflection()
        )

        assertMatch(matcher(SOME_FOO_JSON))
      }
      it("valid value and sub-matcher not matches") {
        val matcherWithSubMatcher = deserializesTo(Matcher(String::isEmpty))

        assertMismatch(matcherWithSubMatcher(JSON_SOURCE))
      }
    }

    describe("value argument") {
      for ((value, description) in INVALID_VALUES__DESCRIPTION) {
        it("mismatch - $description") {
          val matcher = deserializesTo(EXPECTED_VALUE)

          assertMismatch(matcher(value))
        }
      }
      it("valid value and matches expected") {
        val matcher = deserializesTo(EXPECTED_VALUE)

        assertMatch(matcher(JSON_SOURCE))
      }
      it("valid value and matches expected - moshi codegen") {
        val matcher = deserializesTo(SOME_BAR)

        assertMatch(matcher(SOME_BAR_JSON))
      }
      it("valid value and matches expected - moshi reflection") {
        val matcher = deserializesTo(SOME_FOO, moshi = makeMoshiReflection())

        assertMatch(matcher(SOME_FOO_JSON))
      }
      it("valid value and but doesn't match expected") {
        val matcher = deserializesTo(EXPECTED_VALUE)

        assertMismatch(matcher(OTHER_JSON_SOURCE))
      }
    }
  }
  describe("description") {
    it("no matcher - includes class name") {
      val matcher = deserializesTo<String>()

      assertThat(matcher.description, containsSubstring(String::class.simpleName!!))
    }
    it("no matcher, class name not known - includes unknown") {
      val matcher = JsonDeserializeMatcher(className = null, adapter = Moshi.Builder().build().adapter(String::class.java), match = null)

      assertThat(matcher.description, containsSubstring("unknown type"))
    }
    it("matcher - includes matcher description") {
      val valueMatcher = equalTo(EXPECTED_VALUE)
      val matcher = deserializesTo(valueMatcher)

      assertThat(matcher.description, containsSubstring(describe(valueMatcher)))
    }
    it("negated - includes description") {
      val matcher = deserializesTo<String>()
      val negatedMatcher = !matcher

      assertThat(negatedMatcher.description, containsSubstring(matcher.description))
    }
  }
})

fun assertMatch(result: MatchResult) = assertThat(result, isA<MatchResult.Match>())
fun assertMismatch(result: MatchResult) = assertThat(result, isA<MatchResult.Mismatch>())

fun makeMoshiReflection(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

data class Foo(val id: Int, val name: String)

@JsonClass(generateAdapter = true)
data class Bar(val key: String, val count: Int)
