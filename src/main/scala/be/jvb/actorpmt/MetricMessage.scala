package be.jvb.actorpmt

import org.joda.time.DateTime

abstract sealed case class MetricMessage
case class MetricAvailableMessage(metrics:Metrics, availableTime:DateTime) extends MetricMessage