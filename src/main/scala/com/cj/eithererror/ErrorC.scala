// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.language.experimental.macros
import scala.reflect.ClassTag

trait ErrorC[E] extends ClassTag[E] {

  def fromMessage(msg: String): E

  def getDefault: E = fromMessage("")

  def fromThrowable(err: Throwable): E =
    Option(err.getMessage).map(fromMessage).getOrElse(getDefault)

  def toThrowable(e: E): Throwable = new Throwable(e.toString)

  final override def runtimeClass: java.lang.Class[_] = getDefault.getClass
}

object ErrorC {

  def getDefault[E: ErrorC]: E = implicitly[ErrorC[E]].getDefault

  def fromMessage[E: ErrorC](msg: String): E =
    implicitly[ErrorC[E]].fromMessage(msg)

  def fromThrowable[E: ErrorC](err: Throwable): E =
    implicitly[ErrorC[E]].fromThrowable(err)

  def toThrowable[E: ErrorC](e: E): Throwable =
    implicitly[ErrorC[E]].toThrowable(e)

  case class Err(
                  clazz: String,
                  line: Long,
                  message: String,
                  cause: Option[Throwable]
                )

  object Instances {

    /**
      * Captures the message (if present) of thrown [[Throwable]]s.
      */
    implicit lazy val errorString: ErrorC[String] = new ErrorC[String] {
      def fromMessage(msg: String): String = msg
    }

    /**
      * Captures any non-fatal [[Throwable]].
      */
    implicit lazy val errorThrowable: ErrorC[Throwable] = new ErrorC[Throwable] {
      def fromMessage(msg: String): Throwable = new Throwable(msg)
      override def getDefault: Throwable = new Throwable
      override def fromThrowable(err: Throwable): Throwable = err
      override def toThrowable(e: Throwable): Throwable = e
    }

    /**
      * Captures [[Exception]]s. Re-throws non-`Exception` members of [[Throwable]].
      */
    implicit lazy val errorException: ErrorC[Exception] = new ErrorC[Exception] {
      def fromMessage(msg: String): Exception = new Exception(msg)
      override def getDefault: Exception = new Exception
      override def toThrowable(e: Exception): Throwable = e

      @throws[Throwable]("Will rethrow any non-Exception Throwable.")
      override def fromThrowable(err: Throwable): Exception =
        err match { case e: Exception => e; case _ => throw err }
    }

    /**
      * Captures the class name and message of thrown [[Throwable]]s.
      */
    implicit lazy val throwingClassAndMessage: ErrorC[String] = new ErrorC[String] {
      def fromMessage(msg: String): String = msg
      override def fromThrowable(err: Throwable): String = fromMessage(err.toString)
    }

    /**
      * Captures the message and cause (whenever present) of thrown [[Throwable]]s.
      */
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

//    /**
//      * Captures the line number and enclosing class name from source code,
//      * written in the format `[$CLASS:$LINE]: $msg`, similar to a logger.
//      */
//    implicit lazy val classAndLine: ErrorC[String] = new ErrorC[String] {
//      import Macros._
//      def fromMessage(msg: String): String = s"[$CLASS:$LINE]: $msg"
//    }
//
//    /**
//      * Captures the file name of the source code file and the line number
//      * wherein at the point the error was created, formatted as
//      * `[$FILE:$LINE]: $msg`, similar to a logger.
//      */
//    implicit lazy val fileAndLine: ErrorC[String] = new ErrorC[String] {
//      import Macros._
//      def fromMessage(msg: String): String = s"[$FILE:$LINE]: $msg"
//    }
//
//    /**
//      * Captures the line number, enclosing class, and the thrown [[Throwable]]
//      * that caused this error (if one exists), encoded as a [[ErrorC.Err]].
//      */
//    implicit lazy val structured: ErrorC[Err] = new ErrorC[Err] {
//      import Macros._
//
//      def fromMessage(msg: String): Err = Err(CLASS, LINE, msg, None)
//
//      override def fromThrowable(err: Throwable): Err = {
//        val msg = Option(err.getMessage).getOrElse("")
//        Err(CLASS, LINE, msg, Some(err))
//      }
//
//      override def toThrowable(e: Err): Throwable =
//        e.cause.fold (new Throwable(e.toString)) (new Throwable(e.toString, _))
//    }
  }
}
