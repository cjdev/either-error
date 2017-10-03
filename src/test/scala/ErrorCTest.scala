// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ErrorCTest extends FlatSpec with Matchers with PropertyChecks {

  import com.cj.eithererror.ErrorC._

  // Instances should satisfy the following law:
  //
  //     forAll { (e: E) => assert { fromThrowable(toThrowable(e)) == e } }
  //
  // This ensures that a round trip through parallel transport is the identity.

  behavior of "errorString"

  it should "preserve passing values through Throwable" in {

    import Instances.errorString

    forAll { (s: String) =>
      fromThrowable(toThrowable(s)) shouldBe s
    }
  }

  behavior of "errorException"

  it should "preserve passing values through Throwable" in {

    import Instances.errorException

    forAll { (s: Exception) =>
      fromThrowable(toThrowable(s)) shouldBe s
    }
  }

  behavior of "errorThrowable"

  it should "preserve passing values thorugh Throwable" in {

    import Instances.errorThrowable

    forAll { (s: Throwable) =>
      fromThrowable(toThrowable(s)) shouldBe s
    }
  }
}
