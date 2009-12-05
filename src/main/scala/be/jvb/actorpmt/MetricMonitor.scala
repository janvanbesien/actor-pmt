package be.jvb.actorpmt

import actors.Actor
import actors.Actor._
import org.joda.time.{Period, Duration, DateTime, Interval}
import scala.collection._
import org.scala_tools.time.Imports._

class MetricMonitor(val metricDefinition: MetricDefinition, val repository: MonitorRepository) extends Actor with MetricProvider
{
  /**keep a collection of all received metrics, per monitor interval they are accounted in (multiple entries possible),
   * per metric definition we depend on */
  private var received: mutable.Map[MetricDefinition, mutable.MultiMap[Interval, Metrics]] =
  new mutable.HashMap[MetricDefinition, mutable.MultiMap[Interval, Metrics]]

  {
    // initialize the map with received metrics with an empty entry per metric definition that we depend on
    for (dependency <- metricDefinition.dependencies) {
      received.put(dependency, new mutable.HashMap[Interval, mutable.Set[Metrics]] with mutable.MultiMap[Interval, Metrics])
    }
  }

  def dependants = repository.findMonitorsDependingOn(metricDefinition)

  def dependencies = metricDefinition.dependencies

  def act() {
    loop {
      react {
        case m: MetricAvailableMessage => processMetricAvailableMessage(m)
        case msg => println("received unkown message")
      }
    }
  }

  def processMetricAvailableMessage(receivedMessage: MetricAvailableMessage) = {
    val alreadyReceivedMetricsOfThisType: mutable.MultiMap[Interval, Metrics] = received.get(receivedMessage.metrics.definition).get

    // TODO: cleanup old stuff from the received collection...

    val accountingInterval = calculateIntervalInWhichToAccount(receivedMessage)
    println("monitor [" + metricDefinition + "] received something which it accounts in monitor interval [" + accountingInterval + "]")

    // store the metrics with other metrics of this type, in the corresponding monitor interval
    alreadyReceivedMetricsOfThisType.add(accountingInterval, receivedMessage.metrics)

    // check if we received all dependencies in this interval
    if (allDependenciesReceived(accountingInterval)) {
      println("monitor [" + metricDefinition + "] received all its dependencies at [" + new DateTime + "], calculating and providing metrics")

      provideMetrics(new DateTime, calculateMetrics(receivedMessage.metrics, accountingInterval))
      // flush all metrics of this interval (of any type)
      received.values.foreach(metricsPerMonitorInterval => metricsPerMonitorInterval - accountingInterval)
    } else {
      println("monitor [" + metricDefinition + "] didn't receive all its dependencies at [" + new DateTime + "]")
    }
  }

  def calculateIntervalInWhichToAccount(receivedMessage: MetricAvailableMessage): Interval = {
    // calculate the monitor interval in which this metric interval would fit (based on start time of the received metric)
    val accountingIntervalStart = DateTimeUtilities.alignOnPreviousOrEqual(metricDefinition.granularity, receivedMessage.metrics.interval.start)
    return new Interval(accountingIntervalStart, accountingIntervalStart + metricDefinition.granularity)
  }

  // check if all dependencies are received in the given interval, for all expected periods!!!
  def allDependenciesReceived(monitorInterval: Interval): Boolean = {
    // loop through all maps of already received metrics for all metric definitions we depend on
    for ((dependency, metricsPerMonitorInterval) <- received.elements) {
      val alreadyReceivedMetricsInMonitorInterval = metricsPerMonitorInterval.get(monitorInterval)

      alreadyReceivedMetricsInMonitorInterval match {

        case None => // nothing received for this type/interval yet
          return false

        case Some(receivedMetrics) => { // something received, check if everything is received
          // calculate the intervals that should be available for this dependency
          val expectedProviderIntervals = MetricMonitor.calculatedAllExpectedProviderIntervals(dependency.granularity, monitorInterval)
          println("monitor [" + metricDefinition + "] expects [" + dependency + "] in intervals [" + expectedProviderIntervals + "] and has [" + receivedMetrics + "]")
          // check if a metric was received for all expected intervals
          // (filter expected metrics from receivedMetrics and if that list is smaller than what we expect, it is not complete)
          if (receivedMetrics.filter(metric => expectedProviderIntervals.contains(metric.interval)).toList.size < expectedProviderIntervals.size) {
            return false
          }
        }
      }
    }

    return true
  }

  def calculateMetrics(sourceMetrics:Metrics, accountingInterval:Interval) : Metrics = {
    // for now we just multiply every value by two
    return new Metrics(metricDefinition, immutable.Map() ++ sourceMetrics.valuesPerManagedObject.map{case (key,value) => (key,value*2)}, accountingInterval)
  }
}

object MetricMonitor {

  /**
   * Generate a list of all intervals of a duration = providerGranularity that fit within the monitorInterval.
   */
  def calculatedAllExpectedProviderIntervals(providerGranularity: Duration, monitorInterval: Interval): List[Interval] = {

    val result = new mutable.ListBuffer[Interval]()

    // TODO: functional style? (monitorinterval.start to monitorinterval.end by providergranularity)
    var currentIntervalStart = monitorInterval.start
    while (currentIntervalStart < monitorInterval.end) {
      result += providerGranularity.toIntervalFrom(currentIntervalStart)
      currentIntervalStart = currentIntervalStart + providerGranularity
    }

    return result.toList
  }

}