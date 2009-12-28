package be.jvb.actorpmt

import org.joda.time.Interval
/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class OnDemandMetricMonitorAgent(val providerAgent: ProviderAgent) {
  def trigger(metricDefinition: MetricDefinition, interval: Interval) = {
    // find the provider (probably a derived provider aka monitor) I need in the provider agent
    val provider: Option[MetricProvider] = providerAgent.findProviderByProvidedMetricDefinition(metricDefinition)
    provider match {
      case None => throw new IllegalArgumentException("no provider known for [" + metricDefinition + "]")
      case Some(provider) => provider.triggerGeneration(interval: Interval)
    }
  }

//  private def trigger(provider: MetricProvider, interval: Interval): Unit = {
//    // TODO: its a source provider -> trigger it
//    // need to split generation of metrics and providing of metrics better... generation should be regardless of whether is a period provider etc
//  }
//
//  private def trigger(monitor: MetricMonitor, interval: Interval): Unit = {
//    // its a monitor -> recurse its dependencies
//    monitor.dependencies.foreach(trigger(_, interval))
//  }

}