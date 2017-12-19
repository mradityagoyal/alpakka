/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.oracle.bmcs.scaladsl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.ByteRange
import akka.stream.Materializer
import akka.stream.alpakka.oracle.bmcs.{BmcsSettings, ListObjectsResultContents, MultipartUploadResult}
import akka.stream.alpakka.oracle.bmcs.auth.BmcsCredentials
import akka.stream.alpakka.oracle.bmcs.impl._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object BmcsClient {
  val MinChunkSize: Int = 5242880

  def apply(cred: BmcsCredentials)(implicit system: ActorSystem, mat: Materializer): BmcsClient =
    new BmcsClient(BmcsSettings(ConfigFactory.load()), cred)

  def apply(cred: BmcsCredentials, settings: BmcsSettings)(implicit system: ActorSystem,
                                                           mat: Materializer): BmcsClient =
    new BmcsClient(settings, cred)

}

final class BmcsClient(val settings: BmcsSettings, val cred: BmcsCredentials)(implicit system: ActorSystem,
                                                                              mat: Materializer) {

  private[this] val impl = BmcsStream(settings, cred)

  def request(bucket: String, objectName: String): Future[HttpResponse] =
    impl.request(bucket, objectName)

  def download(bucket: String, objectName: String): Source[ByteString, NotUsed] =
    impl.download(bucket, objectName)

  def download(bucket: String, objectName: String, range: ByteRange): Source[ByteString, NotUsed] =
    impl.download(bucket, objectName, Some(range))

  /**
   * Will return a source of object metadata for a given bucket with optional prefix.
   * This will automatically page through all keys with the given parameters.
   *
   * @param prefix Prefix of the keys you want to list under passed bucket
   * @return Source of object metadata
   */
  def listBucket(bucket: String, prefix: Option[String] = None): Source[ListObjectsResultContents, NotUsed] =
    impl.listBucket(bucket, prefix = prefix)

  def multipartUpload(bucket: String,
                      objectName: String,
                      chunkSize: Int = BmcsClient.MinChunkSize,
                      chunkingParallelism: Int = 4): Sink[ByteString, Future[MultipartUploadResult]] =
    impl
      .multipartUpload(bucket, objectName, chunkSize, chunkingParallelism)
      .mapMaterializedValue(_.map(MultipartUploadResult.apply)(system.dispatcher))

  def delete(bucket: String, objectName: String): Future[String] = {
    impl.delete(bucket, objectName)
  }
}
