// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror
package javaapi

import java.util.AbstractMap.SimpleEntry
import java.util.Optional
import java.util.function.{Consumer, Supplier, Function => JFunction}

private[javaapi] object Impl {

  import EitherMonad.EitherMonadInstance

  val string: ErrorStrategy[String] = strat(ErrorC.Instances.errorString)
  val exception: ErrorStrategy[Exception] = strat(ErrorC.Instances.errorException)
  val throwable: ErrorStrategy[Throwable] = strat(ErrorC.Instances.errorThrowable)

  val classNameAndMessage: ErrorStrategy[String] =
    strat(ErrorC.Instances.errorClassNameAndMessage)

  val mc: ErrorC[(String, Option[Throwable])] =
    ErrorC.Instances.errorMessageAndCause

  def msAsJava[A, B](x: (A, Option[B])): SimpleEntry[A, Optional[B]] =
    new SimpleEntry[A, Optional[B]](x._1, op(x._2))

  def mcAsScala[A, B](x: SimpleEntry[A, Optional[B]]): (A, Option[B]) =
    (x.getKey, po(x.getValue))

  def mcFromMessage(msg: String): SimpleEntry[String, Optional[Throwable]] =
    msAsJava(mc.fromMessage(msg))

  def mcGetDefault: SimpleEntry[String, Optional[Throwable]] =
    msAsJava(mc.getDefault)

  def mcFromThrowable(err: Throwable): SimpleEntry[String, Optional[Throwable]] =
    msAsJava(mc.fromThrowable(err))

  def mcToThrowable(e: SimpleEntry[String, Optional[Throwable]]): Throwable =
    mc.toThrowable(mcAsScala(e))

  def inst[E](es: ErrorStrategy[E]): ErrorC[E] =
    new ErrorC[E] {
      def fromMessage(msg: String): E = es.fromMessage(msg)
      override def getDefault: E = es.getDefault
      override def fromThrowable(err: Throwable): E = es.fromThrowable(err)
      override def toThrowable(e: E): Throwable = es.toThrowable(e)
    }

  def strat[E](ec: ErrorC[E]): ErrorStrategy[E] =
    new ErrorStrategy[E] {
      def getDefault: E = ec.getDefault
      def fromThrowable(err: Throwable): E = ec.fromThrowable(err)
      def toThrowable(e: E): Throwable = ec.toThrowable(e)
      def fromMessage(msg: String): E = ec.fromMessage(msg)
    }

  def op[A](scalaOption: Option[A]): Optional[A] =
    scalaOption match {
      case None => Optional.empty()
      case Some(a) => Optional.of(a)
    }

  def po[A](javaOptional: Optional[A]): Option[A] =
    if (!javaOptional.isPresent) None
    else Some(javaOptional.get())

  def fold[E, A, X](repr: Either[E, A],
                    wl: JFunction[E, X], wr: JFunction[A, X]): X =
    repr.fold(e => wl.apply(e), a => wr.apply(a))

  def get[E, A](repr: Either[E, A]): Optional[A] = op(repr.get)

  def getError[E, A](repr: Either[E, A]): Optional[E] = op(repr.getError)

  def getOrElse[E, A](repr: Either[E, A], a: A): A = repr.getOrElse(a)

  def getOrThrow[E, A](ev: ErrorC[E], repr: Either[E, A]): A =
    repr.getOrThrow(ev)

  def foreach[E, A](repr: Either[E, A], f: Consumer[A]): Unit =
    repr.foreach(a => f.accept(a))

  def map[E, A, B](repr: Either[E, A], f: JFunction[A, B]): Either[E, B] =
    repr.map(a => f.apply(a))

  def flatMap[E, A, B](ctx: EitherContext[E], repr: Either[E, A],
                       f: JFunction[A, _]): Either[E, B] =
    repr.flatMap(a => f(a).asInstanceOf[ctx.Either[B]].repr) // Lord, have mercy.

  def filter[E, A](ev: ErrorC[E], repr: Either[E, A],
                   p: JFunction[A, java.lang.Boolean]): Either[E, A] =
    repr.filter(a => p.apply(a))(ev)

  def and1[E, A, B](ctx: EitherContext[E],
                    repr: Either[E, A], other: Object): Either[E, B] =
    repr.and(other.asInstanceOf[ctx.Either[B]].repr)

  def and2[E, A, B](ctx: EitherContext[E],
                    repr: Either[E, A], other: Supplier[_]): Either[E, B] =
    repr.and(other.get().asInstanceOf[ctx.Either[B]].repr)

  def or1[E, A](ctx: EitherContext[E],
                repr: Either[E, A], other: Object): Either[E, A] =
    repr.or(other.asInstanceOf[ctx.Either[A]].repr)

  def or2[E, A](ctx: EitherContext[E],
                repr: Either[E, A], other: Supplier[_]): Either[E, A] =
    repr.or(other.get().asInstanceOf[ctx.Either[A]].repr)

  def recover[E, A](ctx: EitherContext[E], repr: Either[E, A],
                    f: JFunction[E, _]): Either[E, A] =
    repr.recover(e => f.apply(e).asInstanceOf[ctx.Either[A]].repr)

  def unsafe[E, A](a: A): Either[E, A] = Right(a)

  def safely1[E, A](ev: ErrorC[E], a: Supplier[A], alt: Object): Either[E, A] =
    EitherMonad.safely(a.get(), alt)(ev)

  def safely2[E, A](ev: ErrorC[E],
                    a: Supplier[A], alt: Supplier[_]): Either[E, A] =
    EitherMonad.safely(a.get(), alt.get())(ev)

  def safely3[E, A](ev: ErrorC[E], a: Supplier[A]): Either[E, A] =
    EitherMonad.safely(a.get)(ev)

  def ensure1[E](ev: ErrorC[E],
                 p: java.lang.Boolean, alt: Object): Either[E, Unity] =
    EitherMonad.ensure(p, alt)(ev).map(_ => Unity.instance)

  def ensure2[E](ev: ErrorC[E],
                 p: java.lang.Boolean, alt: Supplier[_]): Either[E, Unity] =
    EitherMonad.ensure(p, alt.get())(ev).map(_ => Unity.instance)

  def ensure3[E](ev: ErrorC[E], p: java.lang.Boolean): Either[E, Unity] =
    EitherMonad.ensure(p)(ev).map(_ => Unity.instance)

  def failure1[E, A](ev: ErrorC[E], alt: Object): Either[E, A] =
    EitherMonad.failure(alt)(ev)

  def failure2[E, A](ev: ErrorC[E]): Either[E, A] = EitherMonad.failure(ev)
}
