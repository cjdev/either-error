// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.
package com.cj.eithererror
package javaapi

import java.util.Optional
import java.util.function.{Consumer, Function => JFunction}

private[javaapi] object Cnv {

  val string: ErrorC[String] = ErrorC.Instances.errorString
  val exception: ErrorC[Exception] = ErrorC.Instances.errorException
  val throwable: ErrorC[Throwable] = ErrorC.Instances.errorThrowable

  def cnv[E](es: ErrorStrategy[E]): ErrorC[E] =
    new ErrorC[E] {
      def fromMessage(msg: String): E = es.fromMessage(msg)
      override def getDefault: E = es.getDefault
      override def fromThrowable(err: Throwable): E = es.fromThrowable(err)
      override def toThrowable(e: E): Throwable = es.toThrowable(e)
    }

  def cnv[E](ec: ErrorC[E]): ErrorStrategy[E] =
    new ErrorStrategy[E] {
      def getDefault: E = ec.getDefault
      def fromThrowable(err: Throwable): E = ec.fromThrowable(err)
      def toThrowable(e: E): Throwable = ec.toThrowable(e)
      def fromMessage(msg: String): E = ec.fromMessage(msg)
    }

  def cnv[A](scalaOption: Option[A]): Optional[A] =
    scalaOption match {
      case None => Optional.empty()
      case Some(a) => Optional.of(a)
    }

  def cnv[A, B](f: A => B): JFunction[A, B] =
    new JFunction[A, B] { def apply(a: A): B = f(a) }

  def cnv[A, B](f: JFunction[A, B]): A => B =
    (a: A) => f.apply(a)

  def cnv[A](f: Consumer[A]): A => Unit =
    (a: A) => f.accept(a)

  def cnv[E, A](ea: Either[E, A]): EitherMonad.EitherMonadInstance[E, A] =
    new EitherMonad.EitherMonadInstance[E, A](ea)

  def kleisli[E, A, B](context: EitherContext[E]):
  JFunction[A, context.Either[B]] => A => Either[E, B] =
    f => a => f.apply(a).repr.self

  def pred[A](p: JFunction[A, java.lang.Boolean]): A => Boolean =
    (a: A) => p.apply(a)
}
