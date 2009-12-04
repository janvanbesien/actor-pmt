package be.jvb.actorpmt


import org.joda.time.{DateTime, Interval}

trait MetricProvider {
  val sourceMetricDefinition: MetricDefinition

  def dependants: List[MetricMonitor]

  val mfs = new MetricFileSystem

  def provideMetrics(time: DateTime, providedMetrics: Metrics) = {
    println("provider [" + sourceMetricDefinition.name + "] provides metrics at [" + time + "] for interval [" + providedMetrics.interval + "]")

    saveMetrics(providedMetrics) //maybe do this asynchronously in an actor which processes all the saves?
    notifyDependants(time, providedMetrics)
  }

  def saveMetrics(metricsToSave: Metrics) = {
    mfs.writeMetrics(metricsToSave)
  }

  def notifyDependants(time: DateTime, providedMetrics: Metrics) = {
    // notify all dependants of this new metric by sending them a message
    for (dependant <- dependants) {
      dependant ! MetricAvailableMessage(providedMetrics, time)
    }
  }
}
