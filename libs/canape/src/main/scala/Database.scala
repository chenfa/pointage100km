package net.rfc1149.canape

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import org.jboss.netty.handler.codec.http._

case class Database(val couch: Couch, val database: String) {

  import implicits._

  private[canape] val uri = couch.uri + "/" + database

  override def toString = couch.toString + "/" + database

  override def hashCode = uri.hashCode

  override def canEqual(that: Any) = that.isInstanceOf[Database]

  override def equals(that: Any): Boolean = that match {
      case other: Database if other.canEqual(this) => uri == other.uri
      case _                                       => false
  }

  private[canape] def uriFrom(other: Couch) = if (couch == other) database else uri

  private def encode(extra: String, properties: Seq[(String, String)] = Seq()) = {
    val encoder = new QueryStringEncoder(database + "/" + extra)
    properties foreach { case (name, value) => encoder.addParam(name, value) }
    encoder.toString
  }

  def status(): CouchRequest[mapObject] = couch.makeGetRequest[mapObject](database)

  def apply(id: String): CouchRequest[Map[String, JValue]] =
    couch.makeGetRequest[Map[String, JValue]](encode(id))

  def apply(id: String, rev: String): CouchRequest[Map[String, JValue]] =
    couch.makeGetRequest[Map[String, JValue]](encode(id, Seq("rev" -> rev)))

  def apply(id: String, properties: Map[String, String]): CouchRequest[JValue] =
    apply(id, properties.toSeq)

  def apply(id: String, properties: Seq[(String, String)]): CouchRequest[JValue] =
    couch.makeGetRequest[JValue](encode(id, properties))

  def query(id: String, properties: Seq[(String, String)]): CouchRequest[Result] = {
    couch.makeGetRequest[Result](encode(id, properties))
  }

  def view(design: String, name: String, properties: Seq[(String, String)] = Seq()) =
    query("_design/" + design + "/_view/" + name, properties)

  def allDocs(): CouchRequest[Result] = allDocs(Map())

  def allDocs(params: Map[String, String]): CouchRequest[Result] =
    query("_all_docs", params.toSeq)

  def create(): CouchRequest[JValue] = couch.makePutRequest[JValue](database, "")

  def startCompaction(): CouchRequest[JValue] =
    couch.makePostRequest[JValue](database + "/_compact", "")

  def bulkDocs(docs: Seq[Any], allOrNothing: Boolean = false): CouchRequest[JValue] = {
    val args = Map("all_or_nothing" -> allOrNothing, "docs" -> docs)
    couch.makePostRequest[JValue](database + "/_bulk_docs", args)
  }

  def insert(doc: AnyRef, id: Option[String] = None): CouchRequest[JValue] = {
    val jsDoc = doc match {
	case js: JObject => js
	case _           => parse(write(doc)).extract[JObject]
    }
    id orElse (jsDoc \ "_id" match {
      case JString(docId) => Some(docId)
      case _              => None
    }) match {
      case Some(docId: String) => couch.makePutRequest[JValue](database + "/" + docId, jsDoc)
      case None                => couch.makePostRequest[JValue](database, jsDoc)
    }
  }

  def insert(id: String, doc: AnyRef): CouchRequest[JValue] =
    insert(doc, Some(id))

  def delete(id: String, rev: String): CouchRequest[JValue] =
    couch.makeDeleteRequest[JValue](database + "/" + id + "?rev=" + rev)

  def delete(): CouchRequest[JValue] = couch.makeDeleteRequest[JValue](database)

  def delete(doc: AnyRef): CouchRequest[JValue] = {
    val jsDoc = parse(write(doc)).extract[JObject]
    val JString(id) = jsDoc \ "_id"
    val JString(rev) = jsDoc \ "_rev"
    delete(id, rev)
  }

  def changes(params: Map[String, String] = Map()): CouchRequest[JValue] =
    couch.makeGetRequest[JValue](encode("_changes", params.toSeq))

}
