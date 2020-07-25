package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.squareup.moshi.Moshi
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

const val EXPECTED_VALUE = "a string"
const val JSON_SOURCE = "\"$EXPECTED_VALUE\""
const val OTHER_JSON_SOURCE = "\"NOT EXPECTED VALUE\""


val INVALID_VALUES__DESCRIPTION = listOf(
              null to "no value",
              "null" to "null literal",
              "aa{" to "not json",
              "{}" to "json, but not value"
      )

object JsonConversionMatcherTest : Spek({
  describe("deserializesTo") {
    describe("no match argument") {
      for ((value, description) in INVALID_VALUES__DESCRIPTION) {
        it("mismatch - $description") {
          val matcher = deserializesTo<String>()

          assertMismatch(matcher(value))
        }
      }
      it("valid value") {
        val matcher = deserializesTo<String>()

        assertMatch(matcher(JSON_SOURCE))
      }
    }

    describe("match argument") {
      it("valid value and sub-matcher also matches") {
        val matcherWithSubMatcher = deserializesTo(Matcher(String::isNotEmpty))

        assertMatch(matcherWithSubMatcher(JSON_SOURCE))
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
