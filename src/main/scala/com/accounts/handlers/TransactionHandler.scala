package com.accounts.handlers

import akka.http.scaladsl.model.StatusCodes
import com.accounts.context.ResourceContext
import com.accounts.domain._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object TransactionHandler {

  def createTransaction(transactionRequest: TransactionRequest)(implicit ec:ExecutionContext, rc:ResourceContext): Future[GenericHttpResponse] = {
    implicit val sc= rc.sc
    Future {
      Try(rc.db.createTransaction(transactionRequest)) match {
        case Success(tx:Long)=> GenericHttpResponse(StatusCodes.OK,TransactionResponse(tx))
        case Failure(ex)=> GenericHttpResponse(StatusCodes.InternalServerError,ex.getMessage)
      }
}

  }

  def getAccountAndTransactions(accountNo:Long)(implicit ec:ExecutionContext, rc:ResourceContext):Future[GenericHttpResponse]={
  implicit val sc= rc.sc
    Future{
    rc.db.checkAccountNoExist(accountNo) match {
      case true=> rc.db.getAccountDetailsWithTransactions(accountNo) match {
        case Some(accntInfo)=>
          GenericHttpResponse(StatusCodes.OK,accntInfo)
        case None=> GenericHttpResponse(StatusCodes.NotFound,s"Account Information not found for $accountNo")
      }
      case false=> GenericHttpResponse(StatusCodes.NotFound,s"Account Information not found for $accountNo")
    }
    }
  }
}
