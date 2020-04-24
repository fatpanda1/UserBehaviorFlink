package aggUtils

import computing.WebLog
import org.apache.flink.api.common.functions.AggregateFunction

class CountAgg extends AggregateFunction[WebLog,Long,Long]{
  override def createAccumulator(): Long = 0l

  override def add(in: WebLog, acc: Long): Long = acc + 1

  override def getResult(acc: Long): Long = acc

  override def merge(acc: Long, acc1: Long): Long = acc + acc1
}
