package com.accounts.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.{ExceptionHandler, Route, RouteResult}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.accounts.common.JsonResponse
import com.accounts.domain.{GenericHttpResponse, GetAccountDetailsRequest, TransactionRequest}
import com.accounts.marshallers.AccountsMarshaller._
import com.accounts.marshallers.Json4sMarshaller._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class AccountsRoutes(routingActor: ActorRef)(implicit ec:ExecutionContext ) {
  def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case t: Exception =>
      t.printStackTrace()
      extractUri { uri =>
        complete(HttpResponse(StatusCodes.InternalServerError, entity = "Error " + t.getMessage))
      }
  }

  implicit val timeout: Timeout  = Timeout(6000 milliseconds)
  def routes(implicit mat: ActorMaterializer): Route = {
    AccountsOperationRoute.route(routingActor)
  }

  def apiRoutes(implicit mat: ActorMaterializer): Route =
    handleExceptions(exceptionHandler) {
      logAccess{
        routes
      }

    }

  def logAccess(innerRoute: Route): Route = {
    def toLogEntry(marker: String, f: Any => String) = (r: Any) => LogEntry(marker + f(r), akka.event.Logging.InfoLevel)
    extractRequest { request =>
      logResult(toLogEntry(s"${request.method.name} ${request.uri} ==> ", {
        case c: RouteResult.Complete => c.response.status.toString()
        case x                       => s"unknown response part of type ${x.getClass}"
      }))(innerRoute)
    }
  }
}



object AccountsOperationRoute{
  def route(rootActor: ActorRef)(implicit timeout: Timeout): Route =  pathPrefix("app" / "accounts"/"transactions") {

    get {
      path(LongNumber){ custId=>

        val getAccountsMessage= GetAccountDetailsRequest(custId)
        val genericHttpResponse= rootActor ? getAccountsMessage
         onSuccess(genericHttpResponse) {
           case GenericHttpResponse(StatusCodes.OK, t: JsonResponse) =>  {
             complete(HttpEntity(`application/json`, t.toJson)) }
           case GenericHttpResponse(x: StatusCode, msg: String) => { complete(HttpResponse(status = x, entity = HttpEntity(`application/json`, msg)))  }
           case p => {
             println(p)
             complete(StatusCodes.InternalServerError, "Unknown error occurred. Please contact administrator.") }
        }

      }

    } ~
      post {
        pathEnd {
          entity(as[TransactionRequest]) { c =>
            val genericHttpResponse= rootActor ? c
            onSuccess(genericHttpResponse) {
              case GenericHttpResponse(StatusCodes.OK, t: JsonResponse) =>  {
                complete(HttpEntity(`application/json`, t.toJson)) }
              case GenericHttpResponse(x: StatusCode, msg: String) => { complete(HttpResponse(status = x, entity = HttpEntity(`application/json`, msg)))  }
              case p => {
                println(p)
                complete(StatusCodes.InternalServerError, "Unknown error occurred. Please contact administrator.") }
            }
          }
        }
      } ~
      complete(StatusCodes.MethodNotAllowed)
  }

}
