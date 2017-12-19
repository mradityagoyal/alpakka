/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.oracle.bmcs.scaladsl

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.oracle.bmcs.{BmcsSettings, ListObjectsResultContents, MemoryBufferType, TestUtil}
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Ignore, Matchers}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import akka.stream.alpakka.oracle.bmcs.auth._
import akka.util.ByteString

/*
 * This is an integration test and ignored by default
 *
 * For running the tests you need to create 1 bucket:
 * Update the bucket name and regions in the code below
 *
 * update the ocids and keys.
 *
 * Comment @ignore and run the tests
 *
 */
//@Ignore
class BmcsNoMock extends FlatSpecLike with BeforeAndAfterAll with Matchers with ScalaFutures {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = materializer.executionContext

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(30, Millis))

  val userOcid = "ocid1.user.oc1..aaaaaaaaalwxriuznfhohggk7ejii6lpwo7mebuldxh455hiesnowaoaksyq"
  val keyFingerprint = "2a:1a:ab:b8:0b:6a:7b:3a:e4:f1:24:2f:6c:3b:71:ed"
  val tenancyOcid = "ocid1.tenancy.oc1..aaaaaaaa6gtmn46bketftho3sqcgrlvdfsenqemqy3urkbthlpkos54a6wsa"
  val keyPath = "./bmcs/src/test/resources/keyNew.pem"
  val passphrase: Option[String] = None
  val cred = BasicCredentials(userOcid, tenancyOcid, keyPath, passphrase, keyFingerprint)
  val bucket = "CEGBU_Prime"

  val settings =
    BmcsSettings().copy(bufferType = MemoryBufferType, region = "us-phoenix-1", namespace = "oraclegbudev")
  val client = BmcsClient(cred, settings)
  val largeSrc = TestUtil.largeSource()

  "BmcsClient " should "upload a document " in {

    val objectName = s"AGOYAL-${UUID.randomUUID().toString.take(5)}"
    val sink = client.multipartUpload(bucket, objectName)

    val result = largeSrc.runWith(sink)
    val multipartUploadResult = Await.ready(result, 90.seconds).futureValue
    multipartUploadResult.etag shouldNot be("")

    //now download the same object.
    //compare the hashes of the upload and download.
    val download = client.download(bucket, objectName)

    val hashOfDownload: Future[ByteString] = download.runWith(digest())
    val hashOfUpload = largeSrc.runWith(digest())

    val uploadHash = Await.ready(hashOfUpload, 20.seconds).futureValue
    val downloadHash = Await.ready(hashOfDownload, 20.seconds).futureValue
    val maybeDelete = client.delete(bucket, "AGOYAL-41085")
    val delte = Await.ready(maybeDelete, 20.seconds).futureValue
    println("************* deleted. ")

    uploadHash should equal(downloadHash)
  }

  "it" should "list objects in the bucket correctly " in {
    val objects: Source[ListObjectsResultContents, NotUsed] = client.listBucket(bucket)
    val countFuture: Future[Int] = objects.map(_ => 1).runWith(Sink.fold(0)(_ + _))
    val count = Await.ready(countFuture, 20.seconds).futureValue
    count should be > 0
  }

}
