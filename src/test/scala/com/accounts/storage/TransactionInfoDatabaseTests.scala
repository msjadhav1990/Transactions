package com.accounts.storage

import com.accounts.domain.TransactionRequest
import org.scalatest.AsyncFunSpec

import scala.util.{Failure, Try}

class TransactionInfoDatabaseTests extends AsyncFunSpec {



  describe("Create Transaction") {

    it("Should create Credit transaction and return transaction ID") {
      val custInfo= new TransactionInfoDatabase()
      val transactionRequest= TransactionRequest(1,100,"Credit")
val txId= custInfo.createTransaction(transactionRequest)
      assert(txId==1)

      custInfo.getBalance(1) match {
        case Some(balance)=> assert(balance==transactionRequest.value)
        case None=> fail("Should return Balance")
      }

    }

    it("Should create Debit transaction and return transaction ID") {
      val custInfo= new TransactionInfoDatabase()
      val transactionRequest= TransactionRequest(1,100,"Credit")
      val txId= custInfo.createTransaction(transactionRequest)
      assert(txId==1)

      custInfo.getBalance(1) match {
        case Some(balance)=> assert(balance==transactionRequest.value)
        case None=> fail("Should return Balance")
      }

      val transactionRequest1= TransactionRequest(1,10,"Debit")
      val txId1= custInfo.createTransaction(transactionRequest1)
      assert(txId1==2)

      custInfo.getBalance(1) match {
        case Some(balance)=> assert(balance==90.0)
        case None=> fail("Should return Balance")
      }
    }

    it("Should not create Debit transaction for zero balance") {
      val custInfo= new TransactionInfoDatabase()

      val transactionRequest1= TransactionRequest(1,10,"Debit")
      Try(custInfo.createTransaction(transactionRequest1)) match {
        case Failure(_) => succeed
        case _ => fail("Transaction should fail")
      }

    }

    it("Should return exception for unsupported transaction") {
      val custInfo= new TransactionInfoDatabase()

      val transactionRequest1= TransactionRequest(1,10,"Unknown")
      Try(custInfo.createTransaction(transactionRequest1)) match {
        case Failure(_) => succeed
        case _ => fail("Transaction should fail")
      }

    }

  }

  describe("getBalance"){
    it("Should return balance for existing account"){
      val custInfo= new TransactionInfoDatabase()
      val transactionRequest= TransactionRequest(1,100,"Credit")
      val txId= custInfo.createTransaction(transactionRequest)
      assert(txId==1)

      custInfo.getBalance(1) match {
        case Some(balance)=> assert(balance==transactionRequest.value)
        case None=> fail("Should return Balance")
      }
    }

    it("Should not return balance for invalid account"){
      val custInfo= new TransactionInfoDatabase()
      custInfo.getBalance(1) match {
        case None=> succeed
        case _=> fail("Should not return balance")
      }
    }

  }

  describe("Check Account") {
    val custInfo= new TransactionInfoDatabase()
    it("Should return true for existing Account") {
      val transactionRequest= TransactionRequest(1,100,"Credit")
      val txId= custInfo.createTransaction(transactionRequest)
      assert(txId==1)
      assert(custInfo.checkAccountNoExist(1),"Should return true for existing Account")

    }

    it("Should return false for invalid Account") {
      assert(!custInfo.checkAccountNoExist(10),"Should return false for invalid Account")

    }
  }


  describe("Get Transaction") {

    it("Should get transaction list") {
      val custInfo= new TransactionInfoDatabase()
      val transactionRequest= TransactionRequest(1,100,"Credit")
      val txId= custInfo.createTransaction(transactionRequest)
      assert(txId==1)

      custInfo.getAccountDetailsWithTransactions(1) match {
        case Some(txList)=>
          assert(txList.accountId==transactionRequest.accountId)
          assert(txList.txList.size==1)
        case _=> fail("Should return transaction list")
      }

    }

    it("Should not get transaction list") {
      val custInfo= new TransactionInfoDatabase()

      custInfo.getAccountDetailsWithTransactions(1) match {
        case None=> succeed
        case _=> fail("Should return transaction list")
      }

    }

  }
}
