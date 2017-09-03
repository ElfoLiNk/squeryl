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
package org.squeryl.customtypes

import java.util.{ Date, UUID }
import org.squeryl.dsl._
import java.sql.Timestamp
import org.squeryl.internals.FieldMapper

trait CustomType[T] extends Product1[T] {
  def value: T
  def _1: T            = value
  def canEqual(a: Any) = false
}

trait CustomTypesMode extends QueryDsl with FieldMapper {

  private val ps = PrimitiveTypeSupport

  val stringTEF = new NonPrimitiveJdbcMapper[String, StringField, TString](ps.stringTEF, this) {
    def convertFromJdbc(v: String)            = new StringField(v)
    def convertToJdbc(v: StringField): String = v.value
  }

  val optionStringTEF =
    new TypedExpressionFactory[Option[StringField], TOptionString]
    with DeOptionizer[String, StringField, TString, Option[StringField], TOptionString] {
      val deOptionizer: NonPrimitiveJdbcMapper[String, StringField, TString] {
        def convertFromJdbc(v: String): StringField

        def convertToJdbc(v: StringField): String
      } = stringTEF
    }

  val dateTEF =
    new NonPrimitiveJdbcMapper[Date, DateField, TDate](ps.dateTEF, this) {
      def convertFromJdbc(v: Date)          = new DateField(v)
      def convertToJdbc(v: DateField): Date = v.value
    }

  val optionDateTEF = new TypedExpressionFactory[Option[DateField], TOptionDate]
  with DeOptionizer[Date, DateField, TDate, Option[DateField], TOptionDate] {
    val deOptionizer: NonPrimitiveJdbcMapper[Date, DateField, TDate] {
      def convertFromJdbc(v: Date): DateField

      def convertToJdbc(v: DateField): Date
    } = dateTEF
  }

  val timestampTEF =
    new NonPrimitiveJdbcMapper[Timestamp, TimestampField, TTimestamp](ps.timestampTEF, this) {
      def convertFromJdbc(v: Timestamp)               = new TimestampField(v)
      def convertToJdbc(v: TimestampField): Timestamp = v.value

    }

  val optionTimestampTEF =
    new TypedExpressionFactory[Option[TimestampField], TOptionTimestamp]
    with DeOptionizer[Timestamp, TimestampField, TTimestamp, Option[TimestampField], TOptionTimestamp] {
      val deOptionizer: NonPrimitiveJdbcMapper[Timestamp, TimestampField, TTimestamp] {
        def convertFromJdbc(v: Timestamp): TimestampField

        def convertToJdbc(v: TimestampField): Timestamp
      } = timestampTEF
    }

  val booleanTEF = new NonPrimitiveJdbcMapper[Boolean, BooleanField, TBoolean](ps.booleanTEF, this) {
    def convertFromJdbc(v: Boolean)             = new BooleanField(v)
    def convertToJdbc(v: BooleanField): Boolean = v.value
  }

  val optionBooleanTEF =
    new TypedExpressionFactory[Option[BooleanField], TOptionBoolean]
    with DeOptionizer[Boolean, BooleanField, TBoolean, Option[BooleanField], TOptionBoolean] {
      val deOptionizer: NonPrimitiveJdbcMapper[Boolean, BooleanField, TBoolean] {
        def convertFromJdbc(v: Boolean): BooleanField

        def convertToJdbc(v: BooleanField): Boolean
      } = booleanTEF
    }

  val uuidTEF =
    new NonPrimitiveJdbcMapper[UUID, UuidField, TUUID](ps.uuidTEF, this) {
      def convertFromJdbc(v: UUID)          = new UuidField(v)
      def convertToJdbc(v: UuidField): UUID = v.value
    }

  val optionUUIDTEF = new TypedExpressionFactory[Option[UuidField], TOptionUUID]
  with DeOptionizer[UUID, UuidField, TUUID, Option[UuidField], TOptionUUID] {
    val deOptionizer: NonPrimitiveJdbcMapper[UUID, UuidField, TUUID] {
      def convertFromJdbc(v: UUID): UuidField

      def convertToJdbc(v: UuidField): UUID
    } = uuidTEF
  }

  // =========================== Numerical Integral ===========================

  val byteTEF =
    new NonPrimitiveJdbcMapper[Byte, ByteField, TByte](ps.byteTEF, this) {
      def convertFromJdbc(v: Byte)          = new ByteField(v)
      def convertToJdbc(v: ByteField): Byte = v.value
    }

  val optionByteTEF =
    new IntegralTypedExpressionFactory[Option[ByteField], TOptionByte, Option[FloatField], TOptionFloat]
    with DeOptionizer[Byte, ByteField, TByte, Option[ByteField], TOptionByte] {
      val deOptionizer: NonPrimitiveJdbcMapper[Byte, ByteField, TByte] {
        def convertFromJdbc(v: Byte): ByteField

        def convertToJdbc(v: ByteField): Byte
      } = byteTEF
      val floatifyer: FloatTypedExpressionFactory[Option[FloatField], TOptionFloat] with DeOptionizer[
        Float,
        FloatField,
        TFloat,
        Option[FloatField],
        TOptionFloat
      ] {
        val deOptionizer: NonPrimitiveJdbcMapper[Float, FloatField, TFloat] with FloatTypedExpressionFactory[
          FloatField,
          TFloat
        ] {
          def convertFromJdbc(v: Float): FloatField

          def convertToJdbc(v: FloatField): Float
        }
      } = optionFloatTEF
    }

  val intTEF = new NonPrimitiveJdbcMapper[Int, IntField, TInt](ps.intTEF, this)
  with IntegralTypedExpressionFactory[IntField, TInt, FloatField, TFloat] {
    val floatifyer
      : NonPrimitiveJdbcMapper[Float, FloatField, TFloat] with FloatTypedExpressionFactory[FloatField, TFloat] {
        def convertFromJdbc(v: Float): FloatField

        def convertToJdbc(v: FloatField): Float
      }                                 = floatTEF
    def convertFromJdbc(v: Int)         = new IntField(v)
    def convertToJdbc(v: IntField): Int = v.value
  }

  val optionIntTEF = new IntegralTypedExpressionFactory[Option[IntField], TOptionInt, Option[FloatField], TOptionFloat]
  with DeOptionizer[Int, IntField, TInt, Option[IntField], TOptionInt] {
    val deOptionizer: NonPrimitiveJdbcMapper[Int, IntField, TInt] with IntegralTypedExpressionFactory[
      IntField,
      TInt,
      FloatField,
      TFloat
    ] {
      def convertFromJdbc(v: Int): IntField

      val floatifyer: NonPrimitiveJdbcMapper[Float, FloatField, TFloat] with FloatTypedExpressionFactory[
        FloatField,
        TFloat
      ] {
        def convertFromJdbc(v: Float): FloatField

        def convertToJdbc(v: FloatField): Float
      }

      def convertToJdbc(v: IntField): Int
    } = intTEF
    val floatifyer: FloatTypedExpressionFactory[Option[FloatField], TOptionFloat] with DeOptionizer[
      Float,
      FloatField,
      TFloat,
      Option[FloatField],
      TOptionFloat
    ] {
      val deOptionizer: NonPrimitiveJdbcMapper[Float, FloatField, TFloat] with FloatTypedExpressionFactory[
        FloatField,
        TFloat
      ] {
        def convertFromJdbc(v: Float): FloatField

        def convertToJdbc(v: FloatField): Float
      }
    } = optionFloatTEF
  }

  val longTEF = new NonPrimitiveJdbcMapper[Long, LongField, TLong](ps.longTEF, this)
  with IntegralTypedExpressionFactory[LongField, TLong, DoubleField, TDouble] {
    val floatifyer
      : NonPrimitiveJdbcMapper[Double, DoubleField, TDouble] with FloatTypedExpressionFactory[DoubleField, TDouble] {
        def convertFromJdbc(v: Double): DoubleField

        def convertToJdbc(v: DoubleField): Double
      }                                   = doubleTEF
    def convertFromJdbc(v: Long)          = new LongField(v)
    def convertToJdbc(v: LongField): Long = v.value
  }

  val optionLongTEF =
    new IntegralTypedExpressionFactory[Option[LongField], TOptionLong, Option[DoubleField], TOptionDouble]
    with DeOptionizer[Long, LongField, TLong, Option[LongField], TOptionLong] {
      val deOptionizer: NonPrimitiveJdbcMapper[Long, LongField, TLong] with IntegralTypedExpressionFactory[
        LongField,
        TLong,
        DoubleField,
        TDouble
      ] {
        def convertFromJdbc(v: Long): LongField

        val floatifyer: NonPrimitiveJdbcMapper[Double, DoubleField, TDouble] with FloatTypedExpressionFactory[
          DoubleField,
          TDouble
        ] {
          def convertFromJdbc(v: Double): DoubleField

          def convertToJdbc(v: DoubleField): Double
        }

        def convertToJdbc(v: LongField): Long
      } = longTEF
      val floatifyer: FloatTypedExpressionFactory[Option[DoubleField], TOptionDouble] with DeOptionizer[
        Double,
        DoubleField,
        TDouble,
        Option[DoubleField],
        TOptionDouble
      ] {
        val deOptionizer: NonPrimitiveJdbcMapper[Double, DoubleField, TDouble] with FloatTypedExpressionFactory[
          DoubleField,
          TDouble
        ] {
          def convertFromJdbc(v: Double): DoubleField

          def convertToJdbc(v: DoubleField): Double
        }
      } = optionDoubleTEF
    }

  // =========================== Numerical Floating Point ===========================

  val floatTEF =
    new NonPrimitiveJdbcMapper[Float, FloatField, TFloat](ps.floatTEF, this)
    with FloatTypedExpressionFactory[FloatField, TFloat] {
      def convertFromJdbc(v: Float)           = new FloatField(v)
      def convertToJdbc(v: FloatField): Float = v.value
    }

  val optionFloatTEF =
    new FloatTypedExpressionFactory[Option[FloatField], TOptionFloat]
    with DeOptionizer[Float, FloatField, TFloat, Option[FloatField], TOptionFloat] {
      val deOptionizer
        : NonPrimitiveJdbcMapper[Float, FloatField, TFloat] with FloatTypedExpressionFactory[FloatField, TFloat] {
          def convertFromJdbc(v: Float): FloatField

          def convertToJdbc(v: FloatField): Float
        } = floatTEF
    }

  val doubleTEF =
    new NonPrimitiveJdbcMapper[Double, DoubleField, TDouble](ps.doubleTEF, this)
    with FloatTypedExpressionFactory[DoubleField, TDouble] {
      def convertFromJdbc(v: Double)            = new DoubleField(v)
      def convertToJdbc(v: DoubleField): Double = v.value
    }

  val optionDoubleTEF =
    new FloatTypedExpressionFactory[Option[DoubleField], TOptionDouble]
    with DeOptionizer[Double, DoubleField, TDouble, Option[DoubleField], TOptionDouble] {
      val deOptionizer
        : NonPrimitiveJdbcMapper[Double, DoubleField, TDouble] with FloatTypedExpressionFactory[DoubleField, TDouble] {
          def convertFromJdbc(v: Double): DoubleField

          def convertToJdbc(v: DoubleField): Double
        } = doubleTEF
    }

  val bigDecimalTEF =
    new NonPrimitiveJdbcMapper[BigDecimal, BigDecimalField, TBigDecimal](ps.bigDecimalTEF, this)
    with FloatTypedExpressionFactory[BigDecimalField, TBigDecimal] {
      def convertFromJdbc(v: BigDecimal)                = new BigDecimalField(v)
      def convertToJdbc(v: BigDecimalField): BigDecimal = v.value
    }

  val optionBigDecimalTEF =
    new FloatTypedExpressionFactory[Option[BigDecimalField], TOptionBigDecimal]
    with DeOptionizer[BigDecimal, BigDecimalField, TBigDecimal, Option[BigDecimalField], TOptionBigDecimal] {
      val deOptionizer
        : NonPrimitiveJdbcMapper[BigDecimal, BigDecimalField, TBigDecimal] with FloatTypedExpressionFactory[
          BigDecimalField,
          TBigDecimal
        ] {
          def convertFromJdbc(v: BigDecimal): BigDecimalField

          def convertToJdbc(v: BigDecimalField): BigDecimal
        } = bigDecimalTEF
    }

  implicit def stringToTE(s: String): TypedExpression[StringField, TString] =
    stringTEF.createFromNativeJdbcValue(s)
  implicit def optionStringToTE(s: Option[String]): Option[StringField] =
    s.map(new StringField(_))

  implicit def dateToTE(s: Date): TypedExpression[DateField, TDate] =
    dateTEF.createFromNativeJdbcValue(s)
  implicit def optionDateToTE(s: Option[Date]): Option[DateField] =
    s.map(new DateField(_))

  implicit def timestampToTE(s: Timestamp): TypedExpression[TimestampField, TTimestamp] =
    timestampTEF.createFromNativeJdbcValue(s)
  implicit def optionTimestampToTE(s: Option[Timestamp]): Option[TimestampField] =
    s.map(new TimestampField(_))

  implicit def booleanToTE(s: Boolean): TypedExpression[BooleanField, TBoolean] =
    booleanTEF.createFromNativeJdbcValue(s)
  implicit def optionBooleanToTE(s: Option[Boolean]): Option[BooleanField] =
    s.map(new BooleanField(_))

  implicit def uuidToTE(s: UUID): TypedExpression[UuidField, TUUID] =
    uuidTEF.createFromNativeJdbcValue(s)
  implicit def optionUUIDToTE(s: Option[UUID]): Option[UuidField] =
    s.map(new UuidField(_))

  implicit def byteToTE(f: Byte): TypedExpression[ByteField, TByte] =
    byteTEF.createFromNativeJdbcValue(f)
  implicit def optionByteToTE(f: Option[Byte]): Option[ByteField] =
    f.map(new ByteField(_))

  implicit def intToTE(f: IntField): TypedExpression[IntField, TInt] =
    intTEF.create(f)
  implicit def optionIntToTE(f: Option[IntField]): TypedExpression[Option[IntField], TOptionInt] =
    optionIntTEF.create(f)

  //implicit def _intToTE(f: Int) = intTEF.createFromNativeJdbcValue(f)
  //implicit def _optionIntToTE(f: Option[Int]) = f.map(new IntField(_))

  implicit def longToTE(f: Long): TypedExpression[LongField, TLong] =
    longTEF.createFromNativeJdbcValue(f)
  implicit def optionLongToTE(f: Option[Long]): Option[LongField] =
    f.map(new LongField(_))

  implicit def floatToTE(f: Float): TypedExpression[FloatField, TFloat] =
    floatTEF.createFromNativeJdbcValue(f)
  implicit def optionFloatToTE(f: Option[Float]): Option[FloatField] =
    f.map(new FloatField(_))

  implicit def doubleToTE(f: Double): TypedExpression[DoubleField, TDouble] =
    doubleTEF.createFromNativeJdbcValue(f)
  implicit def optionDoubleToTE(f: Option[Double]): Option[DoubleField] =
    f.map(new DoubleField(_))

  implicit def bigDecimalToTE(f: BigDecimal): TypedExpression[BigDecimalField, TBigDecimal] =
    bigDecimalTEF.createFromNativeJdbcValue(f)
  implicit def optionBigDecimalToTE(f: Option[BigDecimal]): Option[BigDecimalField] =
    f.map(new BigDecimalField(_))
}

object CustomTypesMode extends CustomTypesMode

class ByteField(val value: Byte) extends CustomType[Byte]

class IntField(val value: Int) extends CustomType[Int]

class StringField(val value: String) extends CustomType[String]

class DoubleField(val value: Double) extends CustomType[Double]

class BigDecimalField(val value: BigDecimal) extends CustomType[BigDecimal]

class FloatField(val value: Float) extends CustomType[Float]

class LongField(val value: Long) extends CustomType[Long]

class BooleanField(val value: Boolean) extends CustomType[Boolean]

class DateField(val value: Date) extends CustomType[Date]

class TimestampField(val value: Timestamp) extends CustomType[Timestamp]

class BinaryField(val value: Array[Byte]) extends CustomType[Array[Byte]]

class UuidField(val value: UUID) extends CustomType[UUID]
