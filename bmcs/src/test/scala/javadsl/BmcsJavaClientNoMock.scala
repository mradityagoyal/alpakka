/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package javadsl

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.oracle.bmcs.auth.{BasicCredentials, BmcsCredentials}
import akka.stream.alpakka.oracle.bmcs.{BmcsSettings, TestUtil}
import akka.stream.alpakka.oracle.bmcs.javadsl.BmcsClient
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContextExecutor

class BmcsJavaClientNoMock extends FlatSpecLike with BeforeAndAfterAll with Matchers with ScalaFutures {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = materializer.executionContext

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(30, Millis))

  val userOcid = "ocid1.user.oc1..aaaaaaaaalwxriuznfhohggk7ejii6lpwo7mebuldxh455hiesnowaoaksyq"
  val keyFingerprint = "cb:17:e0:45:d0:24:d3:ff:be:ee:1b:0e:f8:2c:58:27"
  val tenancyOcid = "ocid1.tenancy.oc1..aaaaaaaa6gtmn46bketftho3sqcgrlvdfsenqemqy3urkbthlpkos54a6wsa"
  val keyPath = "/ssd/scala_github/alpakka/oci_api_key.pem"
  val passphrase: Option[String] = Some("aditya")
  val cred = BasicCredentials(userOcid, tenancyOcid, keyPath, passphrase, keyFingerprint)
  val bucket = "CEGBU_Prime"

  val client = BmcsClient.create(cred, actorSystem, materializer)

  "Java client " should " upload " in {

    val src = TestUtil.largeSource(2)
    val objectName = s"BmcsNoMockTest-${UUID.randomUUID().toString.take(5)}"

    client.multipartUpload(bucket, objectName)

  }

}
