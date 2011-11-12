package fr.centkmsteenwerck

import akka.routing.Routing.Broadcast
import akka.dispatch.{CompletableFuture, Future}
import scala.io.Source
import scala.io.Source._
import akka.routing.{ CyclicIterator, Routing }
import akka.actor.{ Actor, ActorRef, PoisonPill }
import Actor._

/**
 * For now we just read from stdin some numbers
 * We send the input string to a new worker that will:
 *   - check that the string is a number
 *   - write the string to a text file
 *   - try to send the message to a server
 */
object Pointage extends App {
  println("===== Bienvenue dans l'application de pointage =====\n")
  println("Veuillez entrer les numéros de dossard l'un après l'autre")

  val master = actorOf(new Master())

  println("Starting the master and the monitor....")
  master.start()

  stdin.getLines().foreach {
    line => line match {
      case "EOF" =>
        println ("Will exit")
        master.stop()
        System.exit(0)
      case the_line =>
        println ("Have read : " + the_line)
        val input = new NewInput(the_line)
        master ! input
        println ("and sent it to master.")
      }
  }


  abstract class Message
  case class NewInput(input: String) extends Message // From main application to worker
  case class NewBib(bib: Integer) extends Message // From main application to worker
  case class Problem(pb: String) extends Message // From worker to monitorStdin

  class Master() extends Actor {
    var workers : List[ActorRef] = Nil // list of all running worker
    //We create the load balancer with a round round robin strategy
    //val loadBalancer = Routing.loadBalancerActor(CyclicIterator(workers))

    def receive = {
      case NewInput(input) =>
        println("Master received a new input: " + input)
        // check that input is an Integer
        // print in text file
        // send message to worker
      case Problem(pb) =>
        println("Worker had a problem: " + pb)
      case _ =>
        println("Master received a message")
    }

    override def preStart() = {
      println("=> preStart() of the Master")
    }

    override def postStop() = {
      println("=> postStop() of the Master")
    }
  }

  object Work {
    var taskCount = 0
  }

  /**
   * A worker just send an AMQP message to the server.
   */
  class Worker extends Actor {
    def receive = {
      case NewBib(bib) =>
        println("Worker doing hard work on bib " + bib)
        println("Here we should send a message to the server with AMQP")
        println("Sleeping for a moment instead :)")
        Thread.sleep(5000)
    }

    override def preStart() = {
      println("=> preStart() of the Worker")
    }

    override def postStop() = {
      println("=> postStop() of the Worker")
    }
  }
}
