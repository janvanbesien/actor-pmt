package be.jvb.actorpmt

import actors.Actor._
import org.joda.time.{Duration, DateTime, Interval}

/**
 * a dummy provider which provides dummy data for a list of metric definitions at the pace of the metric definitions granularities
 */
abstract class MockedPeriodicMetricProvider(val providedMetrics: List[MetricDefinition]) extends MetricProvider {
  def start = {
    for (providedMetric <- providedMetricDefinitions) {
      startPeriodicProvider(providedMetric)
    }
  }

  def startPeriodicProvider(providedMetric: MetricDefinition) = {
    actor {
      loop {
        Thread.sleep(providedMetric.granularity.getMillis) // TODO: use scheduler?
        println

        provideMetrics(now, generateMetrics(providedMetric, calculateProviderInterval(providedMetric.granularity)))
      }
    }
  }

  def calculateProviderInterval(sourceMetricGranularity: Duration): Interval = {
    val endOfProviderInterval = DateTimeUtilities.alignOnPrevious(sourceMetricGranularity, now)
    return new Interval(endOfProviderInterval.minus(sourceMetricGranularity), endOfProviderInterval)
  }

  lazy val now: DateTime = new DateTime

  def generateMetrics(metricDefinition: MetricDefinition, providerInterval: Interval): Metrics
}