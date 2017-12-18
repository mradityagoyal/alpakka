/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.oracle.bmcs

import java.util.Optional

import akka.stream.alpakka.oracle.bmcs.impl.ObjectSummary

final case class ListObjectsResultContents(
    /** the name of the object in bmcs **/
    name: String,
    /** the name of the bucket in which this object is stored. **/
    bucket: String,
    /** the size of the object,  in bytes **/
    size: Option[Long],
    /** Base64 encoded MD5 hash of the object Data.  **/
    md5: Option[String],
    /** The date and time the object was created, as described in RFC 2616, section 14.29. **/
    timeCreated: Option[String]
)

object ListObjectsResultContents {
  def apply(obj: ObjectSummary, bucket: String): ListObjectsResultContents =
    ListObjectsResultContents(obj.name, bucket, obj.size, obj.md5, obj.timeCreated)

  def create(obj: ObjectSummary, bucket: String): ListObjectsResultContents =
    ListObjectsResultContents(obj.name, bucket, obj.size, obj.md5, obj.timeCreated)
}
