package be.jvb.actorpmt

import actors.Actor
import actors.Actor._
import org.joda.time.{Duration, DateTime, Interval}

/**
 * Regularly scans a metric definition and provides the metrics when available
 */
class MetricProviderScanner(val sourceMetricDefinition: MetricDefinition, val dependants: List[MetricMonitor]) extends Actor with MetricProvider {

  def act() {
    loop {
      Thread.sleep(sourceMetricDefinition.granularity.getMillis) // TODO: use scheduler
      println

      val providerInterval = calculateProviderInterval(sourceMetricDefinition.granularity)

      // do as if we generated a few metrics
      val generatedMetrics = new Metrics(sourceMetricDefinition,
                                         Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
                                         providerInterval)

      provideMetrics(now, generatedMetrics)
    }
  }

  def calculateProviderInterval(sourceMetricGranularity: Duration) : Interval = {
    val endOfProviderInterval = DateTimeUtilities.alignOnPrevious(sourceMetricDefinition.granularity, now)
    return new Interval(endOfProviderInterval.minus(sourceMetricDefinition.granularity), endOfProviderInterval)
  }

  def now(): DateTime = new DateTime
}