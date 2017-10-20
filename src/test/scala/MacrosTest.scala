// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import org.scalatest.{FlatSpec, Matchers}

class MacrosTest extends FlatSpec with Matchers {

  import com.cj.eithererror.Macros._

  "LINE" should "be visible and return the line number it appears on" in {
    // given
    val line12 = LINE
    val line13 = LINE

    // then
    line12 shouldBe 12
    line13 shouldBe 13
  }

  "FILE" should "be visible and return the name of the file it appears in" in {
    FILE should endWith("MacrosTest.scala")
  }

  "CLASS" should "give you the name of the enclosing class" in {
    // given
    case class Foo() { def classname: String = CLASS }
    case class Bar[A]() { def classname: String = CLASS }

    // then
    Foo().classname shouldBe "Foo"
    Bar[Char]().classname shouldBe "Bar"
  }

  "LINE, CLASS, and FILE" should "work" in {
    println(s"[$FILE:$CLASS:$LINE]: something went /right/ for a change")
  }
}
