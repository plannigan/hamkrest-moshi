package com.hypercubetools.hamkrest.moshi

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object MoshiConverterTest : Spek({
  describe("MoshiMatcher") {
    it("always false") {
      assertThat(convertsTo(), equalTo(false))
    }
  }
})