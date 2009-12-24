package be.jvb.actorpmt

import org.joda.time.Duration

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

    val metricsConfiguration = m1 :: m2 :: m3 :: m4 :: m5 :: m6 :: Nil
//        val metricsConfiguration = m1 :: m2 :: m3 :: Nil

    val monitorAgent: MonitorAgent = new MonitorAgent(metricsConfiguration)
    val monitors: MonitorRepository = monitorAgent.start

    val providerAgent: ProviderAgent = new MockedProviderAgent(filterOutSourceMetricDefinitions(metricsConfiguration), monitors)
    providerAgent.start

    println("started")
  }

  private def filterOutSourceMetricDefinitions(allMetricDefinitions: List[MetricDefinition]) = {
    allMetricDefinitions.filter(mDefinition => mDefinition.dependencies.isEmpty)
  }


}