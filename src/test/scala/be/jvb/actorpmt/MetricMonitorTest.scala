package be.jvb.actorpmt


import org.joda.time.{DateTime, Interval, Duration}
import org.junit.Test
import org.junit.Assert._

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
class MetricMonitorTest {
  @Test
  def calculatedAllExpectedProviderIntervals() {
    val oneSecond = Duration.standardSeconds(1)
    val twoSeconds = Duration.standardSeconds(2)
    val fourSeconds = Duration.standardSeconds(4)
    val now = DateTimeUtilities.alignOnPrevious(oneSecond, new DateTime);

    assertEquals(
      List(new Interval(now, oneSecond), new Interval(now.plus(oneSecond), oneSecond)),
      MetricMonitor.calculatedAllExpectedProviderIntervals(oneSecond, new Interval(now, twoSeconds)))

    assertEquals(
      List(new Interval(now, twoSeconds)),
      MetricMonitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, twoSeconds)))

    assertEquals(
      List(new Interval(now, twoSeconds), new Interval(now.plus(twoSeconds), twoSeconds)),
      MetricMonitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, fourSeconds)))
  }

  @Test
  def allDependenciesReceived() {
    val source = new MetricDefinition("source", Duration.standardMinutes(1), Nil)
    val derived = new MetricDefinition("derived", Duration.standardMinutes(2), source :: Nil)

    val metricsConfiguration = source :: derived :: Nil

    val monitorAgent: MonitorAgent = new MonitorAgent(metricsConfiguration)
    val monitors: MonitorRepository = monitorAgent.start

    // get the single monitor that was created
    assertEquals(1, monitors.allMonitors.size)
    val monitor = monitors.allMonitors.head

    // pretend a source metric to be available
    val now = new DateTime(2005, 05, 05, 10, 24, 0, 0) // somewhat random
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(now, source.granularity)), // at 24 minutes and something
      new DateTime)

    // monitor should not have provided anything, because not everything is received yet
    //    fail("TODO")

    // pretend another source metric to be available
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(now.plus(source.granularity), source.granularity)), // at 25 minutes and something
      new DateTime)

    // now the monitor should have provided a metric
    Thread.sleep(1000)
    println("done")

    // TODO: make monitors extend a trait "monitor listener" which contains only the logic to see notifications from providers if metrics are available, and use another implementation of such a listener here in the test to catch the fact that our monitor has provided metrics... 
  }

}