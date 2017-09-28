# either-error

Generic Error Handling in Scala

Copyright (c) 2017 CJ Engineering under the terms of the MIT License
See 'LICENSE' in project root.

## Purpose

Poised as a minimal alternative to `scala.util.Try` for when you need the
flexibility to represent errors as some type other than Throwable.

Scala 2.12's monad operations for `Either` largely obviates the need for this
library.

And because I got tired of copying/pasting these classes into all my other
projects.

## Goals

The goals are chosen to support the needs of a dynamic software engineering
environment. Specifically, we hope the library:

- has zero dependencies,
- exposes a stable interface,
- is generic enough to require only infrequent updates.

## Minimal Setup

In your 'pom.xml':

```xml
<dependency>
    <groupId>com.cj</groupId>
    <artifactId>either-error_2.11</artifactId>
    <version>1.0.0</version>
</dependency>
```

In your scala source file:

```scala
import com.cj.eithererror.{ErrorC, EitherMonad => EM}
import EM.EitherMonadInstance

// and import or create exactly one `Error` instance for your chosen error type
import ErrorC.Instances.errorString
// or
import ErrorC.Instances.errorThrowable
// or
type Foo
implicit val errorFoo: ErrorC[Foo] = new ErrorC[Foo] {
  def fromMessage(msg: String): Foo = ...
}
```

The entry points are `EM.safely`, `EM.ensure`, and `EM.failure`.

## Documentation

See documenting comments and tests in
'test/scala/com/cj/eithererror/EitherMonadDoc.scala'.
