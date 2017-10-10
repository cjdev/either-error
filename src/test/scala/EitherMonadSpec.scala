// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class EitherMonadSpec extends FlatSpec with Matchers with PropertyChecks {

  // The EitherMonad interface provides automatic marshalling/unmarshalling
  // combinators for functions and values that have the possibility of failure.

  // imports `safely', `ensure', `failure', `safe', `lift', `bind', `traverse',
  // `sequence', `successes', and extra instance methods for `Either[*,*]'
  import com.cj.eithererror.EitherMonad._

  // Use whatever type to represent errors.
  case class Err(msg: String, code: Int)

  // Define an implicit ErrorC instance
  // so that `safely', `ensure', and `failure' work.
  import com.cj.eithererror.ErrorC
  implicit val errorErr: ErrorC[Err] = new ErrorC[Err] {
    def fromMessage(msg: String): Err = Err(msg, msg.length)
  }

  // Alternatively, you can import (exactly) one of the default instances,
  // `import ErrorC.Instances.errorString' or
  // `import ErrorC.Instances.errorException' or
  // `import ErrorC.Instances.errorThrowable' or
  // `import ErrorC.Instances.messageAndCause' or
  // `import ErrorC.Instances.classNameAndMessage'.

  // Warning: defining (or importing) more than one implicit ErrorC instance
  // will wreck type inference! (But other than that, everything will be okay.)

  // Here are some types for our business logic
  class Foo
  class Bar extends Foo
  class Baz

  // In what follows, I'm going to abuse the FlatSpec machinery when convenient.

  "`failure'" should "be used to construct Left values" in {

    // call `failure' without an argument
    failure shouldBe Left(ErrorC.getDefault)

    // or call with a value of your chosen Error type
    failure(Err("Date is prior to beginning of time.", 19691231)) shouldBe
      Left(Err("Date is prior to beginning of time.", 19691231))

    // or call with a custom message
    failure("This is not an Err") shouldBe Left(ErrorC.fromMessage("This is not an Err"))

    // or call with a throwable
    val exc = new RuntimeException("This is not an Err")
    failure(exc) shouldBe Left(ErrorC.fromThrowable(exc))
  }

  "`safely'" should "be used to fence code blocks that can fail" in {

    // example success
    safely { "5".toInt } shouldBe Right(5)

    // example failure
    safely { "five".toInt } shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"five\"")
    ))

    // optionally provide a custom message or a throwable
    safely("five".toInt, "Well, shucks.") shouldBe
      Left(ErrorC.fromMessage("Well, shucks."))
  }

  "`ensure'" should "convert a boolean expression to an Either value" in {

    // example success
    ensure(1 == 1, "They were not the same!") shouldBe Right(())

    // example failure
    ensure(0 == 1, "They were not the same!") shouldBe
      Left(ErrorC.fromMessage("They were not the same!"))

    // the message is optional
    ensure { 0 == 1 } shouldBe Left(ErrorC.getDefault)
  }

  "`fold'" should "be used to destructure and branch" in {
    // given
    val yourErr = failure
    val yourFoo = Right(new Foo)
    def handleErr(err: Err): Int = 404
    def handleFoo(foo: Foo): Int = 200

    // then
    def yourConsumer(ef: Either[Err, Foo]): Int =
      ef.fold(
        err => handleErr(err),
        foo => handleFoo(foo)
      )

    // then
    yourConsumer(yourErr) shouldBe 404
    yourConsumer(yourFoo) shouldBe 200
  }

  "pattern matching" should "also work" in {
    // given
    val yourErr = failure
    val yourFoo = Right(new Foo)
    def handleErr(err: Err): Int = 404
    def handleFoo(foo: Foo): Int = 200

    // then
    def yourConsumer(ef: Either[Err, Foo]): Int =
      ef match {
        case Left(err) => handleErr(err)
        case Right(foo) => handleFoo(foo)
      }

    // then
    yourConsumer(yourErr) shouldBe 404
    yourConsumer(yourFoo) shouldBe 200
  }

  "but you" should "prefer `fold' because it enforces covering both branch" in {
    // given
    val yourErr = failure
    def handleFoo(foo: Foo): Int = 200

    // when
    // This source prints an error-level log message, yet compiles successfully
    def yourConsumer(ef: Either[Err,Foo]): Int =
      ef match {
        // It's a trap!
        case Right(foo) => handleFoo(foo)
      }

    // then
    withClue("`yourConsumer' throws because the Left branch is undefined") {
      an[Exception] should be thrownBy yourConsumer(yourErr)
    }
  }

  "`get', `getError', `getOrElse', and `getOrThrow'" should "be more accessors" in {
    // when
    val inner = new Foo
    val foo = Right(inner)
    val err = failure
    val fallback = new Bar

    // then
    err.get shouldBe 'isEmpty
    err.getError shouldBe 'nonEmpty
    err.getOrElse(fallback) shouldBe fallback
    a[Throwable] should be thrownBy err.getOrThrow

    // and
    foo.get shouldBe 'nonEmpty
    foo.getError shouldBe 'isEmpty
    foo.getOrElse(fallback) shouldBe inner
    foo.getOrThrow shouldBe inner
  }

  "`foreach'" should "be used to perform arbitrary side effects if " +
    "and only if the provided Either holds a Right value inside" in {
    // given
    val foo = Right(new Foo)
    val err = failure
    var `took off every zig` = false
    var `launched missiles` = false
    def takeOffEveryZig(): Unit = `took off every zig` = true
    def launchMissiles(): Unit = `launched missiles` = true

    // when
    foo.foreach { (f: Foo) => takeOffEveryZig() }
    err.foreach { (f: Foo) => launchMissiles() }

    // then
    withClue("The action occurs if the Either was a success:") {
      `took off every zig` shouldBe true
    }
    withClue("The action occurs only if the Either was a success:") {
      `launched missiles` shouldBe false
    }
  }

  "you" should "be able to manipulate Either values like you'd manipulate booleans" in {
    // give
    val err1 = failure
    val err2 = failure("This is not e2.")
    val foo1 = Right(new Bar)
    val foo2 = Right(new Foo)

    // then
    withClue("`or' returns the first success (or last failure if no successes)") {
      (err1 or foo1 or err2 or foo2) shouldBe foo1
      (err1 or err2) shouldBe err2
    }
    withClue("`and' returns the first failure (or last success if no failures)") {
      (foo1 and err1 and foo2 and err2) shouldBe err1
      (foo1 and foo2) shouldBe foo2
    }
  }

  "`and'" should "be useful for gating actions behind preconditions" in {
    // given
    var `reported success` = false

    def reportSuccess(): Baz = {
      `reported success` = true; new Baz
    }

    def flakeyIOAction(): Either[Err, Unit] = failure("Ran out of internet.")

    // when
    flakeyIOAction() and Right(reportSuccess())

    // then
    withClue("Since `flakeyIOAction' failed, we didn't report success: ") {
      `reported success` shouldBe false
    }
  }

  "`or'" should "be useful for failure recovery" in {
    // given
    val flakeyValue = failure
    val recovery = new Foo

    // when
    val result = flakeyValue or Right(recovery)

    // then
    result shouldBe Right(recovery)
  }

  "`recover'" should "be a more general version of `or' that allows you to " +
    "branch on the contents of the error value" in {
    // given
    val notFound = failure(Err("Not Found", 404))
    val notAllowed = failure(Err("Not Allowed", 403))
    val ok = Right("This is a normal response body.")

    // when
    def handle404(err: Err): Either[Err, String] = err match {
      case Err(_,404) => Right("This is a pretty 404 page.")
      case _ => Left(err) // pass along errors we're not handling
    }

    // then
    notFound.recover(handle404) shouldBe Right("This is a pretty 404 page.")
    notAllowed.recover(handle404) shouldBe Left(Err("Not Allowed", 403))
    ok.recover(handle404) shouldBe Right("This is a normal response body.")
  }

  // The below function fails in an expected way.

  @throws[NumberFormatException]
  def flakeyFunction(str: String): Int = str.toInt

  // Why are we using exceptions for _expected_ failures, anyway?

  // Exceptions should be exit paths for our program, not exit paths for a
  // method. The purpose of the EitherMonad idiom is to have a sane, informative
  // return type for functions that can fail in expected, non-fatal ways.

  def safeFunction(str: String): Either[Err, Int] =
    try Right(str.toInt) catch {
      case scala.util.control.NonFatal(e) => failure(e)
    }

  // The above pattern is so common that we abstract it and give it a name

  "`safely'" should "turn an `A' into an `Either[Err, A]'" in {
    // when
    def safeFunction2(str: String): Either[Err, Int] =
      safely { flakeyFunction(str) }

    // then
    safeFunction2("12") shouldBe Right(12)

    safeFunction2("twelve") shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))
  }

  "`safe'" should "turn an `A => B' into an `A => Either[Err, B]'" in {
    // when
    def safeFunction3: String => Either[Err, Int] = safe(flakeyFunction)

    // then
    safeFunction3("12") shouldBe Right(12)

    safeFunction3("twelve") shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))
  }

  // Besides exception handling, some functions fail because preconditions
  // are not met. EitherMonad has some tools for those situations.

  "you" should "be able to use the EitherMonad idiom to enforce preconditions" in {
    // when
    def withdrawCash(balance: Long, amount: Long): Either[Err, Long] =
      if (amount <= balance) Right(balance - amount)
      else failure("Insufficient Funds!")

    // then
    withdrawCash(1000, 100) shouldBe Right(900)

    withdrawCash(100, 1000) shouldBe
      Left(ErrorC.fromMessage("Insufficient Funds!"))
  }

  "`ensure'" should "be able to accomplish the same thing in a for comprehension" in {
    // when
    def withdrawCash(balance: Long, amount: Long): Either[Err, Long] =
      for {
      // `ensure' is used to gate the computation
      // even though its result is ignored
        _ <- ensure(amount <= balance, "Insufficient Funds!")
      } yield balance - amount

    // then
    withdrawCash(1000, 100) shouldBe Right(900)

    withdrawCash(100, 1000) shouldBe
      Left(ErrorC.fromMessage("Insufficient Funds!"))
  }

  "`ensure'" should "work with `and' outside of for comprehension too" in {
    // when
    def withdrawCash(balance: Long, amount: Long): Either[Err, Long] =
      ensure(amount <= balance, "Insufficient Funds!") and Right(balance - amount)

    // then
    withdrawCash(1000, 100) shouldBe Right(900)

    withdrawCash(100, 1000) shouldBe
      Left(ErrorC.fromMessage("Insufficient Funds!"))
  }

  "Either-y computations and Either-y results" should "be chainable" in {
    // given
    def readInput(str: String): Either[Err, Long] = safely { str.toInt }

    def withdrawCash(balance: Long, amount: Long): Either[Err, Long] =
      ensure(amount <= balance, "Insufficient Funds!") and Right(balance - amount)

    // when
    def atmTxn(input: String, startingBalance: Long): Either[Err, Long] =
      readInput(input).flatMap { withdrawCash(startingBalance, _) }

    // then
    atmTxn("12", 100) shouldBe Right(88)

    atmTxn("twelve", 100) shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))

    atmTxn("12", 10) shouldBe Left(ErrorC.fromMessage("Insufficient Funds!"))
  }

  "`ensure'" should "be usable in arbitrary for comprehensions" in {
    // when
    def doIfDistinct(f: (Int, Int) => Int)
                    (ea1: Either[Err, Int], ea2: Either[Err, Int]):
    Either[Err, Int] = for {
      a1 <- ea1 // stops here if ea1 is a failure
      a2 <- ea2 // stops here if ea2 is a failure
      _ <- ensure(a1 != a2, "Values not distinct!") // stops here if a1 == a2
    } yield f(a1, a2)

    // then
    doIfDistinct(_+_)(Right(12), failure) shouldBe Left(ErrorC.getDefault)

    doIfDistinct(_*_)(Right(12), Right(12)) shouldBe
      Left(ErrorC.fromMessage("Values not distinct!"))

    doIfDistinct(_/_)(Right(12), Right(4)) shouldBe Right(3)
  }

  // The above function take Either values as arguments. This is something of
  // an antipattern, since functions that accept pure arguments can always
  // operate on Either values through `map', `flatMap' and for comprehension.
  // This is part of what we mean by "automatic marshalling/unmarshalling."

  "`doIfDistinct'" should "be refactored so that it doesn't take Either args" in {
    // given
    val `12` = Right[Err,Int](12)
    val `4` = Right[Err,Int](4)

    // when
    def doIfDistinct(f: (Int, Int) => Int)(x: Int, y: Int): Either[Err, Int] =
      ensure(x != y, "Values not distinct!") and Right(f(x, y))

    // then
    withClue("You can use nested `flatMap's [img: small_brain.jpg]: ") {
      val res1 = `12`.flatMap { x =>
        `4`.flatMap { y =>
          doIfDistinct(_/_)(x, y)
        }
      }

      res1 shouldBe Right(3)
    }

    withClue("Or you can use for comprehension [img: glowing_brain.jpg]: ") {
      val res2 =
        for { x <- `12`; y <- `4`; quot <- doIfDistinct(_/_)(x, y) }
          yield quot

      res2 shouldBe Right(3)
    }

    withClue("Or you can use `bind' [img: genius_brain.jpg]: ") {
      val res3 = bind2(doIfDistinct(_/_))(`12`, `4`)

      res3 shouldBe Right(3)
    }
  }

  // `bind' is basically flatMap, but for higher-arity functions

  "`bind'" should "be used to pass Either values into a function that " +
    "expects pure arguments and returns an Either value (see signature below)" in {
    // given
    val `12` = Right(12)
    val `3` = Right(3)
    val sayPlz = failure("You didn't say the magic word!")
    val `0` = Right(0)

    val div: (Int, Int) => Either[Err, Int] = (x, y) =>
      ensure(y != 0, "Division by zero!") and Right(x / y)

    // then
    // bind: (A => Either[*,B]) => (Either[*,A] => Either[*,B])
    // bindN: ((A1,...,AN) => Either[*,B]) => ((Either[*,A1],...,Either[*,AN]) => Either[*,B])

    bind2(div)(`12`, `3`) shouldBe Right(12 / 3)

    bind2(div)(sayPlz, `3`) shouldBe
      Left(ErrorC.fromMessage("You didn't say the magic word!"))

    bind2(div)(`12`, `0`) shouldBe
      Left(ErrorC.fromMessage("Division by zero!"))
  }

  // `lift' is basically `map', but for higher-arity functions

  "`lift'" should "be used to apply a pure function to Either values" in {
    // given
    val `12` = Right(12)
    val `10` = Right(10)
    val err = failure

    val plus2 = (_: Int) + 2
    val sumTwo = (_: Int) + (_: Int)
    val sumThree = (_: Int) + (_: Int) + (_: Int)

    // then
    // lift: (A => B) => (Either[*,A] => Either[*,B])
    // liftN: ((A1,...,AN) => B) => ((Either[*,A1],...,Either[*,AN]) => Either[*,B])
    lift(plus2)(`12`) shouldBe Right(plus2(12))
    lift2(sumTwo)(`12`, `10`) shouldBe Right(sumTwo(12, 10))
    lift3(sumThree)(`12`, `10`, err) shouldBe Left(ErrorC.getDefault)
  }

  "`safe'`" should "be used to convert flakey functions into Either-y functions" in {
    // given
    val `10` = "10"
    val `12` = "12"
    val twelve = "twelve"

    // when
    // safe: (A => B) => (A => Either[*,B])
    // safeN: ((A1,...,AN) => B) => ((A1,...,AN) => Either[*,B])
    val readAndAdd = safe2 {
      (s1: String, s2: String) => s1.toInt + s2.toInt
    }

    // then
    readAndAdd(`10`, `12`) shouldBe Right(22)

    readAndAdd(`10`, twelve) shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))
  }

  "`safely'" should "accomplish the same thing at the value level" in {
    // given
    val `10` = "10"
    val `12` = "12"
    val twelve = "twelve"

    // when
    // safely: A => Either[*,A]
    def readAndAdd(s1: String, s2: String) = safely { s1.toInt + s2.toInt }

    // then
    readAndAdd(`10`, `12`) shouldBe Right(22)

    readAndAdd(`10`, twelve) shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))
  }

  "you" should "use `safely' and `ensure' judiciously in for comprehension" in {
    // given
    val `12` = "12"
    val `10` = "10"
    val twelve = "twelve"

    // when
    val result = for {
      (x, y) <- safely { (`12`.toInt, `10`.toInt) }
      _      <- ensure(x >= y, "Subtracting a larger from a smaller!")
      diff    = x - y
      z      <- safely { twelve.toInt }
      _      <- ensure(z != 0, "Dividing by zero!")
      quot    = diff / z
    } yield quot

    // then
    result shouldBe Left(ErrorC.fromThrowable(
      new NumberFormatException("For input string: \"twelve\"")
    ))
  }

  // In summary:
  //   (1) Write pure methods (e.g., `A => B') whenever possible;
  //   (2) If your method might fail, accept pure values and return Either
  //       values (e.g, `A => Either[*,B]') to reflect failure in the signature;
  //   (3) `safely' is easy-mode exception/null handling;
  //   (4) `ensure' is easy-mode precondition enforcing;
  //   (5) Don't unwrap your Either values until the very end of your program,
  //       since Either values can be used wherever pure values are expected
  //       (using `map'/`lift' and `flatMap'/`bind').

  // Now, for `traverse', `sequence', and `successes'.

  // `traverse' and `sequence' have funky type signatures, but that's only
  // because they are written to be as general as possible. They are easier to
  // understand when you specialize them to lists.

  // sequence: List[Either[*,A]] => Either[*,List[A]]

  "`sequence'" should "be used to factor Either-ness out of a List" in {
    // given
    val eithers: List[Either[Err, Int]] =
      List(Right(1), Right(2), Right(3))

    // then
    // sequence: List[Either[*,A]] => Either[*,List[A]]
    withClue("Succeeds if each list elem is a success: ") {
      sequence(eithers) shouldBe Right(List(1, 2, 3))
    }
    withClue("Succeeds only if each list elem is a success: ") {
      sequence(eithers :+ failure("oops")) shouldBe
        Left(ErrorC.fromMessage("oops"))
    }
  }

  // traverse: List[A] => (A => Either[*,B]) => Either[*,List[B]]

  "`traverse'" should "be used to apply an Either-y function to each " +
    "element of a list while collecting the Either-ness outside the list" in {
    // given
    val ints = List("1", "2", "3")
    val readInt = safe { (n: String) => n.toInt }

    // then
    // traverse: List[A] => (A => Either[*,B]) => Either[*,List[B]]
    withClue("Succeeds if each application succeeds: ") {
      traverse(ints)(readInt) shouldBe Right(List(1, 2, 3))
    }
    withClue("Succeeds only if each application succeeds: ") {
      traverse(ints :+ "twelve")(readInt) shouldBe Left(ErrorC.fromThrowable(
        new NumberFormatException("For input string: \"twelve\"")
      ))
    }
  }

  // successes: List[Either[*,A]] => List[A]

  "`successes'" should "be use with the built-in `map' method for your " +
    "collection if you want to keep all the successes and discard any failures" in {
    // given
    val ints = List("10", "11", "twelve", "13")

    // then
    successes(ints.map(safe(_.toInt))) shouldBe List(10, 11, 13)
  }

  "`mapLeft'" should "turn an `Either[E,*]' into an `Either[F,*]' when you " +
  "provide a mapping function E => F" in {
    withClue("Successes should not be changed: ") {
      safely("foo").mapLeft((e: Err) => e.toString) shouldBe Right("foo")
    }
    withClue("Failures should get mapped: ") {
      val err: Err = Err("bar", 13)
      failure(err).mapLeft(_.toString) shouldBe Left(err.toString)
    }
  }

  "`translate'" should "turn an `Either[E,*]' into an `Either[F,*]' without " +
  "you needing to provide a mapping function E => F" in {
    // given
    implicit val errorInt: ErrorC[Int] = new ErrorC[Int] {
      def fromMessage(msg: String): Int = msg.length
    }
    implicit val errorChar: ErrorC[Char] = new ErrorC[Char] {
      def fromMessage(msg: String): Char = msg.headOption.getOrElse('\0')
    }

    // then
    withClue("Successes should not be changed: ") {
      safely[Int, String]("foo").translate[Char] shouldBe Right("foo")
    }
    withClue("Failures should be recast: ") {
      failure[Int, String](1).translate[Char] shouldBe
        Left(ErrorC.fromThrowable[Char](ErrorC.toThrowable[Int](1)))
    }
  }
}
