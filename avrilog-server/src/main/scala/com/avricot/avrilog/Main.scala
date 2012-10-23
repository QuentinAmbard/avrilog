
package com.avricot.avrilog

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.Terminated
import akka.util.duration._
import akka.actor.ActorLogging
import akka.pattern.ask
import akka.util.Timeout
import org.msgpack.AvrilogMPack
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import com.avricot.avrilog.model.Trace

object Main {
  def main(args: Array[String]) = {
    println("ok")
    val test = new TraceConsumer()
    println("consumer")
  }
}

