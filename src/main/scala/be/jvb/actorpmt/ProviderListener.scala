package be.jvb.actorpmt

import actors.Actor
import actors.Actor._
/**
 * Reacts to metric available messages.
 *
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
trait ProviderListener extends Actor {
  def act() {
    loop {
      react {
        case m: MetricAvailableMessage => messageReceived(m)
        case msg => println("received unknown message")
      }
    }
  }

  def messageReceived(message: MetricAvailableMessage) = {
    println("received " + message + " in " + self)
//    // log
//    println("received in thread [" + Thread.currentThread + "]")
//    // pretend to do some work
//    BigInteger.valueOf(10000L).isProbablePrime(1)
    processMetricAvailableMessage(message)
  }

  def processMetricAvailableMessage(message: MetricAvailableMessage)

}