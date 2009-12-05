package be.jvb.actorpmt

import org.joda.time.Duration
import org.joda.time.format.ISOPeriodFormat
import scala.collection._

object Main {
  def main(args: Array[String]) {
    println("starting")

    // setup a set of metric definitions (aka metrics.xml)
    val m1 = new MetricDefinition("m1", new Duration(1000L), Nil) // source
    val m2 = new MetricDefinition("m2", new Duration(2000L), Nil) // source
    val m3 = new MetricDefinition("m3", new Duration(2000L), m1 :: m2 :: Nil)
    val m4 = new MetricDefinition("m4", new Duration(2000L), m1 :: Nil)
    val m5 = new MetricDefinition("m5", new Duration(5000L), m1 :: m3 :: Nil)
    val m6 = new MetricDefinition("m6", new Duration(2000L), m4 :: Nil)
    val m7 = new MetricDefinition("m7", new Duration(5000L), m1 :: Nil)

//    val metricDefinition = m1 :: m2 :: m3 :: m4 :: m5 :: m6 :: Nil
    val metricDefinition = m1 :: m2 :: m3 :: Nil

    // make monitor for all derived metric definitions
    val derivedMetrics = metricDefinition.filter(mDefinition => !mDefinition.dependencies.isEmpty)

    // TODO: find a way to "dependency inject" this where it is required (trait?)
    val repository = new MonitorRepository()

    for (derivedMetric <- derivedMetrics) {
      val monitor = new MetricMonitor(derivedMetric, repository)
      repository.register(monitor)
    }

    // make providers for all source metric definitions
    val sourceMetrics = metricDefinition.filter(mDefinition => mDefinition.dependencies.isEmpty)
    val providersByProvidedMetric = new mutable.HashMap[MetricDefinition, MetricProviderScanner]
    for (sourceMetric <- sourceMetrics) {
      // find monitors depending on this source metric
      val dependingMonitors = repository.findMonitorsDependingOn(sourceMetric)
      // add provider to the collection of providers
      providersByProvidedMetric += (sourceMetric -> new MetricProviderScanner(sourceMetric, dependingMonitors))
    }

    // start monitors
    repository.allMonitors.foreach(monitor => monitor.start)

    // start providers
    providersByProvidedMetric.values.foreach(provider => provider.start)

    println("started")
  }
}