package be.jvb.actorpmt


import org.joda.time.{DateTime, Interval}

trait MetricProvider {
  val sourceMetricDefinition: MetricDefinition

  def dependants: List[MetricMonitor]

  def provideMetrics(time: DateTime, providedMetrics: Metrics) = {
    println("provider [" + sourceMetricDefinition.name + "] provides metrics at [" + time + "] for interval [" + providedMetrics.interval + "]")

    // notify all dependants of this new metric by sending them a message
    for (dependant <- dependants) {
      dependant ! MetricAvailableMessage(providedMetrics, time)
    }
  }
}
