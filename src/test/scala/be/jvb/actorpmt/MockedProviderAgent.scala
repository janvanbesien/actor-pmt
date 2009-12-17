package be.jvb.actorpmt

import org.joda.time.Interval

/**
 * A provider agent which creates a single MockedPeriodicMetricProvider providing a limited amount of metrics.
 *
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class MockedProviderAgent(val metricDefinitionsToProvide: List[MetricDefinition], override val monitors: MonitorRepository)
        extends ProviderAgent(monitors) {
  def makeProviders() = new SmallMockedPeriodicMetricProvider(metricDefinitionsToProvide) :: Nil
}

class SmallMockedPeriodicMetricProvider(override val providedMetricDefinitions: List[MetricDefinition]) extends MockedPeriodicMetricProvider(providedMetricDefinitions) {
  val name = "mocked periodic provider"

  def generateMetrics(metricDefinition: MetricDefinition, providerInterval: Interval) = {
    // a bunch of dummy metrics
    new Metrics(metricDefinition, Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0), providerInterval)
  }
}