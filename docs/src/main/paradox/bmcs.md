# Oracle BMCS Connector

The Oracle BMCS connector provides Akka Stream sources and sinks to connect to [Oracle BMCS](https://cloud.oracle.com/en_US/infrastructure/storage).
BMCS stands for Bare Metal Cloud Storage and is an object storage service with a web service interface. 
BMCS is also referenced as Oracle Cloud Infrastructure - Storage

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=com.lightbend.akka
  artifact=akka-stream-alpakka-s3_$scalaBinaryVersion$
  version=$version$
}

## Usage

### Set up your BMCS clients

The BMCS connector can be configured within your `application.conf` file.

Configuration
: @@snip (../../../../bmcs/src/main/resources/reference.conf)

### Create an S3 client

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/S3SourceSpec.scala) { #client }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/S3ClientTest.java) { #client }

### Storing a file in S3

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/S3SinkSpec.scala) { #upload }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/S3ClientTest.java) { #upload }

### Downloading a file from S3

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/S3SourceSpec.scala) { #download }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/S3ClientTest.java) { #download }

In order to download a range of a file's data you can use overloaded method which
additionally takes `ByteRange` as argument.

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/S3SourceSpec.scala) { #rangedDownload }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/S3ClientTest.java) { #rangedDownload }

### List bucket contents

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/S3SourceSpec.scala) { #list-bucket }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/S3ClientTest.java) { #list-bucket }


### Running the example code

The code in this guide is part of runnable tests of this project. You are welcome to edit the code and run it in sbt.

Scala
:   ```
    sbt
    > s3/test
    ```

Java
:   ```
    sbt
    > s3/test
    ```

# Bluemix Cloud Object Storage with S3 API

The Alpakka S3 connector can connect to a range of S3 compatible services. One of them is IBM Bluemix Cloud Object Storage, which supports a dialect of the AWS S3 API.
Most functionality provided by the Alpakka S3 connector is compatible with Cloud Object Store, but there are a few limitations, which are listed below.

## Connection limitations

- This S3 connector does not support domain-style access for Cloud Object Store, so only path-style access is supported.
- Regions in COS are always part of the host/endpoint, therefore leave the s3Region field in S3Settings empty
- The object proxy, containing host/endpoint, port and scheme, must always be specified.

## External references

[IBM Cloud Object Storage Documentation](https://ibm-public-cos.github.io/crs-docs/api-reference)

## Example

Scala
: @@snip (../../../../s3/src/test/scala/akka/stream/alpakka/s3/scaladsl/DocSnippets.scala) { #scala-bluemix-example }

Java
: @@snip (../../../../s3/src/test/java/akka/stream/alpakka/s3/javadsl/DocSnippets.java) { #java-bluemix-example }
