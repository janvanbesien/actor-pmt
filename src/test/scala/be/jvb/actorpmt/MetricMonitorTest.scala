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
    val oneSecond = Duration.standardSeconds(1)
    val twoSeconds = Duration.standardSeconds(2)
    val fourSeconds = Duration.standardSeconds(4)
    val now = DateTimeUtilities.alignOnPrevious(oneSecond, new DateTime);

    expect(List(new Interval(now, oneSecond), new Interval(now.plus(oneSecond), oneSecond))) {
      MetricMonitor.calculatedAllExpectedProviderIntervals(oneSecond, new Interval(now, twoSeconds))
    }

    expect(List(new Interval(now, twoSeconds))) {
      MetricMonitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, twoSeconds))
    }

    expect(List(new Interval(now, twoSeconds), new Interval(now.plus(twoSeconds), twoSeconds))) {
      MetricMonitor.calculatedAllExpectedProviderIntervals(twoSeconds, new Interval(now, fourSeconds))
    }
  }

}