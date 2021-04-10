package com.scalawilliam.letsencrypt.play

import cats.effect.IO
import com.scalawilliam.letsencrypt.LetsEncryptScala
import play.api.inject.ApplicationLifecycle

import javax.inject.Inject
import javax.net.ssl._

import play.server.api._

final class LetsEncryptPlay @Inject()(
    applicationLifecycle: ApplicationLifecycle)
    extends SSLEngineProvider {

  override def createSSLEngine(): SSLEngine = sslContext().createSSLEngine

  override def sslContext(): SSLContext =
    LetsEncryptScala
      .fromEnvironment[IO]
      .flatMap(_.sslContextResource[IO])
      .allocated
      .unsafeRunSync() match {
      case (sslContext, close) =>
        applicationLifecycle.addStopHook(() => close.unsafeToFuture())
        sslContext
    }

}
