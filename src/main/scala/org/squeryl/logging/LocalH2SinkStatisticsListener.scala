package org.squeryl.logging

import org.squeryl.{ AbstractSession, Session }
import org.squeryl.adapters.H2Adapter
import org.squeryl.InternalFieldMapper._

object LocalH2SinkStatisticsListener {

  def initializeOverwrite(schemaName: String, workingDir: String = "."): LocalH2SinkStatisticsListener =
    initialize(schemaName, overwrite = true, workingDir)

  def initializeAppend(schemaName: String, workingDir: String = "."): LocalH2SinkStatisticsListener =
    initialize(schemaName, overwrite = false, workingDir)

  def initialize(schemaName: String, overwrite: Boolean, workingDir: String): LocalH2SinkStatisticsListener = {
    Class.forName("org.h2.Driver")

    val file =
      new java.io.File(workingDir, schemaName + ".h2.db").getCanonicalFile

    if (file.exists && overwrite)
      file.delete

    val s = new Session(
      java.sql.DriverManager
        .getConnection("jdbc:h2:" + workingDir + "/" + schemaName, "sa", ""),
      new H2Adapter
    )

    if ((!file.exists) || overwrite)
      using(s) {
        StatsSchema.create()
      }

    val l = new LocalH2SinkStatisticsListener(s)
    l
  }
}

class LocalH2SinkStatisticsListener(val h2Session: AbstractSession) extends StatisticsListener {

  private var _closed = false

  private val _queue =
    new java.util.concurrent.ArrayBlockingQueue[() => Unit](1024, false)

  private val _worker = new Thread {

    override def run(): Unit = {
      h2Session.bindToCurrentThread()
      while (!_closed) {
        val op = _queue.take
        op()
      }
    }
  }

  _worker.start()

  def shutdown(): Unit = _closed = true

  private def _pushOp(op: => Unit): Unit =
    if (!_closed) {
      _queue.put(op _)
    } else
      throw new IllegalStateException('LocalH2SinkStatisticsListener + " has been shutdown.")

  def generateStatSummary(staticHtmlFile: java.io.File, n: Int) = _pushOp {
    BarChartRenderer.generateStatSummary(staticHtmlFile, n)
  }

  def queryExecuted(se: StatementInvocationEvent) = _pushOp {
    StatsSchema.recordStatementInvocation(se)
    h2Session.connection.commit
  }

  def resultSetIterationEnded(
    invocationId: String,
    iterationEndTime: Long,
    rowCount: Int,
    iterationCompleted: Boolean
  ) = _pushOp {
    StatsSchema.recordEndOfIteration(invocationId, iterationEndTime: Long, rowCount: Int, iterationCompleted: Boolean)
    h2Session.connection.commit
  }

  def updateExecuted(se: StatementInvocationEvent) = {}

  def insertExecuted(se: StatementInvocationEvent) = {}

  def deleteExecuted(se: StatementInvocationEvent) = {}
}
