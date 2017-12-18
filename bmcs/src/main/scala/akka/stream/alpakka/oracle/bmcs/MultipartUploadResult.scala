/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.oracle.bmcs

import akka.stream.alpakka.oracle.bmcs.impl.CompleteMultipartUploadResult

final case class MultipartUploadResult(bucketName: String, objectName: String, etag: String)

object MultipartUploadResult {
  def create(r: CompleteMultipartUploadResult): MultipartUploadResult =
    new MultipartUploadResult(r.bucket, r.objectName, r.etag)

  def apply(r: CompleteMultipartUploadResult): MultipartUploadResult =
    new MultipartUploadResult(r.bucket, r.objectName, r.etag)
}