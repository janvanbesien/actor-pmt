package be.jvb.actorpmt


import org.joda.time.{DateTime, Interval, Duration}
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.{JUnitRunner}

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(classOf[JUnitRunner])
class MetricMonitorTest extends FunSuite {

  test("calculated all expected provider intervals") {
    val definition: MetricDefinition = new MetricDefinition("test", Duration.standardMinutes(1), Nil)
    val monitor: MetricMonitor = new MetricMonitor(definition, new MonitorRepository)

    val oneSecond = Duration.standardSeconds(1)
    val twoSeconds = Duration.standardSeconds(2)
    val fourSeconds = Duration.standardSeconds(4)
    val now = DateTimeUtilities.alignOnPrevious(oneSecond, new DateTime);

    expect(List(new Interval(now, oneSecond), new Interval(now.plus(oneSecond), oneSecond))) {
      monitor.calculatedAllExpectedProviderIntervals(oneSecond, new Interval(now, twoSeconds))
    }

    expect(List(new Interval(now, twoSeconds))) {
      monitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, twoSeconds))
    }

    expect(List(new Interval(now, twoSeconds), new Interval(now.plus(twoSeconds), twoSeconds))) {
      monitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, fourSeconds))
    }
  }

  test("flush time calculation - granularity = 1 minute") {
    val granularity = 1
    val definition: MetricDefinition = new MetricDefinition("test", Duration.standardMinutes(granularity), Nil)
    val monitor: MetricMonitor = new MetricMonitor(definition, new MonitorRepository)

    expect(new DateTime(2005, 6, 6, 6, 17 - (5 * granularity), 6, 0)) {monitor.calculateFlushBeforeDate(new DateTime(2005, 6, 6, 6, 17, 6, 0))}
  }

  test("flush time calculation - granularity = 2 minutes") {
    val granularity = 2
    val definition: MetricDefinition = new MetricDefinition("test", Duration.standardMinutes(granularity), Nil)
    val monitor: MetricMonitor = new MetricMonitor(definition, new MonitorRepository)

    expect(new DateTime(2005, 6, 6, 6, 17 - (5 * granularity), 6, 0)) {monitor.calculateFlushBeforeDate(new DateTime(2005, 6, 6, 6, 17, 6, 0))}
  }

}