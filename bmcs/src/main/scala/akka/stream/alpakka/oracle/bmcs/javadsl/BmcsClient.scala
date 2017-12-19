/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.oracle.bmcs.javadsl

import java.util.Optional
import java.util.concurrent.CompletionStage

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.ByteRange
import akka.http.javadsl.model.HttpResponse
import akka.http.scaladsl.model.headers.{ByteRange => ScalaByteRange}
import akka.stream.Materializer
import akka.stream.alpakka.oracle.bmcs.{BmcsSettings, ListObjectsResultContents, MultipartUploadResult}
import akka.stream.alpakka.oracle.bmcs.auth.BmcsCredentials
import akka.stream.alpakka.oracle.bmcs.impl.BmcsStream
import akka.stream.javadsl.{Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

object BmcsClient {
  val MinChunkSize: Int = 5242880

  def create(credentials: BmcsCredentials, system: ActorSystem, mat: Materializer): BmcsClient =
    BmcsClient.create(BmcsSettings(ConfigFactory.load()), credentials, system, mat)

  def create(settings: BmcsSettings, cred: BmcsCredentials, system: ActorSystem, mat: Materializer): BmcsClient =
    new BmcsClient(settings, cred, system, mat)
}

final class BmcsClient(val settings: BmcsSettings, val cred: BmcsCredentials, system: ActorSystem, mat: Materializer) {

  private[this] val impl = BmcsStream(settings, cred)(system, mat)

  def request(bucket: String, objectName: String): CompletionStage[HttpResponse] =
    impl.request(bucket, objectName).map(_.asInstanceOf[HttpResponse])(system.dispatcher).toJava

  def download(bucket: String, objectName: String): Source[ByteString, NotUsed] =
    impl.download(bucket, objectName).asJava

  def download(bucket: String, objectName: String, range: ByteRange): Source[ByteString, NotUsed] = {
    val scalaRange = range.asInstanceOf[ScalaByteRange]
    impl.download(bucket, objectName, Some(scalaRange)).asJava
  }

  def listBucket(bucket: String): Source[ListObjectsResultContents, NotUsed] = listBucket(bucket, Optional.empty())

  /**
   * Will return a source of object metadata for a given bucket with optional prefix.
   * This will automatically page through all keys with the given parameters.
   *
   * @param prefix Prefix of the keys you want to list under passed bucket
   * @return Source of object metadata
   */
  def listBucket(bucket: String, prefix: Optional[String]): Source[ListObjectsResultContents, NotUsed] =
    impl
      .listBucket(bucket, prefix = if (prefix.isPresent) Some(prefix.get) else None)
      .map { scalaContents =>
        ListObjectsResultContents(scalaContents.name,
                                  scalaContents.bucket,
                                  scalaContents.size,
                                  scalaContents.md5,
                                  scalaContents.timeCreated)
      }
      .asJava

  def multipartUpload(
      bucket: String,
      objectName: String
  ): Sink[ByteString, CompletionStage[MultipartUploadResult]] =
    multipartUpload(bucket, objectName, BmcsClient.MinChunkSize, 4)

  def multipartUpload(bucket: String,
                      objectName: String,
                      chunkSize: Int,
                      chunkingParallelism: Int): Sink[ByteString, CompletionStage[MultipartUploadResult]] =
    impl
      .multipartUpload(bucket, objectName, chunkSize, chunkingParallelism)
      .mapMaterializedValue(_.map(MultipartUploadResult.create)(system.dispatcher).toJava)
      .asJava

  def delete(bucket: String, objectName: String): CompletionStage[String] =
    impl.delete(bucket, objectName).toJava
}
