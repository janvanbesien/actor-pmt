package be.jvb.actorpmt

import scala.collection._

class MonitorRepository {

  // map of all the monitors per dependency (a dependency can be in multiple monitors -> multimap)
  private val monitorsByDependencies: mutable.MultiMap[MetricDefinition, MetricMonitor] = new mutable.HashMap[MetricDefinition, mutable.Set[MetricMonitor]] with mutable.MultiMap[MetricDefinition, MetricMonitor]

  // simple list of the same monitors
  private val monitors: mutable.ListBuffer[MetricMonitor] = new mutable.ListBuffer

  def register(monitor: MetricMonitor) = {
    monitors.append(monitor)
    for (dependency <- monitor.dependencies) {
      monitorsByDependencies.add(dependency, monitor)
    }
  }

  def findMonitorsDependingOn(metricDefinition: MetricDefinition): List[MetricMonitor] = {
    monitorsByDependencies.get(metricDefinition) match {
        case Some(monitors) => monitors.toList
        case None => Nil
      }
  }
  
  def allMonitors = monitors.toList
}