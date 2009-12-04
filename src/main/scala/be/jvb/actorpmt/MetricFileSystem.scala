package be.jvb.actorpmt

import java.io.{FileOutputStream, BufferedOutputStream, DataOutputStream, File}

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */

class MetricFileSystem{

  val root = new File("/tmp/actorpmt")

  def writeMetrics(metrics:Metrics) = {
    val metricTypeDirectory = new File(root, metrics.definition.name)
    val metricTypeAndGranularityDirectory = new File(metricTypeDirectory, metrics.definition.granularity.toString)
    metricTypeAndGranularityDirectory.mkdirs
    val file = new File(metricTypeAndGranularityDirectory, metrics.interval.getStart.toString())

    val out: DataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      for ((moName, value) <- metrics.valuesPerManagedObject) {
        out.writeChars(moName.name)
        out.writeChars("=")
        out.writeChars("" + value)
        out.writeChars(";")
      }
    } finally {
      out.close
    }
  }
}