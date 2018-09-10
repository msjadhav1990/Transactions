import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.accounts.actors.RouterActor
import com.accounts.config.ServiceConfiguration
import com.accounts.context.ResourceContext
import com.accounts.routes.AccountsRoutes
import com.accounts.storage.TransactionInfoDatabase
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

object  AccountsServiceApp {

  def start()(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Future[Done] = {
    val db= new TransactionInfoDatabase()
    val config= ConfigFactory.load("application")
    val sc: ServiceConfiguration= ServiceConfiguration(config)
    implicit val rc:ResourceContext= new ResourceContext(db,sc)
    val routerActor = system.actorOf(RouterActor.props, "router-actor")
    Http(system).bindAndHandle(
      AccountsRoutes(routerActor).apiRoutes(mat),"0.0.0.0",9002) .map(x => Done)

  }

}
