package be.jvb.actorpmt


import org.joda.time.{DateTime}
import actors.Actor

trait MetricProvider {
  val name: String

  val providedMetricDefinitions: List[MetricDefinition]

  // TODO: volatile?
  private var dependants: List[Actor] = Nil // TODO: rename to listeners? and shouldn't I check that it's an actor of the correct type?

  private val mfs = new MetricFileSystem

  def start: Any

  def provideMetrics(time: DateTime, providedMetrics: Metrics) = {
    println("provider [" + name + "] provides metrics at [" + time + "] for interval [" + providedMetrics.interval + "]")

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

  def registerDependant(dependant: Actor) = {
    dependants = dependant :: dependants
  }
}
