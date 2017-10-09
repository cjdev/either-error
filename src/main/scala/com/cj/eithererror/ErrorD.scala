package com.cj.eithererror

import scala.reflect.ClassTag

/**
  * An immutable, referentially-transparent alternative to `Throwable`
  */
case class ErrorD private(
  className: String,
  message: Option[String] = None,
  cause: Option[ErrorD] = None
) {
  import ErrorD._
  // TODO: See if we can make this tail recursive, or use a mutable builder
  def toThrowable: Throwable = (message, cause) match {
    case (None, None) =>
      toClass(className)
        .getDeclaredConstructor(classOf[Throwable])
        .newInstance()
    case (Some(m), None) =>
      toClass(className)
        .getDeclaredConstructor(classOf[Throwable])
        .newInstance(m)
    case (None, Some(c)) =>
      toClass(className)
        .getDeclaredConstructor(classOf[Throwable])
        .newInstance(c.toThrowable)
    case (Some(m), Some(c)) =>
      toClass(className)
        .getDeclaredConstructor(classOf[Throwable])
        .newInstance(m, c.toThrowable)
  }
}

object ErrorD {

  def apply[T <: Throwable]()(implicit tag: ClassTag[T]): ErrorD =
    new ErrorD(toName(tag.runtimeClass), None, None)

  def apply[T <: Throwable](message: String)
                           (implicit tag: ClassTag[T]): ErrorD =
    new ErrorD(toName(tag.runtimeClass), Some(message), None)

  def apply[T <: Throwable](cause: ErrorD)
                           (implicit tag: ClassTag[T]): ErrorD =
    new ErrorD(toName(tag.runtimeClass), None, Some(cause))

  def apply[T <: Throwable](message: String, cause: ErrorD)
                           (implicit tag: ClassTag[T]): ErrorD =
    new ErrorD(toName(tag.runtimeClass), Some(message), Some(cause))

  // TODO: see if we can make this tail recursive, or use a mutable builder
  def fromThrowable(err: Throwable): ErrorD = {
    new ErrorD(
      err.getClass.getName,
      Option(err.getMessage),
      Option(err.getCause).map(fromThrowable)
    )
  }

  private def base: Class[Throwable] = classOf[Throwable]

  private def toName(clazz: Class[_]): String =
    if (!clazz.newInstance.isInstanceOf[Throwable]) base.getName
    else clazz.getName

  private def toClass(className: String): Class[_ <: Throwable] =
    if (!Class.forName(className).isInstanceOf[Class[_ <: Throwable]]) base
    else Class.forName(className).asInstanceOf[Class[_ <: Throwable]]
}
