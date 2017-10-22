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

  "FL" should "decorate a string with the file name and line number" in {
    val msg: String = "awesome message"
    val int: Int = scala.util.Random.shuffle(0 to 10).head
    FL(s"$msg $int") shouldBe s"[$FILE:$LINE]: $msg $int"
  }

  "CL" should "decorate a string with the class name and line number" in {
    val msg: String = "awesome message"
    val int: Int = scala.util.Random.shuffle(0 to 10).head
    CL(s"$msg $int") shouldBe s"[$CLASS:$LINE]: $msg $int"
  }

  "FCL" should "decorate a string with the file name, class name, and line number" in {
    val msg: String = "awesome message"
    val int: Int = scala.util.Random.shuffle(0 to 10).head
    FCL(s"$msg $int") shouldBe s"[$FILE:$CLASS:$LINE]: $msg $int"
  }

  "LINE, CLASS, and FILE" should "work" in {
    println(FCL("something went /right/ for a change"))
  }
}
