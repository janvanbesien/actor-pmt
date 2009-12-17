package be.jvb.actorpmt

import actors.Actor
import actors.Actor._
import org.joda.time.{Duration, DateTime, Interval}

/**
 * Regularly scans a metric definition and provides the metrics when available
 */
/*abstract*/ class MetricProviderScanner(val metricDefinition: MetricDefinition) extends Actor with MetricProvider {
  def act() {
    loop {
      Thread.sleep(metricDefinition.granularity.getMillis) // TODO: use scheduler
      println

      val providerInterval = calculateProviderInterval(metricDefinition.granularity)

      provideMetrics(now, generateMetrics(providerInterval))
    }
  }

  def calculateProviderInterval(sourceMetricGranularity: Duration): Interval = {
    val endOfProviderInterval = DateTimeUtilities.alignOnPrevious(metricDefinition.granularity, now)
    return new Interval(endOfProviderInterval.minus(metricDefinition.granularity), endOfProviderInterval)
  }

  lazy val now: DateTime = new DateTime

  // TODO: this needs to move to a test mock implementation... by default this method should be abstract 
  def generateMetrics(providerInterval: Interval): Metrics = {
    // do as if we generated a few metrics
    new Metrics(metricDefinition, Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0), providerInterval)

  }
}