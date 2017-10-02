// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.collection.generic.CanBuildFrom
import scala.collection.{TraversableLike, mutable}

object EitherMonad {

  def safely[E: ErrorC, A](a: => A, alt: => Any): Either[E, A] =
    safelyPrimitive(a).left.map(_ => coerceAlt(alt))

  def safely[E: ErrorC, A](a: => A): Either[E, A] =
    safelyPrimitive(a).left.map(e => ErrorC.fromThrowable(e))

  def ensure[E: ErrorC](p: Boolean, alt: => Any): Either[E, Unit] =
    ensurePrimitive(p).left.map(_ => coerceAlt(alt))

  def ensure[E: ErrorC](p: Boolean): Either[E, Unit] =
    ensurePrimitive(p).left.map(_ => ErrorC.getDefault)

  def failure[E: ErrorC, A](alt: Any): Either[E, A] =
    Left(coerceAlt(alt))

  def failure[E: ErrorC, A]: Either[E, A] =
    Left(ErrorC.getDefault)

  implicit class EitherMonadInstance[E, A](val self: Either[E, A]) {

    def get: Option[A] =
      self.fold(_ => None, Option(_))

    def getError: Option[E] =
      self.fold(Option(_), _ => None)

    def getOrElse(a: A): A =
      self.fold(_ => a, identity)

    @throws[Throwable]("Throws if instance is a Left Either.")
    def getOrThrow(implicit ev: ErrorC[E]): A =
      self.fold(e => throw ErrorC.toThrowable(e), identity)

    def foreach(f: A => Unit): Unit =
      self.fold(_ => {}, a => f(a))

    def map[B](f: A => B): Either[E, B] =
      self.fold(e => Left(e), a => Right(f(a)))

    def flatMap[B](k: A => Either[E, B]): Either[E, B] =
      self.fold(e => Left(e), a => k(a))

    def filter(p: A => Boolean)(implicit ev: ErrorC[E]): Either[E, A] =
      flatMap(a => if (p(a)) Right(a) else failure)

    def withFilter(p: A => Boolean)(implicit ev: ErrorC[E]): Either[E, A] =
      filter(p)

    def ap[B](ef: Either[E, A => B]): Either[E, B] =
      for {f <- ef; a <- self} yield f(a)

    def flatten[B](implicit ev: A <:< Either[E, B]): Either[E, B] =
      flatMap(ev)

    def and[B](eb: => Either[E, B]): Either[E, B] =
      flatMap(_ => eb)

    def or[B >: A](eb: => Either[E, B]): Either[E, B] =
      recover(_ => eb)

    def recover[B >: A](f: E => Either[E, B]): Either[E, B] =
      self.fold(err => f(err), Right(_))

    def mapLeft[F](f: E => F): Either[F, A] =
      self.fold(e => Left(f(e)), Right(_))
  }

  def safe[E: ErrorC, A, X](f: A => X): A => Either[E, X] =
    a => safely(f(a))

  def safe2[E: ErrorC, A, B, X](f: (A, B) => X):
  (A, B) => Either[E, X] =
    (a, b) => safely(f(a, b))

  def safe3[E: ErrorC, A, B, C, X](f: (A, B, C) => X):
  (A, B, C) => Either[E, X] =
    (a, b, c) => safely(f(a, b, c))

  def safe4[E: ErrorC, A, B, C, D, X](f: (A, B, C, D) => X):
  (A, B, C, D) => Either[E, X] =
    (a, b, c, d) => safely(f(a, b, c, d))

  def lift[E, A, X](f: A => X): Either[E, A] => Either[E, X] =
    _.map(f)

  def lift2[E, A, B, X](f: (A, B) => X):
  (Either[E, A], Either[E, B]) => Either[E, X] =
    (ea, eb) =>
      for { a <- ea; b <- eb }
        yield f(a, b)

  def lift3[E, A, B, C, X](f: (A, B, C) => X):
  (Either[E, A], Either[E, B], Either[E, C]) => Either[E, X] =
    (ea, eb, ec) =>
      for { a <- ea; b <- eb; c <- ec }
        yield f(a, b, c)

  def lift4[E, A, B, C, D, X](f: (A, B, C, D) => X):
  (Either[E, A], Either[E, B], Either[E, C], Either[E, D]) => Either[E, X] =
    (ea, eb, ec, ed) =>
      for { a <- ea; b <- eb; c <- ec; d <- ed }
        yield f(a, b, c, d)

  def bind[E, A, X](k: A => Either[E, X]): Either[E, A] => Either[E, X] =
    _.flatMap(k)

  def bind2[E, A, B, X](k: (A, B) => Either[E, X]):
  (Either[E, A], Either[E, B]) => Either[E, X] =
    (ea, eb) =>
      for { a <- ea; b <- eb; res <- k(a,b) }
        yield res

  def bind3[E, A, B, C, X](k: (A, B, C) => Either[E, X]):
  (Either[E, A], Either[E, B], Either[E, C]) => Either[E, X] =
    (ea, eb, ec) =>
      for { a <- ea; b <- eb; c <- ec; res <- k(a, b, c) }
        yield res

  def bind4[E, A, B, C, D, X](k: (A, B, C, D) => Either[E, X]):
  (Either[E, A], Either[E, B], Either[E, C], Either[E, D]) => Either[E, X] =
    (ea, eb, ec, ed) =>
      for { a <- ea; b <- eb; c <- ec; d <- ed; res <- k(a, b, c, d) }
        yield res

  def traverse[E, A, TA, B, TB](ta: TraversableLike[A, TA])
                               (k: A => Either[E, B])
                               (implicit bf: CanBuildFrom[TA, B, TB]):
  Either[E, TB] = {

    type Bldr = mutable.Builder[B, TB]

    val init: Either[E, Bldr] =
    { val bldr = bf(ta.asInstanceOf[TA]); bldr.sizeHint(ta); Right(bldr) }

    def step(acc: Either[E, Bldr], next: A): Either[E, Bldr] =
      for { bldr <- acc; b <- k(next) } yield bldr += b

    ta.foldLeft(init)(step).map(_.result)
  }

  def sequence[E, A, TEA, TA](tea: TraversableLike[Either[E, A], TEA])
                             (implicit bf: CanBuildFrom[TEA, A, TA]):
  Either[E, TA] =
    traverse(tea)(identity)

  def successes[E, A, TEA, TA](tea: TraversableLike[Either[E, A], TEA])
                              (implicit bf: CanBuildFrom[TEA, A, TA]):
  TA = {

    type Bldr = mutable.Builder[A, TA]

    val init: Bldr = bf(tea.asInstanceOf[TEA])

    def step(bldr: Bldr, next: Either[E, A]): Bldr =
      next.fold(_ => bldr, a => bldr += a)

    tea.foldLeft(init)(step).result()
  }

  private val nullValueMsg: String = "EitherMonad.safely: value was null"

  private def safelyPrimitive[A](a: => A): Either[Throwable, A] =
    try {
      val maybeNull = a // evaluating `a' might throw or might return `null'
      if (maybeNull != null) Right(maybeNull)
      else throw new NoSuchElementException(nullValueMsg)
    } catch {
      case scala.util.control.NonFatal(e) => Left(e)
    }

  private def ensurePrimitive[A](p: Boolean): Either[Unit, Unit] =
    if (p) Right(()) else Left(())

  private def coerceAlt[E: ErrorC](alt: Any): E =
    alt match {
      case e: E => e
      case msg: String => ErrorC.fromMessage(msg)
      case err: Throwable => ErrorC.fromThrowable(err)
      case _ => ErrorC.fromMessage(alt.toString)
    }
}
