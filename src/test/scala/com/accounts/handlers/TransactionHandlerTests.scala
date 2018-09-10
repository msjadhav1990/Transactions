package com.accounts.handlers

import java.util.concurrent.Executors

import akka.http.scaladsl.model.{StatusCodes,StatusCode}
import com.accounts.config.ServiceConfiguration
import com.accounts.context.ResourceContext
import com.accounts.domain._
import com.accounts.storage.TransactionInfoDatabase
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class TransactionHandlerTests extends AsyncFunSpec with Matchers with DiagrammedAssertions
  with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar  with ScalaFutures{
  val config = ConfigFactory.load("test")
  val testConfig = ServiceConfiguration(config)
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  describe("createTransaction") {
    it("Create Transaction") {
      val transactionRequest= TransactionRequest(1,100,"Credit")


     implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
      whenReady(TransactionHandler.createTransaction(transactionRequest)) {
        case GenericHttpResponse(code:StatusCode,response:TransactionResponse)=>
          assert(code.intValue()==200)
          assert(response.txId==1)
        case _=> fail("Should return 200 ok response with tx id")

      }
    }

    it("Should not Create Transaction") {
      val transactionRequest= TransactionRequest(1,100,"Debit")


      implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
      whenReady(TransactionHandler.createTransaction(transactionRequest)) {
        case GenericHttpResponse(code:StatusCode,response:String)=>
          assert(code==StatusCodes.InternalServerError)
        case _=> fail("Should return 500 response ")

      }
    }

  }

  describe("Get Transactions"){

    it("Create Transaction") {
      val transactionRequest= TransactionRequest(1,100,"Credit")
      implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
      whenReady(TransactionHandler.createTransaction(transactionRequest)) {
        case GenericHttpResponse(code:StatusCode,response:TransactionResponse)=>
          assert(code.intValue()==200)
          assert(response.txId==1)
          whenReady(TransactionHandler.getAccountAndTransactions(1)) {
            case GenericHttpResponse(code:StatusCode,response:AccountDetailsWithTransactions)=>
              assert(code == StatusCodes.OK)
              assert(response.accountId==1)
              assert(response.txList.size==1)
            case _=> fail("Should return 200 ok response with tx list")
          }

        case _=> fail("Should return 200 ok response with tx id")

      }
    }

    it("Should not Create Transaction") {
      val transactionRequest= TransactionRequest(1,100,"Debit")

      implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
      whenReady(TransactionHandler.getAccountAndTransactions(1)) {
        case GenericHttpResponse(code:StatusCode,response:String)=>
          assert(code == StatusCodes.NotFound)
        case _=> fail("Should return 404 ok response ")
      }
    }

  }



}
