package com.accounts.storage

import java.util.Date
import java.util.concurrent.Semaphore

import com.accounts.domain._

import scala.collection.mutable


class TransactionInfoDatabase {


  private var transactionInfo: mutable.Map[Long,AccountDetailsWithTransactions]= mutable.HashMap.empty

  private var  transactionCnt=0l
  private def getTransactionId(): Long = {
    val semaphore = new Semaphore(1);
semaphore.tryAcquire() match {
  case true=> transactionCnt= transactionCnt + 1l
  case false=> throw new RuntimeException("Unable to acquire lock")
}
    semaphore.release()
    transactionCnt
  }
  def getAccountDetailsWithTransactions(accountNo:Long):Option[AccountDetailsWithTransactions]={
    transactionInfo.get(accountNo)
  }

  def checkAccountNoExist(accountNo:Long): Boolean ={
    transactionInfo.contains(accountNo)
  }

  def getBalance(accountNo:Long):Option[Double]={
    transactionInfo.get(accountNo) match {
      case Some(accnt)=> Some(accnt.balance)
      case None=> None
    }
  }
  def createTransaction(transactionRequest:TransactionRequest):Long= synchronized {
    transactionInfo.get(transactionRequest.accountId) match {
      case Some(acct)=>
        val tx = createTransaction(transactionRequest, acct.balance)
        acct.txList.append(tx)
        acct.balance=tx.balance
        tx.transactionId
      case None=>
        val txList=mutable.ListBuffer.empty[TransactionDetails];
        val tx = createTransaction(transactionRequest, 0)
        txList.append(tx)
        val acct= AccountDetailsWithTransactions(transactionRequest.accountId,tx.balance,txList)
        transactionInfo.put(acct.accountId,acct)
        tx.transactionId
    }

  }

  private def createTransaction(transactionRequest: TransactionRequest, balance:Double) = {
    val balanceUpdated = transactionRequest.`type` match {
      case "Credit" => balance + transactionRequest.value
      case "Debit" => if(!(balance<transactionRequest.value || balance==0)) {
        balance - transactionRequest.value
      }
        else{
        throw new RuntimeException("Insufficient Balance in Account")
      }
      case _ => throw new RuntimeException("Unsupported Operation")
    }
    val txId = getTransactionId()
    TransactionDetails(txId, transactionRequest.value, transactionRequest.`type`, balanceUpdated, new Date())
  }
}
