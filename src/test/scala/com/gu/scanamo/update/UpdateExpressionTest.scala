package com.gu.scanamo.update

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import com.gu.scanamo.syntax._

class UpdateExpressionTest extends org.scalatest.FunSpec
  with org.scalatest.Matchers
  with org.scalatest.prop.Checkers {

  implicit lazy val arbSymbol: Arbitrary[Symbol] = Arbitrary(Gen.alphaNumStr.map(Symbol(_)))

  def leaf: Gen[UpdateExpression] =
    for {
      s <- arbitrary[Symbol]
      i <- arbitrary[Int]
      u <- Gen.oneOf(List(set(s -> i), add(s -> i), remove(s)))
    } yield u

  def genNode(level: Int): Gen[UpdateExpression] = for {
    left <- genTree(level)
    right <- genTree(level)
  } yield left and right

  def genTree(level: Int): Gen[UpdateExpression] =
    if (level >= 100) leaf else {
      Gen.oneOf(leaf, genNode(level + 1))
    }
  implicit lazy val update: Arbitrary[UpdateExpression] = Arbitrary(genTree(0))


  it("should have all attribute keys in the expression") {
    check {
      (ue: UpdateExpression) =>
        ue.attributeValues.keys.forall(s => {
          ue.expression.contains(s)
        })
    }
  }

}
