package be.jvb.actorpmt

import org.joda.time.Interval

/**
 * A bunch of metrics for a bunch of managed objects, all for the same interval and of the same metric type.
 *
 * The interval should be "alligned"
 */
class Metrics(val definition:MetricDefinition, val valuesPerManagedObject:Map[ManagedObjectName, Double], val interval:Interval) {
    override def toString():String = definition + " metrics [#managedObjects=" + valuesPerManagedObject.size + ", interval=" + interval + "]"
}
