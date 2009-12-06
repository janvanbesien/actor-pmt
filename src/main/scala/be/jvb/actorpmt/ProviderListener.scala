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
        case m: MetricAvailableMessage => processMetricAvailableMessage(m)
        case msg => println("received unknown message")
      }
    }
  }

  def processMetricAvailableMessage(message: MetricAvailableMessage)

}