package com.cj.eithererror
package javaapi

import java.util.function.{ Supplier, Function => JFunction }

class EitherContext[E](val es: ErrorStrategy[E]) { context =>

  import EitherMonad.EitherMonadInstance

  private implicit val instance: ErrorC[E] = ErrorStrategy.toScala(es)

  final def safely[A](a: Supplier[A], alt: Object): Either[A] =
    new context.Either[A](EitherMonad.safely[E, A](a.get, alt))

  final def safely[A](a: Supplier[A]): Either[A] =
    new context.Either[A](EitherMonad.safely[E, A](a.get))

  final def ensure(p: Boolean, alt: Object): Either[Void] =
    new context.Either[Void](EitherMonad.ensure(p, alt).map(_ => null))

  final def ensure(p: Boolean): Either[Void] =
    new context.Either[Void](EitherMonad.ensure(p).map(_ => null))

  final def failure[A](alt: Object): Either[A] =
    new context.Either[A](EitherMonad.failure(alt))

  final def failure[A](): Either[A] =
    new context.Either[A](EitherMonad.failure)

  final class Either[A] private[javaapi] (private[javaapi] val repr: scala.util.Either[E, A]) {

    def fold[X](withLeft: JFunction[E, X], withRight: JFunction[A, X]): X =
      repr.fold(e => withLeft.apply(e), a => withRight.apply(a))

    def map[B](f: JFunction[A, B]): context.Either[B] =
      new context.Either[B](repr.map(a => f.apply(a)))

    def flatMap[B](k: JFunction[A, context.Either[B]]): context.Either[B] =
      new context.Either[B](repr.fold(e => Left(e), a => k.apply(a).repr))

    def and[B](eb: context.Either[B]): context.Either[B] =
      new context.Either[B](repr.and(eb.repr))

    def or[B >: A](eb: context.Either[B]): context.Either[B] =
      new context.Either[B](repr.or(eb.repr))

    def recover[B >: A](f: JFunction[E, context.Either[B]]): context.Either[B] =
      new context.Either[B](repr.recover(e => f.apply(e).repr))
  }
}
