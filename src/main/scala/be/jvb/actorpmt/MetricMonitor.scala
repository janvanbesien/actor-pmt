package be.jvb.actorpmt

import scala.collection._
import org.scala_tools.time.Imports._

/**
 * A monitor is a provider listener (and calculates derived metric when receiving metrics from a provider) and a provider in itself
 * (providing the calculated derived metrics to other monitors)
 */
class MetricMonitor(val metricDefinition: MetricDefinition) extends ProviderListener with MetricProvider
{
  /**keep a collection of all received metrics, per monitor interval they are accounted in (multiple entries possible),
   * per metric definition we depend on */
  private val receivedMetricsPerIntervalPerType: mutable.Map[MetricDefinition, mutable.MultiMap[Interval, Metrics]] =
  new mutable.HashMap[MetricDefinition, mutable.MultiMap[Interval, Metrics]]

  // TODO: this looks a bit messy because its not in a constructor... or is that just my java background?
  // initialize the map with received metrics with an empty entry per metric definition that we depend on
  for (dependency <- metricDefinition.dependencies) {
    receivedMetricsPerIntervalPerType.put(dependency, new mutable.HashMap[Interval, mutable.Set[Metrics]] with mutable.MultiMap[Interval, Metrics])
    println("initialized received map with placeholder for [" + dependency + "] in [" + this + "]")
  }

  val providedMetricDefinitions = metricDefinition :: Nil

  val name = "mon {" + metricDefinition + "}"

  def dependencies = metricDefinition.dependencies

  def processMetricAvailableMessage(receivedMessage: MetricAvailableMessage) = {
    if (expected(receivedMessage)) {
      val accountingInterval = calculateIntervalInWhichToAccount(receivedMessage)
      println("monitor [" + metricDefinition + "] received something about [" + receivedMessage.metrics.interval +
              "] which it accounts in monitor interval [" + accountingInterval + "]")

      accountReceivedMetrics(accountingInterval, receivedMessage.metrics)

      if (allDependenciesReceived(accountingInterval)) {
        println("monitor [" + metricDefinition + "] received all its dependencies at [" + new DateTime + "], calculating and providing metrics")

        provideMetrics(new DateTime, calculateMetrics(receivedMessage.metrics, accountingInterval))
        flushMetricsInInterval(accountingInterval)
        flushMetricsOlderThen(calculateFlushBeforeDate(accountingInterval.getStart))
      } else {
        println("monitor [" + metricDefinition + "] didn't receive all its dependencies at [" + new DateTime + "]")
      }
    } else {
      Console.err.println("WARNING: unexpected metric type [" + receivedMessage.metrics.definition + "] in [" + this + "]")
    }
  }

  def expected(message: MetricAvailableMessage): Boolean = {
    // expected if we have the metric definition registered as dependency
    return receivedMetricsPerIntervalPerType.contains(message.metrics.definition)
  }

  def calculateIntervalInWhichToAccount(receivedMessage: MetricAvailableMessage): Interval = {
    // calculate the monitor interval in which this metric interval would fit (based on start time of the received metric)
    val accountingIntervalStart = DateTimeUtilities.alignOnPreviousOrEqual(metricDefinition.granularity, receivedMessage.metrics.interval.start)
    return new Interval(accountingIntervalStart, accountingIntervalStart + metricDefinition.granularity)
  }

  def accountReceivedMetrics(accountingInterval: Interval, metrics: Metrics) = {
    receivedMetricsPerIntervalPerType.get(metrics.definition) match {
      case None => println("accounting something of type [" + metrics.definition + "] in [" + this + "]")
      case _ => () // skip
    }
    val alreadyReceivedMetricsOfThisType: mutable.MultiMap[Interval, Metrics] = receivedMetricsPerIntervalPerType.get(metrics.definition).get
    // store the metrics with other metrics of this type, in the corresponding monitor interval
    alreadyReceivedMetricsOfThisType.add(accountingInterval, metrics)
  }

  def flushMetricsInInterval(interval: Interval) = {
    val receivedMetricsPerIntervalForAllTypes = receivedMetricsPerIntervalPerType.values
    receivedMetricsPerIntervalForAllTypes.foreach((metricsPerInterval: mutable.MultiMap[Interval, Metrics]) => metricsPerInterval - interval)
  }

  def flushMetricsOlderThen(reference: DateTime) = {
    val receivedMetricsPerIntervalForAllTypes = receivedMetricsPerIntervalPerType.values
    receivedMetricsPerIntervalForAllTypes.foreach(
      (metricsPerInterval: mutable.MultiMap[Interval, Metrics]) => {
        val sizeBefore = metricsPerInterval.size
        metricsPerInterval.retain {case (interval, metrics) => interval.isAfter(reference)}
        println("flushed [" + (sizeBefore - metricsPerInterval.size) + "] intervals")
      }
      )
  }

  def calculateFlushBeforeDate(reference: DateTime): DateTime = {
    val nMonitorIntervalsToRetain = 5
    var result = reference
    for (i <- 1 to 5)
      result = result.minus(metricDefinition.granularity)
    result
  }

  // check if all dependencies are received in the given interval, for all expected periods!!!
  def allDependenciesReceived(monitorInterval: Interval): Boolean = {
    // loop through all maps of already received metrics for all metric definitions we depend on
    for ((dependency, metricsPerMonitorInterval) <- receivedMetricsPerIntervalPerType.elements) {
      val alreadyReceivedMetricsInMonitorInterval = metricsPerMonitorInterval.get(monitorInterval)

      alreadyReceivedMetricsInMonitorInterval match {

        case None => // nothing received for this type/interval yet
          return false

        case Some(receivedMetrics) => { // something received, check if everything is received
          // calculate the intervals that should be available for this dependency
          val expectedProviderIntervals = calculatedAllExpectedProviderIntervals(dependency.granularity, monitorInterval)
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

  def calculateMetrics(sourceMetrics: Metrics, accountingInterval: Interval): Metrics = {
    // for now we just multiply every value by two
    return new Metrics(metricDefinition, immutable.Map() ++ sourceMetrics.valuesPerManagedObject.map {
      case (key, value) => (key, value * 2)
    }, accountingInterval)
  }

  override def toString() = "mon {" + metricDefinition + "}"

}

