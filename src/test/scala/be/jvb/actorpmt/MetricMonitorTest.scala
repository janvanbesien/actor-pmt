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
    //    new MetricMonitor()
    fail("TODO")
  }

}