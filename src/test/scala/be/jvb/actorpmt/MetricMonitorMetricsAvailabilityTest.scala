package be.jvb.actorpmt

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.joda.time.{Interval, DateTime, Duration}
import java.util.concurrent.{TimeUnit, CountDownLatch}

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(classOf[JUnitRunner])
class MetricMonitorMetricsAvailabilityTest extends FunSuite {

  /**
   * Helper class which listens to a metric availabilty messages from a provider to assert things about the availability of metrics.
   */
  class TestProviderListener(val expectedInterval: Interval, val notificationLatch: CountDownLatch) extends ProviderListener {
    def processMetricAvailableMessage(message: MetricAvailableMessage) = {
      if (message.metrics.interval == expectedInterval)
        notificationLatch.countDown
    }
  }

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
    monitor.registerDependant(new TestProviderListener(expectedInterval, expectedMetricIntervalReceived).start)

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

  test("late arriving source metrics trigger derived metric availability without a recovery mechanism of some sort") {
    val source = new MetricDefinition("source", Duration.standardMinutes(1), Nil)
    val derived = new MetricDefinition("derived", Duration.standardMinutes(2), source :: Nil)

    val metricsConfiguration = source :: derived :: Nil

    val monitorAgent: MonitorAgent = new MonitorAgent(metricsConfiguration)
    val monitors: MonitorRepository = monitorAgent.start

    // get the single monitor that was created
    expect(1) {monitors.allMonitors.size}
    val monitor = monitors.allMonitors.head

    val now = new DateTime(2005, 05, 05, 10, 24, 0, 0) // somewhat random

    val firstExpectedInterval = new Interval(now, derived.granularity)
    val secondExpectedInterval = new Interval(firstExpectedInterval.getEnd, derived.granularity)
    val thirdExpectedInterval = new Interval(secondExpectedInterval.getEnd, derived.granularity)

    val firstIntervalReceived = new CountDownLatch(1)
    val secondIntervalReceived = new CountDownLatch(1)
    val thirdIntervalReceived = new CountDownLatch(1)

    // register a listener for each expected interval with the monitor
    monitor.registerDependant(new TestProviderListener(firstExpectedInterval, firstIntervalReceived).start)
    monitor.registerDependant(new TestProviderListener(secondExpectedInterval, secondIntervalReceived).start)
    monitor.registerDependant(new TestProviderListener(thirdExpectedInterval, thirdIntervalReceived).start)

    // pretend all source metrics to be available for FIRST monitor interval to be available
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(firstExpectedInterval.getStart, source.granularity)),
      new DateTime)
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(firstExpectedInterval.getStart.plus(source.granularity), source.granularity)),
      new DateTime)

    // first interval should be available
    assert(firstIntervalReceived.await(1, TimeUnit.SECONDS))

    // pretend HALF OF THE source metrics to be available for SECOND monitor interval
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(secondExpectedInterval.getStart, source.granularity)),
      new DateTime)

    // second interval should NOT YET be available
    expect(secondIntervalReceived.getCount) {1}

    // pretend all source metrics to be available for THIRD monitor interval to be available
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(thirdExpectedInterval.getStart, source.granularity)),
      new DateTime)
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(thirdExpectedInterval.getStart.plus(source.granularity), source.granularity)),
      new DateTime)

    // third interval should be available, although SECOND interval is still not available
    assert(thirdIntervalReceived.await(1, TimeUnit.SECONDS))
    expect(secondIntervalReceived.getCount) {1}

    // now pretend remaining HALF OF THE source metrics to be available for SECOND monitor interval
    monitor ! MetricAvailableMessage(
      new Metrics(
        source,
        Map(ManagedObjectName("sit1") -> 1.0, ManagedObjectName("sit2") -> 2.0),
        new Interval(secondExpectedInterval.getStart.plus(source.granularity), source.granularity)),
      new DateTime)

    // now the second interval should be available completely, without having to use a recovery mechanism
    assert(secondIntervalReceived.await(1, TimeUnit.SECONDS))
  }

  test("late arriving source metrics don't trigger derived metric availability if they are so " +
          "late that the other required source metrics were already cleaned up") {
    fail("todo")
  }
}