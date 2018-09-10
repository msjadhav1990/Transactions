package com.accounts.actors

import akka.actor.{Actor, Props}
import com.accounts.context.ResourceContext
import com.accounts.domain.{GetAccountDetailsRequest, TransactionRequest}
import com.accounts.handlers.TransactionHandler

import scala.concurrent.ExecutionContext


class AccountsActor(implicit ec: ExecutionContext,rc:ResourceContext) extends Actor {

  override def receive: Receive = {
    case cmd: TransactionRequest =>
      val senderRef = sender
      TransactionHandler.createTransaction(cmd).map(d=>{
        senderRef ! d
      })

    case cmd:GetAccountDetailsRequest => val senderRef = sender
      TransactionHandler.getAccountAndTransactions(cmd.accountNo).map(d=>{
        senderRef ! d
      })

  }
}

object AccountsActor {
  def props()(implicit ec: ExecutionContext,rc:ResourceContext) = Props(new AccountsActor())
}