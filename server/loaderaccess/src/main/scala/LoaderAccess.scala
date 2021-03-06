import akka.actor.ActorSystem
import com.healthmarketscience.jackcess
import java.io.File
import net.liftweb.json._
import net.rfc1149.canape._
import scala.collection.JavaConversions._
import scopt.OptionParser

// Usage: loaderaccess dbfile

object LoaderAccess extends App {

  implicit val formats = DefaultFormats

  val system = ActorSystem()
  implicit val dispatcher = system.dispatcher

  private object Options {
    var file: File = _
  }

  private val parser = new OptionParser("loader") {
    help("h", "help", "show this help")
    arg("database", "MS access database to import", { s: String => Options.file = new File(s) })
  }

  if (!parser.parse(args))
    sys.exit(1)

  val table = jackcess.Database.open(Options.file, true).getTable("inscription")

  val db = new NioCouch(auth = Some("admin", "admin")).db("steenwerck100km")

  val format = new java.text.SimpleDateFormat("yyyy/MM/dd")

  def get(id: String) = try { Some(db(id).execute()) } catch { case StatusCode(404, _) => None }

  def capitalize(name: String) = {
    val capitalized = "[ -]".r.split(name).map(_.toLowerCase.capitalize).mkString(" ")
    capitalized.zip(name) map { _ match {
      case (_, '-') => '-'
      case (c, _)   => c
    } } mkString
  }

  def fix(contestant: Map[String, AnyRef]) =
    contestant map {
      case ("nom", v: String) => "nom" -> capitalize(v)
      case ("prenom", v: String) => "prenom" -> capitalize(v)
      case (k, v: java.util.Date) => k -> (if (v == null) null else format.format(v))
      case (k, v: java.math.BigDecimal) => k -> v.doubleValue()
      case (k, v) => k -> v
    }

  for (row <- table) {
    val id = "contestant-" + row("dossard")
    val doc = fix(row.toMap + ("_id" -> id) + ("type" -> "contestant"))
    val desc = "bib %d (%s %s)".format(doc("dossard"), doc("prenom"), doc("nom"))
    try {
      db.insert(util.toJObject(doc)).execute()
      println("Inserted " + desc)
    } catch {
	case StatusCode(409, _) =>
	  println("Updating existing " + desc)
	  db.insert(util.toJObject(doc + ("_rev" -> get(id).map(_("_rev"))))).execute()
    }
  }

  db.couch.releaseExternalResources()
  system.shutdown()
}
