# either-error

Generic Error Handling in Scala.

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
    <version>2.0.0</version>
</dependency>
```

In your scala source file:

```scala
import com.cj.eithererror._, EitherMonad._, ErrorC.Instances.thrownClassAndMessage
```

## Usage Example

The entry points are `safely`, `ensure`, and `failure`:

```scala
val err = new Exception("err")
safely(throw err) // returns Left(ErrorC.fromThrowable(err))

val msg = "The condition wasn't true"
ensure(1 < 0, msg) // returns Left(ErrorC.fromMessage(msg))

failure // returns Left(ErrorC.getDefault)
```

Chain computations together with short-circuit logic:

```scala
def `launch the missiles`: Unit = ...
safely("12".toInt)                     // returns Right(12)
  .flatMap((n: Int) => ensure(n > 15)) // returns Left(ErrorC.getDefault)
  .and(Right(`launch the missiles`))   // doesn't launch the missiles
```

## Detailed Explanation

The `eithererror` package exposes the `ErrorC` type class and the `EitherMonad`
module. Import the contents of the module to expose extra constructors and
decorator methods for `scala.util.Either`.

Import exactly one `ErrorC` instance from `ErrorC.Instances` or create your own
custom `ErrorC` instance for any type (but not both).

```scala
class FooErr { ... }
implicit val errorFooErr: ErrorC[FooErr] = new ErrorC[FooErr] {
  def fromMessage(msg: String): FooErr = ...
}
```

or simply

```scala
import ErrorC.Instances.messageAndCause
```

## Documentation

See documenting comments and tests in 'test/scala/EitherMonadSpec.scala'.
