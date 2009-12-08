package be.jvb.actorpmt

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.concurrent.{TimeUnit, CountDownLatch}
import org.joda.time.{Interval, DateTime, Duration}
/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(classOf[JUnitRunner])
class MetricMonitorMetricsAvailabilityTest extends FunSuite {

  test("metrics for one derived metric definition are available when its single source metrics are available") {
    val source = new MetricDefinition("source", Duration.standardMinutes(1), Nil)
    val derived = new MetricDefinition("derived", Duration.standardMinutes(2), source :: Nil)

    val metricsConfiguration = source :: derived :: Nil

    val monitorAgent: MonitorAgent = new MonitorAgent(metricsConfiguration)
    val monitors: MonitorRepository = monitorAgent.start

    // get the single monitor that was created
    expect(1) {monitors.allMonitors.size}
    val monitor = monitors.allMonitors.head

    val expectedMetricIntervalReceived = new CountDownLatch(1)

    val now = new DateTime(2005, 05, 05, 10, 24, 0, 0) // somewhat random
    val expectedInterval = new Interval(now, derived.granularity)

    // register a listener with the monitor
    class TestProviderListener extends ProviderListener {
      def processMetricAvailableMessage(message: MetricAvailableMessage) = {
        if (message.metrics.interval == expectedInterval)
          expectedMetricIntervalReceived.countDown
        else
          fail("received unexpected metrics [" + message + "]")
      }
    }
    monitor.registerDependant(new TestProviderListener().start)

    // pretend a source metric to be available
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(now, source.granularity)), // at 24 minutes and something
      new DateTime)

    // monitor should not have provided anything, because not everything is received yet
    expect(expectedMetricIntervalReceived.getCount) {1}

    // pretend another source metric to be available
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(now.plus(source.granularity), source.granularity)), // at 25 minutes and something
      new DateTime)

    // now the monitor should have provided a metric
    assert(expectedMetricIntervalReceived.await(1, TimeUnit.SECONDS))
  }

}