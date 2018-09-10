package com.accounts.domain

import com.accounts.common.JsonResponse
import org.json4s.NoTypeHints
import org.json4s.native.Serialization

import scala.collection.mutable.ListBuffer



case class AccountDetailsWithTransactions(accountId:Long, var balance:Double,txList:ListBuffer[TransactionDetails]) extends JsonResponse{
  implicit val formats = Serialization.formats(NoTypeHints)
  override def toJson = Serialization.write(this)
}

