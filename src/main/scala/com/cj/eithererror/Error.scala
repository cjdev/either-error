// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.
package com.cj.eithererror

import scala.reflect.ClassTag

trait Error[E] extends ClassTag[E] {
  def getDefault: E = fromMessage("")
  def fromMessage(msg: String): E
  def fromThrowable(err: Throwable): E = fromMessage(err.toString)
  def toThrowable(e: E): Throwable = new Exception(e.toString)
  final override def runtimeClass: java.lang.Class[_] = getDefault.getClass
}

object Error {

  def getDefault[E: Error]: E =
    implicitly[Error[E]].getDefault

  def fromMessage[E: Error](msg: String): E =
    implicitly[Error[E]].fromMessage(msg)

  def fromThrowable[E: Error](err: Throwable): E =
    implicitly[Error[E]].fromThrowable(err)

  def toThrowable[E: Error](e: E): Throwable =
    implicitly[Error[E]].toThrowable(e)

  object Instances {

    implicit lazy val errorString: Error[String] = new Error[String] {
      def fromMessage(msg: String): String = msg
    }

    implicit lazy val errorThrowable: Error[Throwable] = new Error[Throwable] {
      def fromMessage(msg: String): Throwable = new Throwable(msg)
      override def getDefault: Throwable = new Throwable
      override def fromThrowable(err: Throwable): Throwable = err
      override def toThrowable(e: Throwable): Throwable = e
    }

    implicit lazy val errorException: Error[Exception] = new Error[Exception] {
      def fromMessage(msg: String): Exception = new Exception(msg)
      override def getDefault: Exception = new Exception
      override def toThrowable(e: Exception): Throwable = e

      @throws[Throwable]("Will rethrow any non-Exception Throwable.")
      override def fromThrowable(err: Throwable): Exception =
        err match { case e: Exception => e; case _ => throw err }
    }
  }
}
