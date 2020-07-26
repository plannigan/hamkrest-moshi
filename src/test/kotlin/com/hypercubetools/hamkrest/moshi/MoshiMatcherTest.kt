package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

const val SOME_STRING = "a string"
const val SOME_STRING_JSON = "\"$SOME_STRING\""
const val OTHER_STRING_JSON = "\"NOT EXPECTED VALUE\""

const val SOME_ID = 42
const val SOME_NAME = "Foosius"
val SOME_FOO = Foo(id = SOME_ID, name = SOME_NAME)
const val SOME_FOO_JSON = """{"id":$SOME_ID,"name":"$SOME_NAME"}"""
const val SOME_KEY = "bar-123"
const val SOME_COUNT = 50
val SOME_BAR = Bar(key = SOME_KEY, count = SOME_COUNT)
const val SOME_BAR_JSON = """{"key":"$SOME_KEY","count":$SOME_COUNT}"""
val SOME_ARRAYLIST = arrayListOf(1, 2, 3)
const val SOME_ARRAYLIST_JSON = "[1, 2, 3]"



object JsonDeserializeMatcherTest : Spek({
  val invalidValuesToDescription = listOf(
          null to "no value",
          "null" to "null literal",
          "aa{" to "not json",
          "{}" to "json, but not value"
  )

  describe("deserializesTo") {
    describe("no match argument") {
      for ((value, description) in invalidValuesToDescription) {
        it("invalid - $description") {
          val matcher = deserializesTo<String>()

          assertMismatch(matcher(value))
        }
      }
      it("valid value - primitive") {
        val matcher = deserializesTo<String>()

        assertMatch(matcher(SOME_STRING_JSON))
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

    describe("type needs adapter added") {
      it("reflection required, but not there") {
        val matcher = deserializesTo(SOME_FOO)

        assertMismatch(matcher(SOME_FOO_JSON))
      }
      it("platform type") {
        val matcher = deserializesTo(SOME_ARRAYLIST)

        assertMismatch(matcher(SOME_ARRAYLIST_JSON))
      }
    }

    describe("match argument") {
      it("valid value and sub-matcher also matches") {
        val matcherWithSubMatcher = deserializesTo(Matcher(String::isNotEmpty))

        assertMatch(matcherWithSubMatcher(SOME_STRING_JSON))
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

        assertMismatch(matcherWithSubMatcher(SOME_STRING_JSON))
      }
    }

    describe("value argument") {
      for ((value, description) in invalidValuesToDescription) {
        it("mismatch - $description") {
          val matcher = deserializesTo(SOME_STRING)

          assertMismatch(matcher(value))
        }
      }
      it("valid value and matches expected") {
        val matcher = deserializesTo(SOME_STRING)

        assertMatch(matcher(SOME_STRING_JSON))
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
        val matcher = deserializesTo(SOME_STRING)

        assertMismatch(matcher(OTHER_STRING_JSON))
      }
    }
  }

  describe("description") {
    it("no matcher - includes class name") {
      val matcher = deserializesTo<String>()

      assertThat(matcher.description, containsSubstring(String::class.simpleName!!))
    }
    it("no matcher, anonymous class - includes unknown") {
      val matcher = JsonDeserializeMatcher(
              Container.anonymousInstance::class.java, Moshi.Builder().build(),
              match = null)

      assertThat(matcher.description, containsSubstring(UNKNOWN_TYPE_NAME))
    }
    it("array of anonymous class - includes unknown") {
      // Haven't been able to find a way to create an array of an anonymous class
      // or mock Class<T> to return this possible value, so explicitly test value
      assertThat(tweakClassSimpleName("[]"), containsSubstring(ARRAY_UNKNOWN_TYPE_NAME))
    }
    it("matcher - includes matcher description") {
      val valueMatcher = equalTo(SOME_STRING)
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

object JsonSerializeMatcherTest : Spek({
  describe("serializesTo") {
    describe("no match argument") {
      it("valid value - primitive") {
        val matcher = serializesTo<String>()

        assertMatch(matcher(SOME_STRING))
      }
      it("valid value - moshi codegen") {
        val matcher = serializesTo<Bar>()

        assertMatch(matcher(SOME_BAR))
      }
      it("valid value - moshi reflection") {
        val matcher = serializesTo<Foo>(moshi = makeMoshiReflection())

        assertMatch(matcher(SOME_FOO))
      }
    }

    describe("type needs adapter added") {
      it("reflection required, but not there") {
        val matcher = serializesTo<Foo>(SOME_FOO_JSON)

        assertMismatch(matcher(SOME_FOO))
      }
      it("platform type") {
        val matcher = serializesTo<ArrayList<Int>>(SOME_ARRAYLIST_JSON)

        assertMismatch(matcher(SOME_ARRAYLIST))
      }
    }

    describe("match argument") {
      it("valid value and sub-matcher also matches") {
        val matcherWithSubMatcher = serializesTo<String>(Matcher(String::isNotEmpty))

        assertMatch(matcherWithSubMatcher(SOME_STRING))
      }
      it("valid value and sub-matcher also matches - moshi codegen") {
        val matcher = serializesTo<Bar>(containsSubstring(SOME_KEY))

        assertMatch(matcher(SOME_BAR))
      }
      it("valid value and sub-matcher also matches - moshi reflection") {
        val matcher = serializesTo<Foo>(
                match = containsSubstring(SOME_NAME),
                moshi = makeMoshiReflection()
        )

        assertMatch(matcher(SOME_FOO))
      }
      it("valid value and sub-matcher not matches") {
        val matcherWithSubMatcher = serializesTo<String>(Matcher(String::isEmpty))

        assertMismatch(matcherWithSubMatcher(SOME_STRING))
      }
    }

    describe("value argument") {
      it("valid value and matches expected") {
        val matcher = serializesTo<String>(SOME_STRING_JSON)

        assertMatch(matcher(SOME_STRING))
      }
      it("valid value and matches expected - moshi codegen") {
        val matcher = serializesTo<Bar>(SOME_BAR_JSON)

        assertMatch(matcher(SOME_BAR))
      }
      it("valid value and matches expected - moshi reflection") {
        val matcher = serializesTo<Foo>(SOME_FOO_JSON, moshi = makeMoshiReflection())

        assertMatch(matcher(SOME_FOO))
      }
      it("valid value and but doesn't match expected") {
        val matcher = serializesTo<String>(SOME_STRING)

        assertMismatch(matcher(OTHER_STRING_JSON))
      }
    }
  }

  describe("description") {
    it("no matcher - includes class name") {
      val matcher = serializesTo<String>()

      assertThat(matcher.description, containsSubstring(String::class.simpleName!!))
    }
    it("no matcher, anonymous class - includes unknown") {
      val matcher = JsonDeserializeMatcher(
              Container.anonymousInstance::class.java, Moshi.Builder().build(),
              match = null)

      assertThat(matcher.description, containsSubstring(UNKNOWN_TYPE_NAME))
    }
    it("array of anonymous class - includes unknown") {
      // Haven't been able to find a way to create an array of an anonymous class
      // or mock Class<T> to return this possible value, so explicitly test value
      assertThat(tweakClassSimpleName("[]"), containsSubstring(ARRAY_UNKNOWN_TYPE_NAME))
    }
    it("matcher - includes matcher description") {
      val valueMatcher = equalTo(SOME_STRING_JSON)
      val matcher = serializesTo<String>(valueMatcher)

      assertThat(matcher.description, containsSubstring(describe(valueMatcher)))
    }
    it("negated - includes description") {
      val matcher = serializesTo<String>()
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
