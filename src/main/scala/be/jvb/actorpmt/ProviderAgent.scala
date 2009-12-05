package be.jvb.actorpmt

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class ProviderAgent(val metricsConfiguration:List[MetricDefinition], val monitors:MonitorRepository) {

  private def filterOutSourceMetricDefinitions(allMetricDefinitions: List[MetricDefinition]) = {
    allMetricDefinitions.filter(mDefinition => mDefinition.dependencies.isEmpty)
  }

  private def makeProviders(monitors: MonitorRepository, sourceMetricDefinitions: List[MetricDefinition]): List[MetricProviderScanner] = {
    for (sourceMetricDefinition <- sourceMetricDefinitions) yield {
      new MetricProviderScanner(sourceMetricDefinition, monitors.findMonitorsDependingOn(sourceMetricDefinition))
    }
  }

  def start = {
    val providers: List[MetricProviderScanner] = makeProviders(monitors, filterOutSourceMetricDefinitions(metricsConfiguration))
    providers.foreach(_.start)
  }
}