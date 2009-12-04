package be.jvb.actorpmt

import org.joda.time.DateTimeFieldType
import org.scala_tools.time.Imports._

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
object DateTimeUtilities {

  def alignOnPrevious(duration: Duration, original: DateTime): DateTime = {
    val result = original.toMutableDateTime();
    // Align to lower magnitude
    // duration bigger than a half day
    if (duration.getMillis > 12 * 3600 * 1000)
      result.set(DateTimeFieldType.dayOfMonth(), 1);
    // duration bigger than a half hour
    if (duration.getMillis > 1800 * 1000)
      result.set(DateTimeFieldType.hourOfDay(), 0);
    // duration bigger than a half minute
    if (duration.getMillis > 30 * 1000)
      result.set(DateTimeFieldType.minuteOfHour(), 0);
    // duration bigger than a half minute
    if (duration.getMillis > 500)
      result.set(DateTimeFieldType.secondOfMinute(), 0);
    result.set(DateTimeFieldType.millisOfSecond(), 0);
    // Add duration until it is bigger than the original date
    while (result.isBefore(original))
      result.add(duration.getMillis);
    // then subtract a single duration
    result.add(-duration.getMillis);
    // Return date
    return result.toDateTime();
  }

  def alignOnPreviousOrEqual(duration: Duration, original: DateTime): DateTime = {
    val previous = alignOnPrevious(duration, original)
    if (previous + duration == original)
       return original;
    return previous;    
  }

}