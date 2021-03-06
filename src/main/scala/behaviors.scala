package edu.luc.cs.laufer.cs473.expressions

import ast._

object behaviors {

  def evaluate(e: Statement): Int = e match {
    case Constant(c) => c
    case UMinus(r)   => -evaluate(r)
    case Plus(l, r)  => evaluate(l) + evaluate(r)
    case Minus(l, r) => evaluate(l) - evaluate(r)
    case Times(l, r) => evaluate(l) * evaluate(r)
    case Div(l, r)   => evaluate(l) / evaluate(r)
    case Mod(l, r)   => evaluate(l) % evaluate(r)
  }

  def size(e: Statement): Int = e match {
    case Constant(c) => 1
    case UMinus(r)   => 1 + size(r)
    case Plus(l, r)  => 1 + size(l) + size(r)
    case Minus(l, r) => 1 + size(l) + size(r)
    case Times(l, r) => 1 + size(l) + size(r)
    case Div(l, r)   => 1 + size(l) + size(r)
    case Mod(l, r)   => 1 + size(l) + size(r)
  }

  def depth(e: Statement): Int = e match {
    case Constant(c) => 1
    case UMinus(r)   => 1 + depth(r)
    case Plus(l, r)  => 1 + math.max(depth(l), depth(r))
    case Minus(l, r) => 1 + math.max(depth(l), depth(r))
    case Times(l, r) => 1 + math.max(depth(l), depth(r))
    case Div(l, r)   => 1 + math.max(depth(l), depth(r))
    case Mod(l, r)   => 1 + math.max(depth(l), depth(r))
  }

  def toFormattedString(prefix: String)(e: Statement): String = e match {
    case Constant(c) => c.toString
    case UMinus(r)   => buildUnaryExprString(prefix, "-", toFormattedString(prefix)(r))
    case Plus(l, r)  => buildExprString(prefix, "+", toFormattedString(prefix)(l), toFormattedString(prefix)(r))
    case Minus(l, r) => buildExprString(prefix, "-", toFormattedString(prefix)(l), toFormattedString(prefix)(r))
    case Times(l, r) => buildExprString(prefix, "*", toFormattedString(prefix)(l), toFormattedString(prefix)(r))
    case Div(l, r)   => buildExprString(prefix, "/", toFormattedString(prefix)(l), toFormattedString(prefix)(r))
    case Mod(l, r)   => buildExprString(prefix, "%", toFormattedString(prefix)(l), toFormattedString(prefix)(r))

    case Variable(i) => i
    case Assignment(r, l @ _*)                 => l match {
      case head +: Nil => buildAssignmentString(prefix, "=", toFormattedString(prefix)(head), toFormattedString(prefix)(r))
      case head +: tail => buildAssignmentString(prefix, "=", toFormattedString(prefix)(Select(head, tail:_*)), toFormattedString(prefix)(r))
    }
      case Conditional(guard, ifBranch, elseBranch) => buildConditionalString(prefix, "if", guard, ifBranch, elseBranch:Option[Statement])
    case Loop(guard, body)                 => buildLoopString(prefix, "while", toFormattedString(prefix)(guard), toFormattedString(prefix)(body))
    case Block(expressions@_*)              => buildBlockString(prefix, expressions: _*)

    case Select(root, selectors@ _*)       => buildSelectString(prefix, "Select", root, selectors:_*)
    case Struct(s)                          => buildStructString(prefix, "Struct", s)
  }

  def toFormattedString(e: Statement): String = toFormattedString("")(e)

  def toFormattedString(e: List[Statement]): String = {
    val result = new StringBuilder("").append("{").append(EOL)
    e.foreach((ex: Statement) => {
      result.append(toFormattedString(INDENT)(ex))
    })
    result.append("}")
    result.toString
  }

  def buildExprString(prefix: String, nodeString: String, leftString: String, rightString: String) = {
    val result = new StringBuilder()
    result.append("(")
    result.append(leftString)
    result.append(" ")
    result.append(nodeString)
    result.append(" ")
    result.append(rightString)
    result.append(")")
    result.toString
  }

  def buildUnaryExprString(prefix: String, nodeString: String, exprString: String) = {
    val result = new StringBuilder()
    result.append(nodeString)
    result.append("(")
    result.append(exprString)
    result.append(")")
    result.toString
  }

  def buildAssignmentString(prefix: String, nodeString: String, leftString: String, rightString: String) = {
    val result = new StringBuilder(prefix)
    result.append(leftString)
    result.append(" ")
    result.append(nodeString)
    result.append(" ")
    result.append(rightString)
    result.append(";")
    result.append(EOL)
    result.toString
  }

  def buildConditionalString(prefix: String, nodeString: String, guard: Statement, ifBranch: Statement, elseBranch: Option[Statement]) = {
    val result = new StringBuilder(prefix).append(nodeString).append(" (")
    result.append(toFormattedString(prefix )(guard))
    result.append(")")

    result.append(toFormattedString(prefix)(ifBranch))
    elseBranch.foreach((block: Statement) => {

      result.append("else ")
      result.append(toFormattedString(prefix)(block))
    })
    result.toString
  }

  def buildBlockString(prefix: String, expressions: Statement*) = {
    val result = new StringBuilder(prefix).append("{").append(EOL)
    result.append(expressions.map(expr => prefix + INDENT + toFormattedString(prefix)(expr)).mkString(""))
    result.append(prefix+ "}").append(EOL)
    result.toString
  }

  def buildLoopString(prefix: String, nodeString: String, guard: String, body: String) = {
    val result = new StringBuilder(prefix).append(nodeString).append(" (")
    result.append(guard)
    result.append(") ")
    result.append(body)
    result.toString
  }

  def buildStructString(prefix: String, nodeString: String, fields: Map[String, Statement]) = {
    val result = new StringBuilder().append("{").append(EOL)
    val fieldsString = fields.map {
      case (i, e) => toFormattedString(prefix + INDENT)( Assignment(e, Variable(i)))}.mkString(",")
    result.append(fieldsString)
    result.append(prefix + INDENT + "}")
    result.toString
  }

  def buildSelectString(prefix: String, nodeString: String, root: Statement, selectors: Variable*) = {
    val result = new StringBuilder(prefix).append("(")
    result.append(toFormattedString("")(root))
    result.append(".")
    result.append(selectors.map((i: Variable) => toFormattedString(i)).mkString("."))
    result.append(")")
    result.toString
  }

  val EOL = scala.util.Properties.lineSeparator
  val INDENT = "  "
}
