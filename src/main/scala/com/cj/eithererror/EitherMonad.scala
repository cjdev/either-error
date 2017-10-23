// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.collection.generic.CanBuildFrom
import scala.collection.{TraversableLike, mutable}
import scala.util.Try

object EitherMonad {

  import ErrorC._

  /**
    * Perform the supplied computation and construct an [[Either]], catching
    * thrown [[Throwable]]s and performing a null check so that the value of a
    * [[Right]] result is guaranteed non-null. The `alt` parameter is used to
    * construct an error value in case the computation fails.
    */
  def safely[E: ErrorC, A](a: => A, alt: => Any): Either[E, A] =
    safelyPrimitive(a).left.map(_ => coerce(alt))

  /**
    * Perform the supplied computation and construct an [[Either]], catching
    * thrown [[Throwable]]s and performing a null check so that the value of a
    * [[Right]] result is guaranteed non-null.
    */
  def safely[E: ErrorC, A](a: => A): Either[E, A] =
    safelyPrimitive(a).left.map(err => coerce(err))

  /**
    * Represent a [[Boolean]] condition as an [[Either]]. Useful for enforcing
    * preconditions, e.g. within a for comprehension block. The `alt` parameter
    * is used to construct an error value in case the assertion is false.
    */
  def ensure[E: ErrorC](p: => Boolean, alt: => Any): Either[E, Unit] =
    ensurePrimitive(p).left.map(_ => coerce(alt))

  /**
    * Represent a [[Boolean]] condition as an [[Either]]. Useful for enforcing
    * preconditions, e.g. within a for comprehension block.
    */
  def ensure[E: ErrorC](p: => Boolean): Either[E, Unit] =
    ensurePrimitive(p).left.map(err => coerce(err))

  /**
    * Construct a [[Left]] value using the supplied `alt` parameter.
    */
  def failure[E: ErrorC, A](alt: => Any): Either[E, A] = Left(coerce(alt))

  /**
    * Construct a [[Left]] value using [[ErrorC.getDefault]].
    */
  def failure[E: ErrorC, A]: Either[E, A] = Left(coerce(null))

  /**
    * Provides `toEither` method on [[Option]].
    */
  implicit class OptionToEither[E: ErrorC, A](self: Option[A]) {
    def toEither: Either[E, A] = safely(self.get)
  }

  /**
    * Provides `toEither` method on [[scala.util.Try]].
    */
  implicit class TryToEither[E: ErrorC, A](self: Try[A]) {
    def toEither: Either[E, A] = safely(self.get)
  }

  /**
    * Provides combinators and convenience methods to [[Either]] similar to
    * those found in the [[scala.collection]] library.
    */
  implicit class EitherMonadInstance[E: ErrorC, A](self: Either[E, A]) {

    def getError: Option[E] = self.fold(Option(_), _ => None)

    def getValue: Option[A] = self.fold(_ => None, Option(_))

    def getOrElse(a: => A): A = self.fold(_ => a, identity)

    @throws[Throwable]("Throws if this instance is a Left instance.")
    def getOrThrow: A = self.fold(e => throw toThrowable(e), identity)

    def foreach(f: A => Unit): Unit = self.fold(_ => (), a => f(a))

    def map[B](f: A => B): Either[E, B] = flatMap(a => safely(f(a)))

    def flatMap[B](k: A => Either[E, B]): Either[E, B] =
      self.fold(Left(_), a => k(a))

    def filter(p: A => Boolean): Either[E, A] =
      flatMap(a => if (p(a)) Right(a) else Left(getDefault))

    def withFilter(p: A => Boolean): Either[E, A] = filter(p)

    def ap[B](ef: Either[E, A => B]): Either[E, B] =
      for { f <- ef; a <- self } yield f(a)

    def flatten[B](implicit id: A <:< Either[E, B]): Either[E, B] = flatMap(id)

    def and[B](eb: => Either[E, B]): Either[E, B] = flatMap(_ => eb)

    def or[B >: A](eb: => Either[E, B]): Either[E, B] = recover(_ => eb)

    def recover[B >: A](f: E => Either[E, B]): Either[E, B] =
      self.fold(e => f(e), Right(_))

    def mapLeft[F](f: E => F): Either[F, A] =
      self.fold(e => Left(f(e)), Right(_))

    def translate[F: ErrorC]: Either[F, A] =
      self.fold(e => Left(fromThrowable[F](toThrowable(e))), Right(_))

    def toOption: Option[A] = getValue

    def toTry: Try[A] = Try(getOrThrow)
  }

  /**
    * The [[safe]] series decorates a function to return an [[Either]] value,
    * catching throws and checking nulls.
    */
  def safe[E: ErrorC, A, X](f: A => X): A => Either[E, X] = a => safely(f(a))

  /**
    * The [[safe]] series decorates a function to return an [[Either]] value,
    * catching throws and checking nulls.
    */
  def safe2[E: ErrorC, A, B, X](f: (A, B) => X):
  (A, B) => Either[E, X] = (a, b) => safely(f(a, b))

  /**
    * The [[safe]] series decorates a function to return an [[Either]] value,
    * catching throws and checking nulls.
    */
  def safe3[E: ErrorC, A, B, C, X](f: (A, B, C) => X):
  (A, B, C) => Either[E, X] = (a, b, c) => safely(f(a, b, c))

  /**
    * The [[safe]] series decorates a function to return an [[Either]] value,
    * catching throws and checking nulls.
    */
  def safe4[E: ErrorC, A, B, C, D, X](f: (A, B, C, D) => X):
  (A, B, C, D) => Either[E, X] = (a, b, c, d) => safely(f(a, b, c, d))

  /**
    * The [[lift]] series decorates a function to accept and return [[Either]]
    * values, catching throws and checking nulls.
    */
  def lift[E: ErrorC, A, X](f: A => X): Either[E, A] => Either[E, X] = _.map(f)

  /**
    * The [[lift]] series decorates a function to accept and return [[Either]]
    * values, catching throws and checking nulls.
    */
  def lift2[E: ErrorC, A, B, X](f: (A, B) => X):
  (Either[E, A], Either[E, B]) => Either[E, X] =
    (ea, eb) => for { a <- ea; b <- eb } yield f(a, b)

  /**
    * The [[lift]] series decorates a function to accept and return [[Either]]
    * values, catching throws and checking nulls.
    */
  def lift3[E: ErrorC, A, B, C, X](f: (A, B, C) => X):
  (Either[E, A], Either[E, B], Either[E, C]) => Either[E, X] =
    (ea, eb, ec) => for { a <- ea; b <- eb; c <- ec } yield f(a, b, c)

  /**
    * The [[lift]] series decorates a function to accept and return [[Either]]
    * values, catching throws and checking nulls.
    */
  def lift4[E: ErrorC, A, B, C, D, X](f: (A, B, C, D) => X):
  (Either[E, A], Either[E, B], Either[E, C], Either[E, D]) => Either[E, X] =
    (ea, eb, ec, ed) =>
      for { a <- ea; b <- eb; c <- ec; d <- ed } yield f(a, b, c, d)

  /**
    * The [[bind]] series decorates a function that returns an [[Either]] value
    * to also accept [[Either]] values.
    */
  def bind[E: ErrorC, A, X](k: A => Either[E, X]):
  Either[E, A] => Either[E, X] = _.flatMap(k)

  /**
    * The [[bind]] series decorates a function that returns an [[Either]] value
    * to also accept [[Either]] values.
    */
  def bind2[E: ErrorC, A, B, X](k: (A, B) => Either[E, X]):
  (Either[E, A], Either[E, B]) => Either[E, X] =
    (ea, eb) => for { a <- ea; b <- eb; res <- k(a,b) } yield res

  /**
    * The [[bind]] series decorates a function that returns an [[Either]] value
    * to also accept [[Either]] values.
    */
  def bind3[E: ErrorC, A, B, C, X](k: (A, B, C) => Either[E, X]):
  (Either[E, A], Either[E, B], Either[E, C]) => Either[E, X] =
    (ea, eb, ec) =>
      for { a <- ea; b <- eb; c <- ec; res <- k(a, b, c) } yield res

  /**
    * The [[bind]] series decorates a function that returns an [[Either]] value
    * to also accept [[Either]] values.
    */
  def bind4[E: ErrorC, A, B, C, D, X](k: (A, B, C, D) => Either[E, X]):
  (Either[E, A], Either[E, B], Either[E, C], Either[E, D]) => Either[E, X] =
    (ea, eb, ec, ed) =>
      for { a <- ea; b <- eb; c <- ec; d <- ed; res <- k(a, b, c, d) } yield res

  /**
    * Visit each member of a [[TraversableLike]] collection, applying the
    * supplied function and collecting the results into an [[Either]] value
    * containing the collection of results or the first error.
    *
    * Best understood as specialized to [[List]]:
    * `traverse: (List[A], A => Either[*, B ]) => Either[*, List[B] ]`
    */
  def traverse[E: ErrorC, A, TA, B, TB]
              (ta: TraversableLike[A, TA])(f: A => Either[E, B])
              (implicit bf: CanBuildFrom[TA, B, TB]):
  Either[E, TB] = {

    type Bldr = mutable.Builder[B, TB]

    val init: Either[E, Bldr] =
    { val bldr = bf(ta.asInstanceOf[TA]); bldr.sizeHint(ta); Right(bldr) }

    def step(acc: Either[E, Bldr], next: A): Either[E, Bldr] =
      for { bldr <- acc; b <- f(next) } yield bldr += b

    ta.foldLeft(init)(step).map(_.result)
  }

  /**
    * Transpose a [[TraversableLike]] collection of [[Either]] values, resulting
    * in an [[Either]] value containing the collection of unwrapped values or
    * the first error.
    *
    * Best understood as specialized to [[List]]:
    * `sequence: List[ Either[*, A ] ] => Either[*, List[A] ]`
    */
  def sequence[E: ErrorC, A, TEA, TA]
              (tea: TraversableLike[Either[E, A], TEA])
              (implicit bf: CanBuildFrom[TEA, A, TA]):
  Either[E, TA] = traverse(tea)(identity)

  /**
    * From a [[TraversableLike]] collection of [[Either]] values, returns the
    * collection of unwrapped [[Right]] values, discarding any [[Left]] values.
    *
    * Best understood as specialized to [[List]]:
    * `successes: List[ Either[*, A ] ] => List[A]`
    */
  def successes[E: ErrorC, A, TEA, TA]
               (tea: TraversableLike[Either[E, A], TEA])
               (implicit bf: CanBuildFrom[TEA, A, TA]):
  TA = {

    type Bldr = mutable.Builder[A, TA]

    val init: Bldr = bf(tea.asInstanceOf[TEA])

    def step(bldr: Bldr, next: Either[E, A]): Bldr =
      next.fold(_ => bldr, a => bldr += a)

    tea.foldLeft(init)(step).result()
  }

  private val nullValueMsg: String = "EitherMonad.safely: value was null"
  private val ensureFailMsg: String = "EitherMonad.ensure: condition was false"

  private def safelyPrimitive[A](a: => A): Either[Throwable, A] =
    try {
      val maybeNull = a // evaluating `a' might throw or might return `null'
      if (maybeNull != null) Right(maybeNull)
      else throw new NoSuchElementException(nullValueMsg)
    } catch {
      case scala.util.control.NonFatal(e) => Left(e)
    }

  private def ensurePrimitive(p: => Boolean): Either[Throwable, Unit] =
    safelyPrimitive(p) match {
      case Left(err) => Left(err)
      case Right(bool) => if (bool) Right(())
        else Left(new AssertionError(ensureFailMsg))
    }

  private def coerce[E: ErrorC](alt: => Any): E =
    safelyPrimitive(alt) match {
      case Left(_) => getDefault
      case Right(any) => any match {
        case e: E => e
        case err: Throwable => fromThrowable(err)
        case msg: String => fromMessage(msg)
        case _ => fromMessage(alt.toString)
      }
    }
}
