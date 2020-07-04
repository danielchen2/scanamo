/*
 * Copyright 2019 Scanamo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org

import org.scanamo.query._
import org.scanamo.update._
import scala.language.implicitConversions

package object scanamo {
  object syntax {
    implicit class AttributeNameKeyCondition(s: String) {
      def and(other: String) = HashAndRangeKeyNames(AttributeName.of(s), AttributeName.of(other))
    }

    case class HashAndRangeKeyNames(hash: AttributeName, range: AttributeName)

    implicit def stringTupleToUniqueKey[V: DynamoFormat](pair: (String, V)) =
      UniqueKey(KeyEquals(AttributeName.of(pair._1), pair._2))

    implicit def stringTupleToKeyCondition[V: DynamoFormat](pair: (String, V)) =
      KeyEquals(AttributeName.of(pair._1), pair._2)

    implicit def toUniqueKey[T: UniqueKeyCondition](t: T) = UniqueKey(t)

    implicit def stringListTupleToUniqueKeys[V: DynamoFormat](pair: (String, Set[V])) =
      UniqueKeys(KeyList(AttributeName.of(pair._1), pair._2))

    implicit def toMultipleKeyList[H: DynamoFormat, R: DynamoFormat](pair: (HashAndRangeKeyNames, Set[(H, R)])) =
      UniqueKeys(MultipleKeyList(pair._1.hash -> pair._1.range, pair._2))

    implicit def stringTupleToQuery[V: DynamoFormat](pair: (String, V)) =
      Query(KeyEquals(AttributeName.of(pair._1), pair._2))

    implicit def toQuery[T: QueryableKeyCondition](t: T) = Query(t)

    case class Bounds[V: DynamoFormat](lowerBound: V, upperBound: V)

    implicit final class Bound[V](private val v: V) extends AnyVal {
      def and(upperBound: V)(implicit V: DynamoFormat[V]): Bounds[V] = Bounds(v, upperBound)
    }

    def attributeExists(string: String) = AttributeExists(AttributeName.of(string))

    def attributeNotExists(string: String) = AttributeNotExists(AttributeName.of(string))

    implicit final class ConditionExpressionOps[X](private val x: X) extends AnyVal {
      def unary_!(implicit X: ConditionExpression[X]): Not[X] = Not(x)

      def &&[Y](y: Y)(implicit X: ConditionExpression[X], Y: ConditionExpression[Y]): AndCondition[X, Y] =
        AndCondition(x, y)
      def ||[Y](y: Y)(implicit X: ConditionExpression[X], Y: ConditionExpression[Y]): OrCondition[X, Y] =
        OrCondition(x, y)
    }

    def set(to: String, from: String): UpdateExpression = UpdateExpression.setFromAttribute(from, to)
    def set[V: DynamoFormat](fieldValue: (AttributeName, V)): UpdateExpression = UpdateExpression.set(fieldValue)
    def append[V: DynamoFormat](fieldValue: (AttributeName, V)): UpdateExpression = UpdateExpression.append(fieldValue)
    def prepend[V: DynamoFormat](fieldValue: (AttributeName, V)): UpdateExpression =
      UpdateExpression.prepend(fieldValue)
    def appendAll[V: DynamoFormat](fieldValue: (AttributeName, List[V])): UpdateExpression =
      UpdateExpression.appendAll(fieldValue)
    def prependAll[V: DynamoFormat](fieldValue: (AttributeName, List[V])): UpdateExpression =
      UpdateExpression.prependAll(fieldValue)
    def add[V: DynamoFormat](fieldValue: (AttributeName, V)): UpdateExpression = UpdateExpression.add(fieldValue)
    def delete[V: DynamoFormat](fieldValue: (AttributeName, V)): UpdateExpression = UpdateExpression.delete(fieldValue)
    def remove(field: AttributeName): UpdateExpression = UpdateExpression.remove(field)

    implicit def stringAttributeName(s: String): AttributeName = AttributeName.of(s)
    implicit def stringAttributeNameValue[T](sv: (String, T)): (AttributeName, T) = AttributeName.of(sv._1) -> sv._2
  }
}
