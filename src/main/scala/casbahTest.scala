package main.scala

import com.mongodb.casbah.Imports._

//Must have mongo db installed on localhost
//sudo apt-get install mongodb
object testcasbah extends App {

val mongoColl = MongoConnection()("casbah_test")("test_data")

val bib1 = MongoDBObject("bib" -> 123,
                          "lap" -> 0   ,
                          "site" -> "croix_du_bac",
                          "time" -> "0h48",
                          "timestamp" -> 0)

val bib2 = MongoDBObject("bib" -> 123,
                          "lap" -> 0   ,
                          "site" -> "blanch",
                          "time" -> "1h23",
                          "timestamp" -> 0)

val bib3 = MongoDBObject("bib" -> 123,
                          "lap" -> 0   ,
                          "site" -> "croix_du_bac",
                          "time" -> "0h51",
                          "timestamp" -> 1)
mongoColl += bib1
mongoColl += bib2
mongoColl += bib3

def getMostRecent(q: MongoDBObject): MongoDBObject = {
	var item: MongoDBObject = null;
	var ts = -1
	for ( x <- mongoColl.find(q) ) {
		val new_ts = x.as[Int]("timestamp")
		if (new_ts > ts) {
			ts = new_ts
			item = x
		}
	}
	return item
}

val q = MongoDBObject("bib" -> 123,
		       "lap" -> 0 ,
                       "site" -> "croix_du_bac")

println(getMostRecent(q))

mongoColl.drop()

}

// vim: set ts=4 sw=4 et:
