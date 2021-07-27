package com.scalawilliam

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits.toSemigroupKOps
import com.scalawilliam.letsencrypt.LetsEncryptScala
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.server.{Server, defaults}

import java.net.InetAddress
import scala.concurrent.ExecutionContext

object LetsEncryptHttp4sSampleApp extends IOApp {

  /**
    * The pure HTTP parts.
    * This code you can re-use easily in a Jetty or Tomcat servlet
    * or any other server supported by http4s. It's really amazing!
    */
  private def httpRoutes: HttpRoutes[IO] = {
    import org.http4s.dsl.io._
    HttpRoutes.of[IO] {

      /** Capture all the requests */
      case req @ GET -> _ =>
        import org.http4s.scalatags._
        import scalatags.Text.all._

        /** We use scalatags - a cross-platform (Scala-JVM & Scala.js) solution to render HTML without the noise */
        Ok(
          html(
            head(link(rel := "stylesheet", href := "/style.css")),
            body(
              h1("Hello, ",
                 img(src := "/Scala-full-color.svg", alt := "Scala logo"),
                 "!"),
              p(s"Here are the details from your request:"),
              code(req.toString),
              p("Other information:"),
              table(
                tbody(
                  List[(String, Any)](
                    "Remote"          -> req.remote,
                    "Remote user"     -> req.remoteUser,
                    "From"            -> req.from,
                    "Server"          -> req.server,
                    "Server"          -> req.serverAddr,
                    "Server"          -> req.serverPort,
                    "Is secure?"      -> req.isSecure,
                    "Server software" -> req.serverSoftware,
                    "Attributes"      -> req.attributes.toString,
                  ).map {
                    case (k, v) => tr(th(k), td(v.toString))
                  }
                )
              )
            )
          ))
    }
  }

  /** Server assets; in particular the stylesheet for nice visuals */
  private def assetRoutes =
    resourceServiceBuilder[IO](
      "/web-assets",
      Blocker.liftExecutionContext(ExecutionContext.global)
    ).toRoutes

  private def baseServer =
    BlazeServerBuilder
    /** In Scala, execution context allows parallelism, which is important for an HTTP server */
      .apply[IO](ExecutionContext.global)
      .bindHttp(
        /** Get the port and bind hostname from the environment */
        port = sys.env
          .get("HTTP_PORT")
          .flatMap(_.toIntOption)
          .getOrElse(defaults.HttpPort),
        host = sys.env.getOrElse("HTTP_HOST",
                                 InetAddress.getLoopbackAddress.getHostAddress)
      )
      .withHttpApp((assetRoutes <+> httpRoutes).orNotFound)

  private def secureServer: Resource[IO, Server] =
    LetsEncryptScala
      .fromEnvironment[IO]
      .flatMap(_.sslContextResource[IO])
      .flatMap(sslContext => baseServer.withSslContext(sslContext).resource)

  override def run(args: List[String]): IO[ExitCode] = {

    /** To develop locally */
    if (args.contains("--insecure")) baseServer.resource else secureServer
  }.use(_ => IO.never).as(ExitCode.Success)
}
