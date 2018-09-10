package com.accounts.actors

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import com.accounts.config.ServiceConfiguration
import com.accounts.context.ResourceContext
import com.accounts.domain._
import com.accounts.storage.TransactionInfoDatabase
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class AccountsActorTests extends AsyncFunSpec with Matchers with DiagrammedAssertions
  with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar  with ScalaFutures{
  val config = ConfigFactory.load("test")
  val testConfig = ServiceConfiguration(config)
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val actorSystem = ActorSystem("CreateStreamActorTests", config)

  describe("create transaction") {
    implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
    val accountsActor = actorSystem.actorOf(AccountsActor.props())
    implicit val timeout = Timeout(30 seconds)
    it("Should create transaction") {

      val transactionRequest= TransactionRequest(1,100,"Credit")

      whenReady(accountsActor ? transactionRequest) {
        case GenericHttpResponse(code:StatusCode,response:TransactionResponse)=>
          assert(code.intValue()==200)
          assert(response.txId==1)
        case _=> fail("Should return 200 ok response with transaction id")

      }
    }


  }

  describe("get Transactions") {
    implicit  val resourceContext= ResourceContext(new TransactionInfoDatabase(),testConfig)
    val accountsActor = actorSystem.actorOf(AccountsActor.props())
    implicit val timeout = Timeout(30 seconds)
    it("Should return accounts for  account") {

      val transactionRequest= TransactionRequest(1,100,"Credit")

      whenReady(accountsActor ? transactionRequest) {
        case GenericHttpResponse(code:StatusCode,response:TransactionResponse)=>
          assert(code.intValue()==200)
          assert(response.txId==1)
          whenReady(accountsActor ? GetAccountDetailsRequest(1)) {
            case GenericHttpResponse(code:StatusCode,response:AccountDetailsWithTransactions)=>
              assert(code==StatusCodes.OK)
              assert(response.accountId==transactionRequest.accountId)
              assert(response.txList.size==1)

            case _=> fail("Should return 200 ok response with accounts information")

          }
        case _=> fail("Should return 200 ok response with transaction id")

      }


    }


  }


}
