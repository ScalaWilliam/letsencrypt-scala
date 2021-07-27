/*
 * Copyright 2021 ScalaWilliam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scalawilliam.letsencrypt

import cats.effect.{Async, Resource, Sync}
import cats.implicits._
import com.scalawilliam.letsencrypt.LetsEncryptScala.{
  CertificateAliasPrefix,
  PrivateKeyAlias
}
import com.scalawilliam.letsencrypt.LetsEncryptScalaUtils._

import java.io.ByteArrayInputStream
import java.nio.file.{Path, Paths}
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyException, KeyFactory, KeyStore, PrivateKey}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

/**
  *
  * All the certificates are picked up from the filesystem. You have 3 ways to specify the Let's Encrypt directory to use:
  * - Via an environment variable 'LETSENCRYPT_CERT_DIR'
  * - Via a System property 'letsencrypt.cert.dir',
  * - Programmatically with a [[java.nio.file.Path]] - we leave this in case it is needed, for example, in corporate environments,
  * where you may be fetching stuff based on a configuration option you fetch from elsewhere.
  *
  * All the file paths are normalised before reading, and then are read with BouncyCastle's PemReader.
  *
  * Once the certificates are successfully fetched, you can:
  * - Get a standard [[javax.net.ssl.SSLContext]], so that you can use it in combination with libraries that are not FS2-TLS based.
  *   For example, http4s-blaze will use SSLContext, but http4s-ember will use TLSContext.
  *
  * All the certificates are supplied as [[cats.effect.Resource]] types, so that all the clean-ups are taken care of for you.
  * This also includes clearing out Arrays that should not retain passwords, private keys, and so forth, once they are no longer needed.
  *
  * This being a convenience library, these will be 2 most common use cases:
  *
  *  @example {{{
  * LetsEncryptScala
  *   .fromEnvironment[IO]
  *   .flatMap(sslContext => /* Use with fs2/http4s/other servers */ )
  * }}}
  *
  */
object LetsEncryptScala {

  /**
    * Load the certificate configuration from a directory specified.
    */
  def fromLetsEncryptDirectory[F[_]: Sync](
      certificateDirectory: Path): Resource[F, LetsEncryptScala] =
    (loadCertificateChain(certificateDirectory),
     loadPrivateKey(certificateDirectory))
      .mapN(new LetsEncryptScala(_, _))

  /** Load the certificate configuration from a directory specified as
    * an environment variable or as a system property.
    *
    * The system property is prioritised over the environment variable
    * because it is more specific.
    */
  def fromEnvironment[F[_]: Sync]: Resource[F, LetsEncryptScala] =
    Resource
      .eval {
        Sync[F]
          .delay {
            Paths
              .get(
                sys.props
                  .get(DefaultSysPropertyName)
                  .orElse(sys.env
                    .get(DefaultEnvVarName))
                  .getOrElse(
                    sys.error(
                      s"Expected environment variable '$DefaultEnvVarName' or system property '$DefaultSysPropertyName'"
                    )
                  )
              )
              .toAbsolutePath
          }
      }
      .flatMap(path => fromLetsEncryptDirectory(path))

  /**
    * Default environment variable name to use by [[fromEnvironment]].
    */
  val DefaultEnvVarName = "LETSENCRYPT_CERT_DIR"

  /**
    * Default Java system property name to use by [[fromEnvironment]].
    */
  val DefaultSysPropertyName = "letsencrypt.cert.dir"

  private[letsencrypt] val PrivateKeyAlias        = "PrivateKeyAlias"
  private[letsencrypt] val CertificateAliasPrefix = "CertificateAlias"

  /**
    * The filename of the 'full chain' file in the LetsEncrypt certificate directory.
    * This normally contains the root certificate all the way through intermediate certificates
    * and to our own certificate. Let's Encrypt only has 2, but no guarantee that it will
    * continue the case.
    */
  val LetsEncryptFullChainPemFilename = "fullchain.pem"

  /**
    * The filename of the 'private key' file in the LetsEncrypt certificate directory.
    *
    * Try to restrict access to this as much as you possibly can because this
    * you don't want to get in the wrong hands.
    *
    * When setting up on Linux environments, there is a good chance that
    * you would need to share this file with your Java application,
    * and the easiest way to give that access is to use a command `setfacl -m u:javaappuser:r /path/to/privkey.pem`,
    * so that you can give access to ''javaappuser'' for this file specifically.
    */
  val LetsEncryptPrivateKeyPemFilename = "privkey.pem"

  private def loadPrivateKey[F[_]: Sync](
      certificateDirectory: Path): Resource[F, Array[Byte]] = {
    val privateKeyPath =
      certificateDirectory
        .resolve(LetsEncryptPrivateKeyPemFilename)
        .toAbsolutePath
    loadChain(privateKeyPath)
      .map(
        _.headOption.getOrElse(
          throw new KeyException(
            s"Could not extract a private key from ${privateKeyPath}"
          )
        ))
  }

  private def loadCertificateChain[F[_]: Sync](
      certificateDirectory: Path): Resource[F, List[Array[Byte]]] = {
    val chainPath =
      certificateDirectory
        .resolve(LetsEncryptFullChainPemFilename)
        .toAbsolutePath
    loadChain(chainPath).map(
      _.ensuring(_.nonEmpty,
                 s"Could not extract a single certificate from $chainPath"))
  }
}

final class LetsEncryptScala(certificateChain: List[Array[Byte]],
                             privateKeyBytes: Array[Byte]) {

  private[letsencrypt] def addToKeyStore[F[_]: Sync](
      keyStore: KeyStore,
      withPassword: Array[Char]): F[Unit] =
    Sync[F].delay {
      val certificates = certificateChain.map { bytes =>
        CertificateFactory
          .getInstance("X.509")
          .generateCertificate(new ByteArrayInputStream(bytes))
      }
      certificates.zipWithIndex.foreach {
        case (certificate, index) =>
          keyStore.setCertificateEntry(s"$CertificateAliasPrefix$index",
                                       certificate)
      }
      keyStore.setKeyEntry(
        PrivateKeyAlias,
        makePrivateKey(),
        withPassword,
        certificates.toArray
      )
    }

  private[letsencrypt] def makePrivateKey(): PrivateKey =
    KeyFactory
      .getInstance("RSA")
      .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))

  def sslContextResource[F[_]: Sync]: Resource[F, SSLContext] =
    for {
      internalPassword <- randomPassword[F]
      keyStore <- Resource.eval {
        Sync[F].delay {
          val keyStore = KeyStore.getInstance("PKCS12")
          keyStore.load(null)
          keyStore
        }
      }
      _ <- Resource.eval(addToKeyStore(keyStore, internalPassword))
      sslContext <- Resource.eval {
        Sync[F].delay {
          val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
          keyManagerFactory.init(keyStore, internalPassword)
          val sslContext = SSLContext.getInstance("TLS")
          sslContext.init(keyManagerFactory.getKeyManagers, null, null)
          sslContext
        }
      }
    } yield sslContext

}
