/*******************************************************************************
 * Copyright 2010 Maxime Lévesque
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package org.squeryl

import java.sql

import dsl.ast._
import dsl._
import java.util.{ Date, UUID }
import java.sql.{ ResultSet, Timestamp }

import org.squeryl.internals.{ ArrayTEF, FieldMapper }

@deprecated(
  "the PrimitiveTypeMode companion object is deprecated, you should define a mix in the trait for your application. See : http://squeryl.org/0.9.6.html",
  "0.9.6"
)
object PrimitiveTypeMode extends PrimitiveTypeMode

private[squeryl] object InternalFieldMapper extends PrimitiveTypeMode

trait PrimitiveTypeMode extends QueryDsl with FieldMapper {

  // =========================== Non Numerical ===========================
  implicit val stringTEF: TypedExpressionFactory[String, TString] with PrimitiveJdbcMapper[String] {
    val defaultColumnLength: Int

    val sample: String

    def extractNativeJdbcValue(rs: ResultSet, i: Int): String
  } = PrimitiveTypeSupport.stringTEF
  implicit val optionStringTEF: TypedExpressionFactory[Option[String], TOptionString] with DeOptionizer[
    String,
    String,
    TString,
    Option[String],
    TOptionString
  ] {
    val deOptionizer: TypedExpressionFactory[String, TString] with PrimitiveJdbcMapper[String] {
      val sample: String

      val defaultColumnLength: Int

      def extractNativeJdbcValue(rs: ResultSet, i: Int): String
    }
  } = PrimitiveTypeSupport.optionStringTEF
  implicit val dateTEF: TypedExpressionFactory[Date, TDate] with PrimitiveJdbcMapper[Date] {
    val defaultColumnLength: Int

    val sample: Date

    def extractNativeJdbcValue(rs: ResultSet, i: Int): sql.Date
  } = PrimitiveTypeSupport.dateTEF
  implicit val optionDateTEF: TypedExpressionFactory[Option[Date], TOptionDate] with DeOptionizer[
    Date,
    Date,
    TDate,
    Option[Date],
    TOptionDate
  ] {
    val deOptionizer: TypedExpressionFactory[Date, TDate] with PrimitiveJdbcMapper[Date] {
      val sample: Date

      val defaultColumnLength: Int

      def extractNativeJdbcValue(rs: ResultSet, i: Int): sql.Date
    }
  } = PrimitiveTypeSupport.optionDateTEF
  implicit val sqlDateTEF: TypedExpressionFactory[sql.Date, TDate] with PrimitiveJdbcMapper[sql.Date] {
    val defaultColumnLength: Int

    val sample: sql.Date

    def extractNativeJdbcValue(rs: ResultSet, i: Int): sql.Date
  } = PrimitiveTypeSupport.sqlDateTEF
  implicit val optionSqlDateTEF
    : TypedExpressionFactory[Option[sql.Date], TOptionDate] with DeOptionizer[sql.Date, sql.Date, TDate, Option[
      sql.Date
    ], TOptionDate] {
      val deOptionizer: TypedExpressionFactory[sql.Date, TDate] with PrimitiveJdbcMapper[sql.Date] {
        val sample: sql.Date

        val defaultColumnLength: Int

        def extractNativeJdbcValue(rs: ResultSet, i: Int): sql.Date
      }
    } = PrimitiveTypeSupport.optionSqlDateTEF
  implicit val timestampTEF: TypedExpressionFactory[Timestamp, TTimestamp] with PrimitiveJdbcMapper[Timestamp] {
    val defaultColumnLength: Int

    val sample: Timestamp

    def extractNativeJdbcValue(rs: ResultSet, i: Int): Timestamp
  } = PrimitiveTypeSupport.timestampTEF
  implicit val optionTimestampTEF: TypedExpressionFactory[Option[Timestamp], TOptionTimestamp] with DeOptionizer[
    Timestamp,
    Timestamp,
    TTimestamp,
    Option[Timestamp],
    TOptionTimestamp
  ] {
    val deOptionizer: TypedExpressionFactory[Timestamp, TTimestamp] with PrimitiveJdbcMapper[Timestamp] {
      val sample: Timestamp

      val defaultColumnLength: Int

      def extractNativeJdbcValue(rs: ResultSet, i: Int): Timestamp
    }
  } = PrimitiveTypeSupport.optionTimestampTEF
  implicit val doubleArrayTEF: ArrayTEF[Double, TDoubleArray] {
    def fromWrappedJDBCType(elements: Array[Object]): Array[Double]

    val sample: Array[Double]

    def toWrappedJDBCType(element: Double): Object
  } = PrimitiveTypeSupport.doubleArrayTEF
  implicit val intArrayTEF: ArrayTEF[Int, TIntArray] {
    def fromWrappedJDBCType(elements: Array[Object]): Array[Int]

    val sample: Array[Int]

    def toWrappedJDBCType(element: Int): Object
  } = PrimitiveTypeSupport.intArrayTEF
  implicit val longArrayTEF: ArrayTEF[Long, TLongArray] {
    def fromWrappedJDBCType(elements: Array[Object]): Array[Long]

    val sample: Array[Long]

    def toWrappedJDBCType(element: Long): Object
  } = PrimitiveTypeSupport.longArrayTEF
  implicit val stringArrayTEF: ArrayTEF[String, TStringArray] {
    def fromWrappedJDBCType(elements: Array[Object]): Array[String]

    val sample: Array[String]

    def toWrappedJDBCType(element: String): Object
  } = PrimitiveTypeSupport.stringArrayTEF

  // =========================== Numerical Integral ===========================
  implicit val byteTEF: IntegralTypedExpressionFactory[Byte, TByte, Float, TFloat] with PrimitiveJdbcMapper[Byte] {
    val defaultColumnLength: Int

    val sample: Byte

    val floatifyer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] {
      val sample: Float

      val defaultColumnLength: Int

      def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
    }

    def extractNativeJdbcValue(rs: ResultSet, i: Int): Byte
  } = PrimitiveTypeSupport.byteTEF
  implicit val optionByteTEF
    : IntegralTypedExpressionFactory[Option[Byte], TOptionByte, Option[Float], TOptionFloat] with DeOptionizer[
      Byte,
      Byte,
      TByte,
      Option[Byte],
      TOptionByte
    ] {
      val floatifyer: FloatTypedExpressionFactory[Option[Float], TOptionFloat] with DeOptionizer[
        Float,
        Float,
        TFloat,
        Option[Float],
        TOptionFloat
      ] {
        val deOptionizer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] with Object {
          val sample: Float

          val defaultColumnLength: Int

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
        }
      }

      val deOptionizer: IntegralTypedExpressionFactory[Byte, TByte, Float, TFloat] with PrimitiveJdbcMapper[Byte] {
        val sample: Byte

        val defaultColumnLength: Int

        val floatifyer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] {
          val defaultColumnLength: Int

          val sample: Float

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
        }

        def extractNativeJdbcValue(rs: ResultSet, i: Int): Byte
      }
    } = PrimitiveTypeSupport.optionByteTEF
  implicit val intTEF: IntegralTypedExpressionFactory[Int, TInt, Float, TFloat] with PrimitiveJdbcMapper[Int] {
    val defaultColumnLength: Int

    val sample: Int

    val floatifyer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] {
      val sample: Float

      val defaultColumnLength: Int

      def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
    }

    def extractNativeJdbcValue(rs: ResultSet, i: Int): Int
  } = PrimitiveTypeSupport.intTEF
  implicit val optionIntTEF
    : IntegralTypedExpressionFactory[Option[Int], TOptionInt, Option[Float], TOptionFloat] with DeOptionizer[
      Int,
      Int,
      TInt,
      Option[Int],
      TOptionInt
    ] {
      val floatifyer: FloatTypedExpressionFactory[Option[Float], TOptionFloat] with DeOptionizer[
        Float,
        Float,
        TFloat,
        Option[Float],
        TOptionFloat
      ] {
        val deOptionizer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] with Object {
          val sample: Float

          val defaultColumnLength: Int

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
        }
      }

      val deOptionizer: IntegralTypedExpressionFactory[Int, TInt, Float, TFloat] with PrimitiveJdbcMapper[Int] {
        val sample: Int

        val defaultColumnLength: Int

        val floatifyer: FloatTypedExpressionFactory[Float, TFloat] with PrimitiveJdbcMapper[Float] {
          val defaultColumnLength: Int

          val sample: Float

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Float
        }

        def extractNativeJdbcValue(rs: ResultSet, i: Int): Int
      }
    } = PrimitiveTypeSupport.optionIntTEF
  implicit val longTEF: IntegralTypedExpressionFactory[Long, TLong, Double, TDouble] with PrimitiveJdbcMapper[Long] {
    val defaultColumnLength: Int

    val sample: Long

    val floatifyer: FloatTypedExpressionFactory[Double, TDouble] with PrimitiveJdbcMapper[Double] with Object {
      val defaultColumnLength: Int

      val sample: Double

      def extractNativeJdbcValue(rs: ResultSet, i: Int): Double
    }

    def extractNativeJdbcValue(rs: ResultSet, i: Int): Long
  } = PrimitiveTypeSupport.longTEF
  implicit val optionLongTEF
    : IntegralTypedExpressionFactory[Option[Long], TOptionLong, Option[Double], TOptionDouble] with DeOptionizer[
      Long,
      Long,
      TLong,
      Option[Long],
      TOptionLong
    ] {
      val floatifyer: FloatTypedExpressionFactory[Option[Double], TOptionDouble] with DeOptionizer[
        Double,
        Double,
        TDouble,
        Option[Double],
        TOptionDouble
      ] with Object {
        val deOptionizer: FloatTypedExpressionFactory[Double, TDouble] with PrimitiveJdbcMapper[Double] with Object {
          val defaultColumnLength: Int

          val sample: Double

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Double
        }
      }

      val deOptionizer: IntegralTypedExpressionFactory[Long, TLong, Double, TDouble] with PrimitiveJdbcMapper[Long] with Object {
        val defaultColumnLength: Int

        val sample: Long

        val floatifyer: FloatTypedExpressionFactory[Double, TDouble] with PrimitiveJdbcMapper[Double] with Object {
          val defaultColumnLength: Int

          val sample: Double

          def extractNativeJdbcValue(rs: ResultSet, i: Int): Double
        }

        def extractNativeJdbcValue(rs: ResultSet, i: Int): Long
      }
    } = PrimitiveTypeSupport.optionLongTEF

  // =========================== Numerical Floating Point ===========================
  implicit val floatTEF            = PrimitiveTypeSupport.floatTEF
  implicit val optionFloatTEF      = PrimitiveTypeSupport.optionFloatTEF
  implicit val doubleTEF           = PrimitiveTypeSupport.doubleTEF
  implicit val optionDoubleTEF     = PrimitiveTypeSupport.optionDoubleTEF
  implicit val bigDecimalTEF       = PrimitiveTypeSupport.bigDecimalTEF
  implicit val optionBigDecimalTEF = PrimitiveTypeSupport.optionBigDecimalTEF

  implicit def stringToTE(s: String)               = stringTEF.create(s)
  implicit def optionStringToTE(s: Option[String]) = optionStringTEF.create(s)

  implicit def dateToTE(s: Date)               = dateTEF.create(s)
  implicit def optionDateToTE(s: Option[Date]) = optionDateTEF.create(s)

  implicit def timestampToTE(s: Timestamp) = timestampTEF.create(s)
  implicit def optionTimestampToTE(s: Option[Timestamp]) =
    optionTimestampTEF.create(s)

  implicit def booleanToTE(s: Boolean) =
    PrimitiveTypeSupport.booleanTEF.create(s)
  implicit def optionBooleanToTE(s: Option[Boolean]) =
    PrimitiveTypeSupport.optionBooleanTEF.create(s)

  implicit def uuidToTE(s: UUID) = PrimitiveTypeSupport.uuidTEF.create(s)
  implicit def optionUUIDToTE(s: Option[UUID]) =
    PrimitiveTypeSupport.optionUUIDTEF.create(s)

  implicit def binaryToTE(s: Array[Byte]) =
    PrimitiveTypeSupport.binaryTEF.create(s)
  implicit def optionByteArrayToTE(s: Option[Array[Byte]]) =
    PrimitiveTypeSupport.optionByteArrayTEF.create(s)

  implicit def enumValueToTE[A >: Enumeration#Value <: Enumeration#Value](e: A): TypedExpression[A, TEnumValue[A]] =
    PrimitiveTypeSupport.enumValueTEF[A](e).create(e)

  implicit def optionEnumcValueToTE[A >: Enumeration#Value <: Enumeration#Value](
    e: Option[A]
  ): TypedExpression[Option[A], TOptionEnumValue[A]] =
    PrimitiveTypeSupport.optionEnumValueTEF[A](e).create(e)

  implicit def byteToTE(f: Byte)               = byteTEF.create(f)
  implicit def optionByteToTE(f: Option[Byte]) = optionByteTEF.create(f)

  implicit def intToTE(f: Int)               = intTEF.create(f)
  implicit def optionIntToTE(f: Option[Int]) = optionIntTEF.create(f)

  implicit def longToTE(f: Long)               = longTEF.create(f)
  implicit def optionLongToTE(f: Option[Long]) = optionLongTEF.create(f)

  implicit def floatToTE(f: Float)               = floatTEF.create(f)
  implicit def optionFloatToTE(f: Option[Float]) = optionFloatTEF.create(f)

  implicit def doubleToTE(f: Double)               = doubleTEF.create(f)
  implicit def optionDoubleToTE(f: Option[Double]) = optionDoubleTEF.create(f)

  implicit def bigDecimalToTE(f: BigDecimal) = bigDecimalTEF.create(f)
  implicit def optionBigDecimalToTE(f: Option[BigDecimal]) =
    optionBigDecimalTEF.create(f)

  implicit def doubleArrayToTE(f: Array[Double]) = doubleArrayTEF.create(f)
  implicit def intArrayToTE(f: Array[Int])       = intArrayTEF.create(f)
  implicit def longArrayToTE(f: Array[Long])     = longArrayTEF.create(f)
  implicit def stringArrayToTE(f: Array[String]) = stringArrayTEF.create(f)

  implicit def logicalBooleanToTE(l: LogicalBoolean) =
    PrimitiveTypeSupport.booleanTEF.convert(l)

  implicit def queryStringToTE(q: Query[String]) =
    new QueryValueExpressionNode[String, TString](q.copy(false, Nil).ast, stringTEF.createOutMapper)
  implicit def queryOptionStringToTE(q: Query[Option[String]]) =
    new QueryValueExpressionNode[Option[String], TOptionString](q.copy(false, Nil).ast, optionStringTEF.createOutMapper)
  implicit def queryStringGroupedToTE(q: Query[Group[String]]) =
    new QueryValueExpressionNode[String, TString](q.copy(false, Nil).ast, stringTEF.createOutMapper)
  implicit def queryOptionStringGroupedToTE(q: Query[Group[Option[String]]]) =
    new QueryValueExpressionNode[Option[String], TOptionString](q.copy(false, Nil).ast, optionStringTEF.createOutMapper)
  implicit def queryStringMeasuredToTE(q: Query[Measures[String]]) =
    new QueryValueExpressionNode[String, TString](q.copy(false, Nil).ast, stringTEF.createOutMapper)
  implicit def queryOptionStringMeasuredToTE(q: Query[Measures[Option[String]]]) =
    new QueryValueExpressionNode[Option[String], TOptionString](q.copy(false, Nil).ast, optionStringTEF.createOutMapper)

  implicit def queryDateToTE(q: Query[Date]) =
    new QueryValueExpressionNode[Date, TDate](q.copy(false, Nil).ast, dateTEF.createOutMapper)
  implicit def queryOptionDateToTE(q: Query[Option[Date]]) =
    new QueryValueExpressionNode[Option[Date], TOptionDate](q.copy(false, Nil).ast, optionDateTEF.createOutMapper)
  implicit def queryDateGroupedToTE(q: Query[Group[Date]]) =
    new QueryValueExpressionNode[Date, TDate](q.copy(false, Nil).ast, dateTEF.createOutMapper)
  implicit def queryOptionDateGroupedToTE(q: Query[Group[Option[Date]]]) =
    new QueryValueExpressionNode[Option[Date], TOptionDate](q.copy(false, Nil).ast, optionDateTEF.createOutMapper)
  implicit def queryDateMeasuredToTE(q: Query[Measures[Date]]) =
    new QueryValueExpressionNode[Date, TDate](q.copy(false, Nil).ast, dateTEF.createOutMapper)
  implicit def queryOptionDateMeasuredToTE(q: Query[Measures[Option[Date]]]) =
    new QueryValueExpressionNode[Option[Date], TOptionDate](q.copy(false, Nil).ast, optionDateTEF.createOutMapper)

  implicit def queryTimestampToTE(q: Query[Timestamp]) =
    new QueryValueExpressionNode[Timestamp, TTimestamp](q.copy(false, Nil).ast, timestampTEF.createOutMapper)
  implicit def queryOptionTimestampToTE(q: Query[Option[Timestamp]]) =
    new QueryValueExpressionNode[Option[Timestamp], TOptionTimestamp](
      q.copy(false, Nil).ast,
      optionTimestampTEF.createOutMapper
    )
  implicit def queryTimestampGroupedToTE(q: Query[Group[Timestamp]]) =
    new QueryValueExpressionNode[Timestamp, TTimestamp](q.copy(false, Nil).ast, timestampTEF.createOutMapper)
  implicit def queryOptionTimestampGroupedToTE(q: Query[Group[Option[Timestamp]]]) =
    new QueryValueExpressionNode[Option[Timestamp], TOptionTimestamp](
      q.copy(false, Nil).ast,
      optionTimestampTEF.createOutMapper
    )
  implicit def queryTimestampMeasuredToTE(q: Query[Measures[Timestamp]]) =
    new QueryValueExpressionNode[Timestamp, TTimestamp](q.copy(false, Nil).ast, timestampTEF.createOutMapper)
  implicit def queryOptionTimestampMeasuredToTE(q: Query[Measures[Option[Timestamp]]]) =
    new QueryValueExpressionNode[Option[Timestamp], TOptionTimestamp](
      q.copy(false, Nil).ast,
      optionTimestampTEF.createOutMapper
    )

  implicit def queryBooleanToTE(q: Query[Boolean]) =
    new QueryValueExpressionNode[Boolean, TBoolean](
      q.copy(false, Nil).ast,
      PrimitiveTypeSupport.booleanTEF.createOutMapper
    )
  implicit def queryOptionBooleanToTE(q: Query[Option[Boolean]]) =
    new QueryValueExpressionNode[Option[Boolean], TOptionBoolean](
      q.copy(false, Nil).ast,
      PrimitiveTypeSupport.optionBooleanTEF.createOutMapper
    )

  implicit def queryUUIDToTE(q: Query[UUID]) =
    new QueryValueExpressionNode[UUID, TUUID](q.copy(false, Nil).ast, PrimitiveTypeSupport.uuidTEF.createOutMapper)
  implicit def queryOptionUUIDToTE(q: Query[Option[UUID]]) =
    new QueryValueExpressionNode[Option[UUID], TOptionUUID](
      q.copy(false, Nil).ast,
      PrimitiveTypeSupport.optionUUIDTEF.createOutMapper
    )

  implicit def queryByteArrayToTE(q: Query[Array[Byte]]) =
    new QueryValueExpressionNode[Array[Byte], TByteArray](
      q.copy(false, Nil).ast,
      PrimitiveTypeSupport.binaryTEF.createOutMapper
    )
  implicit def queryOptionByteArrayToTE(q: Query[Option[Array[Byte]]]) =
    new QueryValueExpressionNode[Option[Array[Byte]], TOptionByteArray](
      q.copy(false, Nil).ast,
      PrimitiveTypeSupport.optionByteArrayTEF.createOutMapper
    )

  implicit def queryByteToTE(q: Query[Byte]) =
    new QueryValueExpressionNode[Byte, TByte](q.copy(false, Nil).ast, byteTEF.createOutMapper)
  implicit def queryOptionByteToTE(q: Query[Option[Byte]]) =
    new QueryValueExpressionNode[Option[Byte], TOptionByte](q.copy(false, Nil).ast, optionByteTEF.createOutMapper)
  implicit def queryByteGroupedToTE(q: Query[Group[Byte]]) =
    new QueryValueExpressionNode[Byte, TByte](q.copy(false, Nil).ast, byteTEF.createOutMapper)
  implicit def queryOptionByteGroupedToTE(q: Query[Group[Option[Byte]]]) =
    new QueryValueExpressionNode[Option[Byte], TOptionByte](q.copy(false, Nil).ast, optionByteTEF.createOutMapper)
  implicit def queryByteMeasuredToTE(q: Query[Measures[Byte]]) =
    new QueryValueExpressionNode[Byte, TByte](q.copy(false, Nil).ast, byteTEF.createOutMapper)
  implicit def queryOptionByteMeasuredToTE(q: Query[Measures[Option[Byte]]]) =
    new QueryValueExpressionNode[Option[Byte], TOptionByte](q.copy(false, Nil).ast, optionByteTEF.createOutMapper)

  implicit def queryIntToTE(q: Query[Int]) =
    new QueryValueExpressionNode[Int, TInt](q.copy(false, Nil).ast, intTEF.createOutMapper)
  implicit def queryOptionIntToTE(q: Query[Option[Int]]) =
    new QueryValueExpressionNode[Option[Int], TOptionInt](q.copy(false, Nil).ast, optionIntTEF.createOutMapper)
  implicit def queryIntGroupedToTE(q: Query[Group[Int]]) =
    new QueryValueExpressionNode[Int, TInt](q.copy(false, Nil).ast, intTEF.createOutMapper)
  implicit def queryOptionIntGroupedToTE(q: Query[Group[Option[Int]]]) =
    new QueryValueExpressionNode[Option[Int], TOptionInt](q.copy(false, Nil).ast, optionIntTEF.createOutMapper)
  implicit def queryIntMeasuredToTE(q: Query[Measures[Int]]) =
    new QueryValueExpressionNode[Int, TInt](q.copy(false, Nil).ast, intTEF.createOutMapper)
  implicit def queryOptionIntMeasuredToTE(q: Query[Measures[Option[Int]]]) =
    new QueryValueExpressionNode[Option[Int], TOptionInt](q.copy(false, Nil).ast, optionIntTEF.createOutMapper)

  implicit def queryLongToTE(q: Query[Long]) =
    new QueryValueExpressionNode[Long, TLong](q.copy(false, Nil).ast, longTEF.createOutMapper)
  implicit def queryOptionLongToTE(q: Query[Option[Long]]) =
    new QueryValueExpressionNode[Option[Long], TOptionLong](q.copy(false, Nil).ast, optionLongTEF.createOutMapper)
  implicit def queryLongGroupedToTE(q: Query[Group[Long]]) =
    new QueryValueExpressionNode[Long, TLong](q.copy(false, Nil).ast, longTEF.createOutMapper)
  implicit def queryOptionLongGroupedToTE(q: Query[Group[Option[Long]]]) =
    new QueryValueExpressionNode[Option[Long], TOptionLong](q.copy(false, Nil).ast, optionLongTEF.createOutMapper)
  implicit def queryLongMeasuredToTE(q: Query[Measures[Long]]) =
    new QueryValueExpressionNode[Long, TLong](q.copy(false, Nil).ast, longTEF.createOutMapper)
  implicit def queryOptionLongMeasuredToTE(q: Query[Measures[Option[Long]]]) =
    new QueryValueExpressionNode[Option[Long], TOptionLong](q.copy(false, Nil).ast, optionLongTEF.createOutMapper)

  implicit def queryFloatToTE(q: Query[Float]) =
    new QueryValueExpressionNode[Float, TFloat](q.copy(false, Nil).ast, floatTEF.createOutMapper)
  implicit def queryOptionFloatToTE(q: Query[Option[Float]]) =
    new QueryValueExpressionNode[Option[Float], TOptionFloat](q.copy(false, Nil).ast, optionFloatTEF.createOutMapper)
  implicit def queryFloatGroupedToTE(q: Query[Group[Float]]) =
    new QueryValueExpressionNode[Float, TFloat](q.copy(false, Nil).ast, floatTEF.createOutMapper)
  implicit def queryOptionFloatGroupedToTE(q: Query[Group[Option[Float]]]) =
    new QueryValueExpressionNode[Option[Float], TOptionFloat](q.copy(false, Nil).ast, optionFloatTEF.createOutMapper)
  implicit def queryFloatMeasuredToTE(q: Query[Measures[Float]]) =
    new QueryValueExpressionNode[Float, TFloat](q.copy(false, Nil).ast, floatTEF.createOutMapper)
  implicit def queryOptionFloatMeasuredToTE(q: Query[Measures[Option[Float]]]) =
    new QueryValueExpressionNode[Option[Float], TOptionFloat](q.copy(false, Nil).ast, optionFloatTEF.createOutMapper)

  implicit def queryDoubleToTE(q: Query[Double]) =
    new QueryValueExpressionNode[Double, TDouble](q.copy(false, Nil).ast, doubleTEF.createOutMapper)
  implicit def queryOptionDoubleToTE(q: Query[Option[Double]]) =
    new QueryValueExpressionNode[Option[Double], TOptionDouble](q.copy(false, Nil).ast, optionDoubleTEF.createOutMapper)
  implicit def queryDoubleGroupedToTE(q: Query[Group[Double]]): QueryValueExpressionNode[Double, TDouble] =
    new QueryValueExpressionNode[Double, TDouble](q.copy(false, Nil).ast, doubleTEF.createOutMapper)
  implicit def queryOptionDoubleGroupedToTE(
    q: Query[Group[Option[Double]]]
  ): QueryValueExpressionNode[Option[Double], TOptionDouble] =
    new QueryValueExpressionNode[Option[Double], TOptionDouble](q.copy(false, Nil).ast, optionDoubleTEF.createOutMapper)
  implicit def queryDoubleMeasuredToTE(q: Query[Measures[Double]]): QueryValueExpressionNode[Double, TDouble] =
    new QueryValueExpressionNode[Double, TDouble](q.copy(false, Nil).ast, doubleTEF.createOutMapper)
  implicit def queryOptionDoubleMeasuredToTE(
    q: Query[Measures[Option[Double]]]
  ): QueryValueExpressionNode[Option[Double], TOptionDouble] =
    new QueryValueExpressionNode[Option[Double], TOptionDouble](q.copy(false, Nil).ast, optionDoubleTEF.createOutMapper)

  implicit def queryBigDecimalToTE(q: Query[BigDecimal]): QueryValueExpressionNode[BigDecimal, TBigDecimal] =
    new QueryValueExpressionNode[BigDecimal, TBigDecimal](q.copy(false, Nil).ast, bigDecimalTEF.createOutMapper)
  implicit def queryOptionBigDecimalToTE(
    q: Query[Option[BigDecimal]]
  ): QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal] =
    new QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal](
      q.copy(false, Nil).ast,
      optionBigDecimalTEF.createOutMapper
    )
  implicit def queryBigDecimalGroupedToTE(
    q: Query[Group[BigDecimal]]
  ): QueryValueExpressionNode[BigDecimal, TBigDecimal] =
    new QueryValueExpressionNode[BigDecimal, TBigDecimal](q.copy(false, Nil).ast, bigDecimalTEF.createOutMapper)
  implicit def queryOptionBigDecimalGroupedToTE(
    q: Query[Group[Option[BigDecimal]]]
  ): QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal] =
    new QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal](
      q.copy(false, Nil).ast,
      optionBigDecimalTEF.createOutMapper
    )
  implicit def queryBigDecimalMeasuredToTE(
    q: Query[Measures[BigDecimal]]
  ): QueryValueExpressionNode[BigDecimal, TBigDecimal] =
    new QueryValueExpressionNode[BigDecimal, TBigDecimal](q.copy(false, Nil).ast, bigDecimalTEF.createOutMapper)
  implicit def queryOptionBigDecimalMeasuredToTE(
    q: Query[Measures[Option[BigDecimal]]]
  ): QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal] =
    new QueryValueExpressionNode[Option[BigDecimal], TOptionBigDecimal](
      q.copy(false, Nil).ast,
      optionBigDecimalTEF.createOutMapper
    )

}
