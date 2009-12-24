package be.jvb.actorpmt


import org.joda.time.{DateTime}
import actors.Actor
import collection._

trait MetricProvider {
  val name: String

  val providedMetricDefinitions: List[MetricDefinition]

  private val dependantsPerMetricDefinition: mutable.MultiMap[MetricDefinition, Actor] = new mutable.HashMap[MetricDefinition, mutable.Set[Actor]] with mutable.MultiMap[MetricDefinition, Actor]

  private val mfs = new MetricFileSystem

  def start: Any

  def provideMetrics(time: DateTime, providedMetrics: Metrics) = {
    println("provider [" + name + "] provides metrics of type [" + providedMetrics.definition + "] at [" + time + "] for interval [" + providedMetrics.interval + "]")

    saveMetrics(providedMetrics)
    notifyDependants(time, providedMetrics)
  }

  private def saveMetrics(metricsToSave: Metrics) = {
    mfs.writeMetrics(metricsToSave)
  }

  private def notifyDependants(time: DateTime, providedMetrics: Metrics) = {
    // notify all interested dependants of this new metric by sending them a message
    // (first foreach is to get the Some(...), second is to loop through the Set[Actor]
    dependantsPerMetricDefinition.get(providedMetrics.definition).foreach {_.foreach{_ ! MetricAvailableMessage(providedMetrics, time)}}
  }

  /**
   * Register a dependant which is interested in only the given metric type. Fails if this is not a type we can provide.
   */
  def registerDependant(dependant: Actor, metricDefinitionDependantIsInterestedIn: MetricDefinition) = {
    if (!providedMetricDefinitions.contains(metricDefinitionDependantIsInterestedIn))
      throw new IllegalArgumentException("[" + this + "] doesn't provide [" + metricDefinitionDependantIsInterestedIn + "]")
    dependantsPerMetricDefinition.add(metricDefinitionDependantIsInterestedIn, dependant)
  }

  /**
   * Register a dependant which is interested in all metrics we are providing
   */
  def registerDependant(dependant: Actor) = {
    providedMetricDefinitions.foreach(dependantsPerMetricDefinition.add(_, dependant))
  }

}
