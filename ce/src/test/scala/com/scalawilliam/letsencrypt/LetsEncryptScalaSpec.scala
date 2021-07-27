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

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.scalawilliam.letsencrypt.LetsEncryptScala.DefaultEnvVarName
import com.scalawilliam.letsencrypt.LetsEncryptScalaUtils.{
  extractDER,
  randomPassword
}
import org.scalatest.freespec.AnyFreeSpec

import java.io.StringReader
import java.security.KeyStore
import java.util.Base64

final class LetsEncryptScalaSpec extends AnyFreeSpec {

  private def testKeyStoreName: String = "KeyStore is populated correctly"

  if (sys.env.contains(DefaultEnvVarName)) {
    testKeyStoreName in {
      val pass     = "test".toCharArray
      val keyStore = KeyStore.getInstance("PKCS12")
      keyStore.load(null)
      LetsEncryptScala
        .fromEnvironment[IO]
        .use(ls =>
          ls.addToKeyStore[IO](keyStore, pass).map { _ =>
            assert(keyStore.containsAlias(
              s"${LetsEncryptScala.CertificateAliasPrefix}0"))
            assert(keyStore.containsAlias(
              s"${LetsEncryptScala.CertificateAliasPrefix}1"))
            assert(keyStore.containsAlias(LetsEncryptScala.PrivateKeyAlias))
        })
        .unsafeRunSync()
    }
  } else {
    testKeyStoreName ignore {}
  }

  "Bad DER cannot be parsed" in {
    assert(extractDER(new StringReader("ABC")).isEmpty)
  }

  "Realer DER can be parsed" in {
    val strA                    = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val encodedStr              = new String(Base64.getEncoder.encode(strA.getBytes()))
    val (firstPart, secondPart) = encodedStr.splitAt(10)
    val inputLines = List(
      "-----BEGIN C-----",
      s" $firstPart   ",
      s" $secondPart",
      "-----END C-----",
    ).mkString("\n")

    val DER     = extractDER(new StringReader(inputLines))
    val DER_STR = DER.map(ab => new String(ab))

    assert(DER_STR.contains(s"$strA"))
  }

  "Multiple DER can be parsed" in {
    val strA                    = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val strB                    = "AXBCDEFGEHIJKELMNOEPQRSETUVWXYZ"
    val encodedStr              = new String(Base64.getEncoder.encode(strA.getBytes()))
    val encodedStr2             = new String(Base64.getEncoder.encode(strB.getBytes()))
    val (firstPart, secondPart) = encodedStr.splitAt(10)
    val (thirdPart, fourthPath) = encodedStr2.splitAt(10)
    val inputLines = List(
      "-----BEGIN C-----",
      s" $firstPart   ",
      s" $secondPart",
      "-----END C-----",
      "-----BEGIN D-----",
      s" $thirdPart   ",
      s" $fourthPath",
      "-----END D-----",
    ).mkString("\n")

    val DER     = extractDER(new StringReader(inputLines))
    val DER_STR = DER.map(ab => new String(ab))

    assert(DER_STR.contains(s"$strA") && DER_STR.contains(s"$strB"))
  }

  "A random password can be generated and is cleared afterwards" in {
    val resultingArrays = List
      .fill(100)(randomPassword[IO])
      .sequence
      .use(lac => IO.pure(lac))
      .unsafeRunSync()
    assert(resultingArrays.flatten.toSet == Set('0'))
  }

}
