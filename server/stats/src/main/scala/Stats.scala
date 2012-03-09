import akka.actor.ActorSystem
import net.liftweb.json._
import net.rfc1149.canape._
import net.rfc1149.canape.implicits._
import scala.util.Random.nextInt

object Stats extends App {

  private implicit val system = ActorSystem()

  private implicit val formats = DefaultFormats

  val db: Database = new NioCouch().db("steenwerck100km")

  def update(checkpoint: Int ,bib: Int, race: Int) {
    val id = "checkpoint-" + checkpoint + "-" + bib
    val r = db.update("bib_input", "add-checkpoint", id,
		      Map("ts" -> System.currentTimeMillis.toString)).execute()
    if (r \ "need_more" == JBool(true)) {
      val d = db(id).execute() + ("race_id" -> race) + ("bib" -> bib) + ("site_id" -> checkpoint)
      db.insert(d).execute()
    }
  }

  def recentCheckpointsMillis() = {
    val before = System.currentTimeMillis
    db.view("bib_input", "recent-checkpoints").execute()
    System.currentTimeMillis - before
  }

  try {
    for (i <- 1 to args(0).toInt) {
      val checkpoint = nextInt(3)
      val bib = nextInt(1000)
      val race = nextInt(5)
      update(checkpoint, bib, race)
      println(i + " " + recentCheckpointsMillis)
    }
  } finally {
    db.couch.releaseExternalResources()
    system.shutdown()
  }

}