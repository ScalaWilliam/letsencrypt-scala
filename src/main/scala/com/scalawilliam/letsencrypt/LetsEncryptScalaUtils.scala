package com.scalawilliam.letsencrypt

import cats.effect.{Resource, Sync}
import org.bouncycastle.util.io.pem.PemReader

import java.io.{FileReader, Reader}
import java.nio.file.Path

private[letsencrypt] object LetsEncryptScalaUtils {

  def loadChain[F[_]: Sync](filePath: Path): Resource[F, List[Array[Byte]]] =
    clearableListByteArray {
      Sync[F].delay {
        extractDER(new FileReader(filePath.toFile))
      }
    }

  def extractDER(reader: => Reader): List[Array[Byte]] = {
    val readerInstance = reader
    try {
      val pemReader = new PemReader(readerInstance)
      try Iterator
        .continually(Option(pemReader.readPemObject()))
        .takeWhile(_.isDefined)
        .flatten
        .flatMap(o => Option(o.getContent))
        .toList
      finally pemReader.close()
    } finally readerInstance.close()
  }

  def clearableCharArray[F[_]: Sync](
      f: F[Array[Char]]): Resource[F, Array[Char]] =
    Resource.make(f)(array =>
      Sync[F].delay {
        java.util.Arrays.fill(array, '0')
    })

  def clearableByteArray[F[_]: Sync](
      f: F[Array[Byte]]): Resource[F, Array[Byte]] =
    Resource.make(f)(array =>
      Sync[F].delay {
        java.util.Arrays.fill(array, 0.toByte)
    })

  def clearableListByteArray[F[_]: Sync](
      f: F[List[Array[Byte]]]): Resource[F, List[Array[Byte]]] =
    Resource.make(f)(list =>
      Sync[F].delay {
        list.foreach(array => java.util.Arrays.fill(array, 0.toByte))
    })

  val PrintableRange: Range.Inclusive = 0x20 to 0x7E

  def randomPassword[F[_]: Sync]: Resource[F, Array[Char]] =
    Resource.eval(Sync[F].delay(16 + scala.util.Random.nextInt(20))).flatMap {
      length =>
        clearableCharArray[F] {
          Sync[F].delay {
            Array.fill(length) {
              PrintableRange(
                scala.util.Random.nextInt(PrintableRange.length - 1)).toChar
            }
          }
        }
    }
}
