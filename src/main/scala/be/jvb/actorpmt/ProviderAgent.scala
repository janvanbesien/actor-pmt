package be.jvb.actorpmt

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class ProviderAgent(val metricsConfiguration: List[MetricDefinition], val monitors: MonitorRepository) {
  private def filterOutSourceMetricDefinitions(allMetricDefinitions: List[MetricDefinition]) = {
    allMetricDefinitions.filter(mDefinition => mDefinition.dependencies.isEmpty)
  }

  private def makeProviders(monitors: MonitorRepository, sourceMetricDefinitions: List[MetricDefinition]): List[MetricProviderScanner] = {
    // TODO: in the real pmt, there is no provider per source metric definition but per sub system, and one provider providers multiple source metrics...
    for (sourceMetricDefinition <- sourceMetricDefinitions) yield {
      new MetricProviderScanner(sourceMetricDefinition)
    }
  }

  def start = {
    val providers: List[MetricProviderScanner] = makeProviders(monitors, filterOutSourceMetricDefinitions(metricsConfiguration))

    // register all monitors depending on these provider as listeners
    for (provider <- providers) {
      monitors.findMonitorsDependingOn(provider.metricDefinition).foreach(monitor => provider.registerDependant(monitor))
    }

    providers.foreach(_.start)
  }
}