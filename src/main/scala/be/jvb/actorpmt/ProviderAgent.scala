package be.jvb.actorpmt

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
abstract class ProviderAgent(val monitors: MonitorRepository) {
  /**
   * Construct all providers. "Real" provider agens will do this based on information about which sub systems exist in the hub.
   */
  def makeProviders(): List[MetricProvider]

  def start = {
    val providers: List[MetricProvider] = makeProviders()

    // register all monitors depending on metrics provided by these provider as listeners
    for (provider <- providers) {
      for (providedMetric <- provider.providedMetricDefinitions) {
        monitors.findMonitorsDependingOn(providedMetric).foreach(monitor => provider.registerDependant(monitor, providedMetric))
      }
    }

    providers.foreach(_.start)
  }
}