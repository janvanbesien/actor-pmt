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

    // TODO: monitors need to access monitors that depend on them, just like providers do... It might be nicer to have a repostory of providers and monitors which can be used by providers and monitors to look up all this stuff in stead of building it like currently done on the fly and passing in the mutable repo to every monitor when creating them...

    // make monitor for all derived metric definitions
    val derivedMetrics = metricDefinition.filter(mDefinition => !mDefinition.dependencies.isEmpty)
    // map of all the monitors per dependency (a dependency can be in multiple monitors -> multimap)
    val monitorsByDependencies: mutable.MultiMap[MetricDefinition, MetricMonitor] = new mutable.HashMap[MetricDefinition, mutable.Set[MetricMonitor]] with mutable.MultiMap[MetricDefinition, MetricMonitor]
    // simple list of all the monitors
    val monitors: mutable.ListBuffer[MetricMonitor] = new mutable.ListBuffer
    for (derivedMetric <- derivedMetrics) {
      val monitor = new MetricMonitor(derivedMetric, monitorsByDependencies)
      monitors.append(monitor)
      for (dependency <- derivedMetric.dependencies) {
        monitorsByDependencies.add(dependency, monitor)
      }
    }

    // make providers for all source metric definitions
    val sourceMetrics = metricDefinition.filter(mDefinition => mDefinition.dependencies.isEmpty)
    val providersByProvidedMetric = new mutable.HashMap[MetricDefinition, MetricProviderScanner]
    for (sourceMetric <- sourceMetrics) {
      // find monitors depending on this source metric
      val dependingMonitors = monitorsByDependencies.get(sourceMetric) match {
        case Some(monitors) => monitors.toList
        case None => Nil
      }
      // add provider to the collection of providers
      providersByProvidedMetric += (sourceMetric -> new MetricProviderScanner(sourceMetric, dependingMonitors))
    }

    // start monitors
    for (monitor <- monitors) {
      monitor.start
    }

    // start providers
    for (provider <- providersByProvidedMetric.values) {
      provider.start
    }

    println("started")
  }
}