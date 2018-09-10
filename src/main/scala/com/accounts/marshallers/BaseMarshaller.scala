package com.accounts.marshallers

import org.json4s.{Formats, native}

import scala.util.{Success, Try}

trait BaseMarshaller {
  implicit val serialization = native.Serialization

  def classList: List[Class[_]]

  implicit def formats: Formats

  def resolveEntity[T](cmd: String)(implicit m: Manifest[T]): Option[T] = {
    Try {
      serialization.read[T](cmd)
    } match {
      case Success(cmdT) =>
        Some(cmdT)
      case _ =>
        None
    }
  }
}