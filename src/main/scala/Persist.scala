package main.scala

import akka.stm._
import akka.actor._
import akka.actor.Actor._
import scala.util.Random

object testSTM extends App {

	val bibs = TransactionalMap[Int, String]
	val random = Random

	println("Adding stuff")

	atomic {
		bibs += 42 -> "test"
	}


	for (i <- 1 to 10) {
		spawn {
			Thread.sleep(random.nextInt(2000))
			atomic {
				println("Added stuff")
				bibs += i -> ("salut" + i)
			}
		}
	}

	Thread.sleep(5000)
	println("We have :")
	for ((key, value) <- bibs)
		println(key + " ---> " + value);
}

// vim: set ts=4 sw=4 et:
