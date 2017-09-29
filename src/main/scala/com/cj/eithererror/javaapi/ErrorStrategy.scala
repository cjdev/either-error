package com.cj.eithererror
package javaapi

abstract class ErrorStrategy[E] {
  def fromMessage(msg: String): E
  def getDefault(): E = fromMessage("")
  def fromThrowable(err: Throwable): E = fromMessage(err.toString)
  def toThrowable(e: E): Throwable = new Exception(e.toString)
}

object ErrorStrategy {

  val string: ErrorStrategy[String] =
    fromScala(ErrorC.Instances.errorString)

  val exception: ErrorStrategy[Exception] =
    fromScala(ErrorC.Instances.errorException)

  val throwable: ErrorStrategy[Throwable] =
    fromScala(ErrorC.Instances.errorThrowable)

  private[javaapi] def fromScala[E](ec: ErrorC[E]): ErrorStrategy[E] =
    new ErrorStrategy[E] {
      def fromMessage(msg: String): E = ec.fromMessage(msg)
      override def getDefault(): E = ec.getDefault
      override def fromThrowable(err: Throwable): E = ec.fromThrowable(err)
      override def toThrowable(e: E): Throwable = ec.toThrowable(e)
    }

  private[javaapi] def toScala[E](es: ErrorStrategy[E]): ErrorC[E] =
    new ErrorC[E] {
      def fromMessage(msg: String): E = es.fromMessage(msg)
      override def getDefault: E = es.getDefault()
      override def fromThrowable(err: Throwable): E = es.fromThrowable(err)
      override def toThrowable(e: E): Throwable = es.toThrowable(e)
    }
}
