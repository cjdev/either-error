// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror
package javaapi

import java.util.AbstractMap.SimpleEntry
import java.util.Optional
import java.util.function.{Consumer, Supplier, Function => JFunction}

private[javaapi] object Impl {

  import EitherMonad.EitherMonadInstance

  def esGetDefault[E](es: ErrorStrategy[E]): E =
    ErrorC.Defaults.getDefault(es.fromMessage)

  def esFromThrowable[E](es: ErrorStrategy[E], err: Throwable): E =
    ErrorC.Defaults.fromThrowable(es.fromMessage, err)

  def esToThrowable[E](e: E): Throwable = ErrorC.Defaults.toThrowable(e)

  def toErrorC[E](es: ErrorStrategy[E]): ErrorC[E] =
    new ErrorC[E] {
      def fromMessage(msg: String): E = es.fromMessage(msg)
      override def getDefault: E = es.getDefault
      override def fromThrowable(err: Throwable): E = es.fromThrowable(err)
      override def toThrowable(e: E): Throwable = es.toThrowable(e)
    }

  def toErrorStrategy[S, J](ec: ErrorC[S], f: S => J, g: J => S):
  ErrorStrategy[J] =
    new ErrorStrategy[J] {
      def getDefault: J = f(ec.getDefault)
      def fromThrowable(err: Throwable): J = f(ec.fromThrowable(err))
      def toThrowable(e: J): Throwable = ec.toThrowable(g(e))
      def fromMessage(msg: String): J = f(ec.fromMessage(msg))
    }

  def toErrorStrategyId[E](ec: ErrorC[E]): ErrorStrategy[E] =
    toErrorStrategy(ec, identity[E], identity[E])

  val string: ErrorStrategy[String] =
    toErrorStrategyId(ErrorC.Instances.errorString)

  val exception: ErrorStrategy[Exception] =
    toErrorStrategyId(ErrorC.Instances.errorException)

  val throwable: ErrorStrategy[Throwable] =
    toErrorStrategyId(ErrorC.Instances.errorThrowable)

  val classNameAndMessage: ErrorStrategy[String] =
    toErrorStrategyId(ErrorC.Instances.classNameAndMessage)

  val messageAndCause: ErrorStrategy[SimpleEntry[String, Optional[Throwable]]] =
    toErrorStrategy(ErrorC.Instances.messageAndCause,
      (x: (String, Option[Throwable])) => new SimpleEntry(x._1, toOptional(x._2)),
      (x: SimpleEntry[String, Optional[Throwable]]) => (x.getKey, toOption(x.getValue))
    )

  def toOptional[A](scalaOption: Option[A]): Optional[A] =
    scalaOption match {
      case None => Optional.empty()
      case Some(a) => Optional.ofNullable(a)
    }

  def toOption[A](javaOptional: Optional[A]): Option[A] =
    if (!javaOptional.isPresent) Option.empty
    else Option(javaOptional.get())

  def equals[E, A](ctx: EitherContext[E],
                   repr: Either[E, A], other: Object): Boolean =
    if (!other.isInstanceOf[ctx.Either[_]]) false
    else repr == other.asInstanceOf[ctx.Either[_]].repr

  def fold[E, A, X](repr: Either[E, A],
                    wl: JFunction[E, X], wr: JFunction[A, X]): X =
    repr.fold(e => wl.apply(e), a => wr.apply(a))

  def get[E, A](repr: Either[E, A]): Optional[A] = toOptional(repr.get)

  def getError[E, A](repr: Either[E, A]): Optional[E] = toOptional(repr.getError)

  def getOrElse1[E, A](repr: Either[E, A], a: A): A = repr.getOrElse(a)

  def getOrElse2[E, A](repr: Either[E, A], a: Supplier[A]): A =
    repr.getOrElse(a.get)

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
