// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ErrorCTest extends FlatSpec with Matchers with PropertyChecks {

  import com.cj.eithererror.ErrorC._

  "errorString" should "preserve passing values through Throwable" in {

    import Instances.errorString

    forAll { (msg: String) =>
      fromThrowable(toThrowable(msg)) shouldBe msg
    }
  }

  "errorException" should "preserve passing values through Throwable" in {

    import Instances.errorException

    forAll { (err: Exception) =>
      fromThrowable(toThrowable(err)) shouldBe err
    }
  }

  "errorThrowable" should "preserve passing values through Throwable" in {

    import Instances.errorThrowable

    forAll { (err: Throwable) =>
      fromThrowable(toThrowable(err)) shouldBe err
    }
  }

  "classNameAndMessage" should "fail to preserve values through Throwable" in {

    import Instances.throwingClassAndMessage

    val msg = ""
    fromThrowable(toThrowable(msg)) shouldNot be(msg)
  }

  "messageAndCause" should "preserve passing values through Throwable" in {

    import Instances.messageAndCause

    forAll { (msg: String, errs: Option[Throwable]) =>
      fromThrowable(toThrowable((msg, errs))) shouldBe (msg, errs)
    }
  }

//  "classAndLine" should "fail to preserve values through Throwable" in {
//
//    import Instances.classAndLine
//
//    val msg = ""
//    fromThrowable(toThrowable(msg)) shouldNot be(msg)
//  }
//
//  "classAndLine" should "prepend the class name and line number to messages" in {
//
//    import Instances.classAndLine
//
//    val msg = "foo bar baz"
//    fromMessage(msg) shouldBe Left(s"[ErrorCTest:68] $msg")
//  }
}
