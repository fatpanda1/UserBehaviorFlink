package computing

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import windowFunctionUtils.UvCountWindow

object UniqueVisitor {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    //用相对路径定义数据源
    val resource = getClass.getResource("/UserBehavior.csv")
    val dataStream = env.readTextFile(resource.getPath)
      .map(data => {
        val dataArray = data.split(",")
        val userId = dataArray(0).trim.toLong
        val itemId = dataArray(1).trim.toLong
        val categoryId = dataArray(2).trim.toInt
        val behavior = dataArray(3).trim
        val time = dataArray(4).trim.toLong
        UserBehavior(userId, itemId, categoryId, behavior, time)
      })
      .assignAscendingTimestamps(_.timestamp * 1000L)
      .filter(_.behavior.equals("pv")) //筛选pv操作
      .timeWindowAll(Time.hours(1))
      .apply(new UvCountWindow())
      .print()

    env.execute("uv count job")
  }
}

case class UvCount(time: Long, uvCount: Long)
