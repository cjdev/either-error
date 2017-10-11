// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.annotation.StaticAnnotation
import scala.reflect.ClassTag

/**
  * Instances should satisfy the following law:
  *
  *     forAll { (e: E) => assert { fromThrowable(toThrowable(e)) == e } }
  *
  * Instances that do not satisfy the above law should be annotated with
  * [[ErrorC.IllegalInstance[E]].
  *
  * @tparam E An arbitrary type that is to be used to describe errors.
  */
trait ErrorC[E] extends ClassTag[E] {

  def fromMessage(msg: String): E

  def getDefault: E = ErrorC.Defaults.getDefault(fromMessage)

  def fromThrowable(err: Throwable): E =
    ErrorC.Defaults.fromThrowable(fromMessage, err)

  def toThrowable(e: E): Throwable = ErrorC.Defaults.toThrowable(e)

  final override def runtimeClass: java.lang.Class[_] = getDefault.getClass
}

object ErrorC {

  case class IllegalInstance[A](note: String = "") extends StaticAnnotation

  def getDefault[E: ErrorC]: E = implicitly[ErrorC[E]].getDefault

  def fromMessage[E: ErrorC](msg: String): E =
    implicitly[ErrorC[E]].fromMessage(msg)

  def fromThrowable[E: ErrorC](err: Throwable): E =
    implicitly[ErrorC[E]].fromThrowable(err)

  def toThrowable[E: ErrorC](e: E): Throwable =
    implicitly[ErrorC[E]].toThrowable(e)

  object Instances {

    implicit lazy val errorString: ErrorC[String] = new ErrorC[String] {
      def fromMessage(msg: String): String = msg
    }

    implicit lazy val errorThrowable: ErrorC[Throwable] = new ErrorC[Throwable] {
      def fromMessage(msg: String): Throwable = new Throwable(msg)
      override def getDefault: Throwable = new Throwable
      override def fromThrowable(err: Throwable): Throwable = err
      override def toThrowable(e: Throwable): Throwable = e
    }

    implicit lazy val errorException: ErrorC[Exception] = new ErrorC[Exception] {
      def fromMessage(msg: String): Exception = new Exception(msg)
      override def getDefault: Exception = new Exception
      override def toThrowable(e: Exception): Throwable = e

      @throws[Throwable]("Will rethrow any non-Exception Throwable.")
      override def fromThrowable(err: Throwable): Exception =
        err match { case e: Exception => e; case _ => throw err }
    }

    @ErrorC.IllegalInstance[String]("Breaks round trips through Throwable.")
    implicit lazy val classNameAndMessage: ErrorC[String] = new ErrorC[String] {
      def fromMessage(msg: String): String = msg
      override def fromThrowable(err: Throwable): String = fromMessage(err.toString)
    }

    implicit lazy val messageAndCause: ErrorC[(String, Option[Throwable])] =
      new ErrorC[(String, Option[Throwable])] {

        def fromMessage(msg: String): (String, Option[Throwable]) = (msg, None)

        override def fromThrowable(err: Throwable): (String, Option[Throwable]) =
          (Option(err.getMessage).getOrElse(""), Option(err.getCause))

        override def toThrowable(e: (String, Option[Throwable])): Throwable =
          e._2 match {
            case Some(err) => new Throwable(e._1, err)
            case None => new Throwable(e._1)
          }
      }
  }

  private[eithererror] object Defaults {

    def getDefault[E](fromMessageImpl: String => E): E =
      fromMessageImpl("")

    def fromThrowable[E](fromMessageImpl: String => E, err: Throwable): E =
      fromMessageImpl(Option(err.getMessage).getOrElse(""))

    def toThrowable[E](e: E): Throwable = new Throwable(e.toString)

    def asScala[A, B](f: java.util.function.Function[A, B]): A => B =
      (a: A) => f.apply(a)
  }
}
