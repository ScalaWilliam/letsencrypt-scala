package com.scalawilliam

import cats.effect.kernel.{Async, Sync}
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.Port
import com.scalawilliam.letsencrypt.LetsEncryptScala
import fs2.io.net.tls.TLSContext
import fs2.io.net.{Network, Socket}
import fs2.{INothing, Stream, text}
import cats.effect.Concurrent

object FS2LetsEncryptEchoApp extends IOApp {

  type SocketHandler[F[_]] = Socket[F] => Stream[F, INothing]

  /** Respond to a client socket with echoes - if they send a line, we return a line */
  private def handleEchoes[F[_]: Concurrent]: SocketHandler[F] =
    client =>
      client.reads
        .through(text.utf8Decode)
        .through(text.lines)
        .interleave(Stream.constant("\n"))
        .through(text.utf8Encode)
        .through(client.writes)
        /** This is VERY important; if you don't handle errors, the whole server will go down;
          * One example is if a plaintext client connects to an SSL server */
        .handleErrorWith(_ => Stream.empty)

  private def runTcpServer[F[_]: Concurrent: Network](
      forHandler: SocketHandler[F]
  ): F[Unit] =
    Network[F]
      .server(port = Port.fromInt(5555))
      .map(forHandler)
      .parJoin(100)
      .compile
      .drain

  /** Utility function to pipe a socket through SSL.
    * This is a prime example of composition in functional programming:
    * you merely 'secure' a socket handler, without having to
    * modify any of the plaintext server code.
    *
    * We have to use a 'Resource' here because the SSL Context
    * is something we should let go of once not needed any more.
    * */
  private def secureHandler[F[_]: Concurrent: Sync: Async](
      originalHandler: SocketHandler[F]
  ): Resource[F, SocketHandler[F]] =
    LetsEncryptScala
      .fromEnvironment[F]
      .flatMap(_.sslContextResource)
      .map(TLSContext.Builder.forAsync[F].fromSSLContext)
      .map { tlsContext => (clientSocket: Socket[F]) =>
        fs2.Stream
          .resource(tlsContext.server(clientSocket))
          .flatMap(originalHandler)
      }

  private def secureConditionally[F[_]: Concurrent: Sync: Async](
      enableSecurity: Boolean,
      handler: SocketHandler[F]
  ): Resource[F, SocketHandler[F]] =
    if (enableSecurity) secureHandler(handler)
    else Resource.pure(handler)

  override def run(args: List[String]): IO[ExitCode] =
    secureConditionally[IO](
      enableSecurity = !args.contains("--insecure"),
      handler = handleEchoes
    ).use(handler => runTcpServer(handler).as(ExitCode.Success))

}
