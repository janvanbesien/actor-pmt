package be.jvb.actorpmt

import org.joda.time.Duration
import scala.collection._

/**
 * Represents a metric type. If there are no dependencies, it is a source type (i.e. one for which a the metrics should be provided by a provider)
 */
class MetricDefinition(val name: String, val granularity: Duration, val dependencies: List[MetricDefinition], val dependants: mutable.ListBuffer[MetricDefinition]) {

  // TODO: verify that the granularity of the dependencies is smaller or equal to the granularity of the metric

  /**
   * Create a metric definition with dependencies but without any other metric definition depending on it yet.
   */
  def this(name: String, granularity: Duration, dependencies: List[MetricDefinition]) = {
    this (name, granularity, dependencies, new mutable.ListBuffer[MetricDefinition])

//    // check granularity of all dependencies
//    for (dependency <- dependencies) {
//      if (granularity.getMillis % dependency.granularity.getMillis != 0)
//        throw new IllegalStateException("metric granularity should be factor of granularity of its dependency")
//    }

    // all dependencies now have this as dependant (// TODO: can this be done with immutable data structures?)
    for (dependency <- dependencies) {
      dependency.dependants.append(this)
    }
  }

  override def toString: String = name + "[gran=" + granularity + "]"
}