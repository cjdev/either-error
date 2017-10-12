# either-error

Generic Error Handling in Scala

Copyright (c) 2017 CJ Engineering under the terms of the MIT License
See 'LICENSE' in project root.

## Purpose

Poised as a minimal alternative to `scala.util.Try` for when you need the
flexibility to represent errors as some type other than Throwable. And because I
got tired of copying/pasting these classes into all my other projects.

Scala 2.12's monad operations for `Either` largely obviate the need for this
library, as do scalaz's and cats'.

## Goals

The goals are chosen to support the needs of a dynamic software engineering
environment. Specifically, we hope the library:

- has zero dependencies,
- exposes a stable interface,
- integrates well with `Predef`, and
- is generic enough to require only infrequent updates.

## Minimal Setup

In your 'pom.xml':

```xml
<dependency>
    <groupId>com.cj</groupId>
    <artifactId>either-error_2.11</artifactId>
    <version>1.1.0</version>
</dependency>
```

In your scala source file:

```scala
import com.cj.eithererror.{ErrorC, EitherMonad => EM}
import EM.EitherMonadInstance // decorates Either values with additional methods

// and import or create exactly one `ErrorC` instance for your chosen error type
import ErrorC.Instances.errorString
// or
import ErrorC.Instances.errorThrowable
// or
class Foo
implicit val errorFoo: ErrorC[Foo] = new ErrorC[Foo] {
  def fromMessage(msg: String): Foo = ...
}

// The entry points are `EM.safely`, `EM.ensure`, and `EM.failure`.

val err = new Exception("err")
EM.safely(throw err) // returns Left(ErrorC.fromThrowable(err))

val msg = "The condition wasn't true"
EM.ensure(1 < 0, msg) // returns Left(ErrorC.fromMessage(msg))

EM.failure // returns Left(ErrorC.getDefault)

// Chain computations together with short-circuit logic
def `launch the missiles`: Unit = ...
EM.safely("12".toInt)                     // returns Right(12)
  .flatMap((n: Int) => EM.ensure(n > 15)) // returns Left(ErrorC.getDefault)
  .and(Right(`launch the missiles`))      // doesn't launch the missiles
```

## Documentation

See documenting comments and tests in 'test/scala/EitherMonadSpec.scala'.
