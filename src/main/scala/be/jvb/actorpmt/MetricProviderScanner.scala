package be.jvb.actorpmt

import actors.Actor
import actors.Actor._
import org.joda.time.{Duration, DateTime, Interval}

/**
 * Regularly scans a metric definition and provides the metrics when available
 */
class MetricProviderScanner(val metricDefinition: MetricDefinition) extends Actor with MetricProvider {

  def act() {
    loop {
      Thread.sleep(metricDefinition.granularity.getMillis) // TODO: use scheduler
      println

      val providerInterval = calculateProviderInterval(metricDefinition.granularity)

      // do as if we generated a few metrics
      val generatedMetrics = new Metrics(metricDefinition,
                                         Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
                                         providerInterval)

      provideMetrics(now, generatedMetrics)
    }
  }

  def calculateProviderInterval(sourceMetricGranularity: Duration) : Interval = {
    val endOfProviderInterval = DateTimeUtilities.alignOnPrevious(metricDefinition.granularity, now)
    return new Interval(endOfProviderInterval.minus(metricDefinition.granularity), endOfProviderInterval)
  }

  def now(): DateTime = new DateTime
}