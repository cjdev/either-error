// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {

  def LINE: Int = macro lineImpl

  def lineImpl(ctx: blackbox.Context): ctx.Expr[Int] = {
    import ctx.universe._
    val line = Literal(Constant(ctx.enclosingPosition.line))
    ctx.Expr[Int](line)
  }

  def CLASS: String = macro classImpl

  def classImpl(ctx: blackbox.Context): ctx.Expr[String] = {
    import ctx.universe._
    def nearestClass(s: Symbol): Symbol =
      if (s.isClass) s else nearestClass(s.owner)
    val classname = Literal(Constant(nearestClass(
      ctx.internal.enclosingOwner).toString.split(" ").last))
    ctx.Expr[String](classname)
  }

  def FILE: String = macro fileImpl

  def fileImpl(ctx: blackbox.Context): ctx.Expr[String] = {
    import ctx.universe._
    val basename = Literal(Constant(ctx.enclosingPosition.source.toString))
    ctx.Expr[String](basename)
  }

  def FL(msg: String): String = macro flImpl
  def CL(msg: String): String = macro clImpl
  def FCL(msg: String): String = macro fclImpl

  def flImpl(ctx: blackbox.Context)(msg: ctx.Expr[String]): ctx.Expr[String] = {
    import ctx.universe._
    val f = fileImpl(ctx)
    val l = lineImpl(ctx)
    reify(s"[${f.splice}:${l.splice}]: ${msg.splice}")
  }

  def clImpl(ctx: blackbox.Context)(msg: ctx.Expr[String]): ctx.Expr[String] = {
    import ctx.universe._
    val c = classImpl(ctx)
    val l = lineImpl(ctx)
    reify(s"[${c.splice}:${l.splice}]: ${msg.splice}")
  }

  def fclImpl(ctx: blackbox.Context)(msg: ctx.Expr[String]): ctx.Expr[String] = {
    import ctx.universe._
    val f = fileImpl(ctx)
    val c = classImpl(ctx)
    val l = lineImpl(ctx)
    reify(s"[${f.splice}:${c.splice}:${l.splice}]: ${msg.splice}")
  }
}
