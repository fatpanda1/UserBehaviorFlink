package computing

import org.apache.flink.streaming.api.scala._
import sink.FunnelPlotToRedisSink

object FunnelPlot {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    //设置为事件时间
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val resource = getClass.getResource("/UserBehavior.csv")

    val dataStream = env.readTextFile(resource.getPath)
      .map(line => {
        val dataArray = line.split(",")
        //        val userId = dataArray(0).trim.toLong
        val behavior = dataArray(3).trim
        (behavior, 1)
      })
      .keyBy(_._1)
      .sum(1)
      .addSink(new FunnelPlotToRedisSink)

    env.execute("FunnelPlot")
  }
}

//case class UserBehavior(userId: Long, behavior: String)

