package be.jvb.actorpmt


import org.joda.time.{DateTime}
import actors.Actor

trait MetricProvider {
  val metricDefinition: MetricDefinition

  var dependants: List[Actor] = Nil // TODO: rename to listeners? and shouldn't I check that it's an actor of the correc type?

  val mfs = new MetricFileSystem

  def provideMetrics(time: DateTime, providedMetrics: Metrics) = {
    println("provider [" + metricDefinition.name + "] provides metrics at [" + time + "] for interval [" + providedMetrics.interval + "]")

    saveMetrics(providedMetrics)
    notifyDependants(time, providedMetrics)
  }

  private def saveMetrics(metricsToSave: Metrics) = {
    mfs.writeMetrics(metricsToSave)
  }

  private def notifyDependants(time: DateTime, providedMetrics: Metrics) = {
    // notify all dependants of this new metric by sending them a message
    for (dependant <- dependants) {
      dependant ! MetricAvailableMessage(providedMetrics, time)
    }
  }

  def registerDependant(dependant : Actor) = {
    dependants = dependant :: dependants
  }
}
