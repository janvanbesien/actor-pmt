package be.jvb.actorpmt

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class MonitorAgent(val metricsConfiguration:List[MetricDefinition]) {

  private def filterOutDerivedMetricDefinitions(allMetricDefinitions: List[MetricDefinition]) = {
    allMetricDefinitions.filter(mDefinition => !mDefinition.dependencies.isEmpty)
  }

  private def makeMonitors(derivedMetricDefinitions: List[MetricDefinition]): MonitorRepository = {
    val repository = new MonitorRepository

    for (derivedMetricDefinition <- derivedMetricDefinitions) {
      repository.register(new MetricMonitor(derivedMetricDefinition, repository))
    }

    return repository
  }

  def start : MonitorRepository = {
    val monitors: MonitorRepository = makeMonitors(filterOutDerivedMetricDefinitions(metricsConfiguration))
    monitors.allMonitors.foreach(_.start)
    return monitors
  }
}