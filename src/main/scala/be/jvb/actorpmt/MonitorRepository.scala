package be.jvb.actorpmt

import scala.collection._

trait MonitorRepository {

  // map of all the monitors per dependency (a dependency can be in multiple monitors -> multimap)
  private val monitorsByDependencies: mutable.MultiMap[MetricDefinition, MetricMonitor] = new mutable.HashMap[MetricDefinition, mutable.Set[MetricMonitor]] with mutable.MultiMap[MetricDefinition, MetricMonitor]

  def register(monitor:MetricMonitor) = {

  }

//  def find
}