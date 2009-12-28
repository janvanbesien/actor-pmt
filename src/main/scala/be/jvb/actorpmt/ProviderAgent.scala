package be.jvb.actorpmt

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
abstract class ProviderAgent(val monitors: MonitorRepository) {
  /**
   * Construct all providers. "Real" provider agens will do this based on information about which sub systems exist in the hub.
   */
  def makeProviders(): List[MetricProvider]

  private var providers: Option[List[MetricProvider]] = None

  def start = {
    providers = Some(makeProviders())

    // register all monitors depending on metrics provided by these provider as listeners
    for (provider <- providers) {
      for (providedMetric <- provider.providedMetricDefinitions) {
        monitors.findMonitorsDependingOn(providedMetric).foreach(monitor => provider.registerDependant(monitor, providedMetric))
      }
    }

    providers.foreach(_.start)
  }

  def findProviderByProvidedMetricDefinition(metricDefinition: MetricDefinition) : Option[MetricProvider] = {
    providers match {
      case Some(p:List[MetricProvider]) => p.find(_.providedMetricDefinitions.contains(metricDefinition))
      case None => None
    }
  }
}