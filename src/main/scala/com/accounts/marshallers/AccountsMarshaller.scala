package com.accounts.marshallers

import com.accounts.domain.TransactionResponse
import org.json4s.{ShortTypeHints, native}

object AccountsMarshaller extends BaseMarshaller {

  override def classList: List[Class[_]] = (classOf[TransactionResponse] :: Nil)

  implicit val formats = native.Serialization.formats(ShortTypeHints(classList))

}