package com.ephox
package argonaut

import scalaz._, Scalaz._, LensT._

trait DecodeJson[-A] {
  def name: String

  def apply(a: A): Json

  def contramap[B](f: B => A): DecodeJson[B] =
    DecodeJson(b => apply(f(b)), name)
}

object DecodeJson extends DecodeJsons {
  def apply[A](f: A => Json, n: String): DecodeJson[A] =
    new DecodeJson[A] {
      def name = n
      def apply(a: A) = f(a)
    }
}

trait DecodeJsons {
  import JsonLike._

  implicit def IdDecodeJson: DecodeJson[Json] =
    DecodeJson(q => q, "Json")

  implicit def ListDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[List[A]] =
    DecodeJson(a => jArray[Json](a map (e(_))), "[A]List[A]")

  implicit def StreamDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[Stream[A]] =
    DecodeJson(a => jArray[Json](a.toList map (e(_))), "[A]Stream[A]")

  implicit def StringDecodeJson: DecodeJson[String] =
    DecodeJson(jString[Json], "String")

  implicit def DoubleDecodeJson: DecodeJson[Double] =
    DecodeJson(jNumber[Json], "Double")

  implicit def FloatDecodeJson: DecodeJson[Float] =
    DecodeJson(a => jNumber[Json](a.toFloat), "Float")

  implicit def IntDecodeJson: DecodeJson[Int] =
    DecodeJson(a => jNumber[Json](a.toInt), "Int")

  implicit def LongDecodeJson: DecodeJson[Long] =
    DecodeJson(a => jNumber[Json](a.toLong), "Long")

  implicit def BooleanDecodeJson: DecodeJson[Boolean] =
    DecodeJson(jBool[Json], "Boolean")

  implicit def CharDecodeJson: DecodeJson[Char] =
    DecodeJson(a => jString[Json](a.toString), "Char")

  implicit def JDoubleDecodeJson: DecodeJson[java.lang.Double] =
    DecodeJson(a => jNumber[Json](a.doubleValue), "java.lang.Double")

  implicit def JFloatDecodeJson: DecodeJson[java.lang.Float] =
    DecodeJson(a => jNumber[Json](a.floatValue.toDouble), "java.lang.Float")

  implicit def JIntegerDecodeJson: DecodeJson[java.lang.Integer] =
    DecodeJson(a => jNumber[Json](a.intValue.toDouble), "java.lang.Integer")

  implicit def JLongDecodeJson: DecodeJson[java.lang.Long] =
    DecodeJson(a => jNumber[Json](a.longValue.toDouble), "java.lang.Long")

  implicit def JBooleanDecodeJson: DecodeJson[java.lang.Boolean] =
    DecodeJson(a => jBool[Json](a.booleanValue), "java.lang.Boolean")

  implicit def JCharacterDecodeJson: DecodeJson[Char] =
    DecodeJson(a => jString[Json](a.toString), "java.lang.Character")

  implicit def OptionDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[Option[A]] =
    DecodeJson(_ match {
      case None => jNull[Json]
      case Some(a) => e(a)
    }, "[A]Option[A]")

  implicit def EitherDecodeJson[A, B](implicit ea: DecodeJson[A], eb: DecodeJson[B]): DecodeJson[Either[A, B]] =
    DecodeJson(_ match {
      case Left(a) => jSingleObject[Json]("Left", ea(a))
      case Right(b) => jSingleObject[Json]("Right", eb(b))
    }, "[A, B]Either[A, B]")

  implicit def ValidationDecodeJson[E, A](implicit ea: DecodeJson[E], eb: DecodeJson[A]): DecodeJson[Validation[E, A]] =
    DecodeJson(_ fold (
      failure = e => jSingleObject[Json]("Failure", ea(e))
    , success = a => jSingleObject[Json]("Success", eb(a))
    ), "[E, A]Validation[E, A]")

  implicit def MapDecodeJson[V](implicit e: DecodeJson[V]): DecodeJson[Map[String, V]] =
    DecodeJson(ListDecodeJson[(String, V)] contramap ((_: Map[String, V]).toList) apply _, "[V]Map[String, V]")

  implicit def SetDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[Set[A]] =
    DecodeJson(ListDecodeJson[A] contramap ((_: Set[A]).toList) apply _, "[A]Set[A]")

  implicit def Tuple2DecodeJson[A, B](implicit ea: DecodeJson[A], eb: DecodeJson[B]): DecodeJson[(A, B)] =
    DecodeJson({
      case (a, b) => jArray[Json](List(ea(a), eb(b)))
    }, "[A, B](A, B)")

  implicit def Tuple3DecodeJson[A, B, C](implicit ea: DecodeJson[A], eb: DecodeJson[B], ec: DecodeJson[C]): DecodeJson[(A, B, C)] =
    DecodeJson({
      case (a, b, c) => jArray[Json](List(ea(a), eb(b), ec(c)))
    }, "[A, B, C](A, B, C)")


  implicit def DecodeJsonContra: Contravariant[DecodeJson] = new Contravariant[DecodeJson] {
    def contramap[A, B](r: DecodeJson[A])(f: B => A) = r contramap f
  }
}