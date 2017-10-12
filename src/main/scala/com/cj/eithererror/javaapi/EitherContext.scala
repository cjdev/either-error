// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror
package javaapi

import java.lang.{ Boolean => JBoolean }
import java.util.Optional
import java.util.function.{Consumer, Predicate, Supplier, Function => JFunction}

class EitherContext[E](val strategy: ErrorStrategy[E]) {

  import EitherMonad.EitherMonadInstance

  private implicit val ev: ErrorC[E] = new ErrorC[E] {
    def fromMessage(msg: String): E = strategy.fromMessage(msg)
    override def fromThrowable(err: Throwable): E = strategy.fromThrowable(err)
    override def toThrowable(e: E): Throwable = strategy.toThrowable(e)
    override def getDefault: E = strategy.getDefault
  }

  final case class Either[A] private(private val repr: scala.util.Either[E, A]) {

    def fold[X](withError: JFunction[E, X], withValue: JFunction[A, X]): X =
      repr.fold(withError.apply, withValue.apply)

    def get: Optional[A] =
      repr.get.fold(Optional.empty[A])(Optional.of)

    def getError: Optional[E] =
      repr.getError.fold(Optional.empty[E])(Optional.of)

    def getOrElse(a: A): A =
      repr.getOrElse(a)

    def getOrElse(a: Supplier[A]): A =
      repr.getOrElse(a.get)

    def getOrThrow: A =
      repr.getOrThrow

    def foreach(f: Consumer[A]): Unit =
      repr.foreach(f.accept)

    def map[B](f: JFunction[A, B]): Either[B] =
      Either(repr.map(f.apply))

    def flatMap[B](k: JFunction[A, Either[B]]): Either[B] =
      Either(repr.flatMap(k.apply(_).repr))

    def filter(p: Predicate[A]): Either[A] =
      Either(repr.filter(p.test))

    def and[B](other: Either[B]): Either[B] =
      Either(repr.and(other.repr))

    def and[B](other: Supplier[Either[B]]): Either[B] =
      Either(repr.and(other.get.repr))

    def or(other: Either[A]): Either[A] =
      Either(repr.or(other.repr))

    def or(other: Supplier[Either[A]]): Either[A] =
      Either(repr.or(other.get.repr))

    def recover(f: JFunction[E, Either[A]]): Either[A] =
      Either(repr.recover(f.apply(_).repr))
  }

  def unsafe[A](a: A): Either[A] =
    Either(EitherMonad.safely(a))

  def safely[A](a: Supplier[A]): Either[A] =
    Either(EitherMonad.safely(a.get))

  def safely[A](a: Supplier[A], alt: Object): Either[A] =
    Either(EitherMonad.safely(a.get, alt))

  def safely[A](a: Supplier[A], alt: Supplier[Object]): Either[A] =
    Either(EitherMonad.safely(a.get, alt.get))

  def ensure[A](p: JBoolean): Either[Unity] =
    Either(EitherMonad.ensure(p).map(_ => Unity.instance))

  def ensure[A](p: JBoolean, alt: Object): Either[Unity] =
    Either(EitherMonad.ensure(p, alt).map(_ => Unity.instance))

  def ensure[A](p: JBoolean, alt: Supplier[Object]): Either[Unity] =
    Either(EitherMonad.ensure(p, alt.get).map(_ => Unity.instance))

  def failure[A]: Either[A] =
    Either(EitherMonad.failure)

  def failure[A](alt: Object): Either[A] =
    Either(EitherMonad.failure(alt))
}
