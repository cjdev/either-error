// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {

  def LINE: Int = macro lineImpl

  def lineImpl(c: blackbox.Context): c.Expr[Int] = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    c.Expr[Int](line)
  }

  def CLASS: String = macro classImpl

  def classImpl(c: blackbox.Context): c.Expr[String] = {
    import c.universe._
    def nearestClass(s: Symbol): Symbol = if (s.isClass) s else nearestClass(s.owner)
    val cl = Literal(Constant(nearestClass(c.internal.enclosingOwner).toString.split(" ").last))
    c.Expr[String](cl)
  }

  def FILE: String = macro fileImpl

  def fileImpl(c: blackbox.Context): c.Expr[String] = {
    import c.universe._
    val basename = Literal(Constant(c.enclosingPosition.source.toString))
    c.Expr[String](basename)
  }
}
