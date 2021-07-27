package com.scalawilliam.letsencrypt.play

import cats.effect.IO
import com.scalawilliam.letsencrypt.LetsEncryptScala
import play.api.inject.ApplicationLifecycle
import play.core.ApplicationProvider
import cats.effect.unsafe.implicits.global

import javax.inject.Inject
import javax.net.ssl._
import play.server.api._

final class LetsEncryptPlay @Inject()(applicationProvider: ApplicationProvider)
    extends SSLEngineProvider {

  override def createSSLEngine(): SSLEngine = sslContext().createSSLEngine

  override def sslContext(): SSLContext =
    LetsEncryptScala
      .fromEnvironment[IO]
      .flatMap(_.sslContextResource[IO])
      .allocated
      .unsafeRunSync() match {
      case (sslContext, close) =>
        applicationProvider.get.get.injector
          .instanceOf[ApplicationLifecycle]
          .addStopHook(() => close.unsafeToFuture())
        sslContext
    }

}
