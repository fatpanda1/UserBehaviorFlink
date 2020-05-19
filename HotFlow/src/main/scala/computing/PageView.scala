package computing

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import sink.PvToRedisSink
import windowFunctionUtils.PvCountWindow

object PageView {
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
        UserBehavior(userId,itemId,categoryId,behavior,time)
      })
      .assignAscendingTimestamps(_.timestamp * 1000L)
      .filter(_.behavior.equals("pv")) //筛选pv操作
      .map(data => ("pv",1))
      .keyBy(_._1)
      .timeWindow(Time.hours(1))
      .apply( new PvCountWindow )

    dataStream.addSink(new PvToRedisSink)

    env.execute("pv count")
  }

}

/**
 * 定义数据输入样例类
 * @param userId  用户ID
 * @param itemId  商品ID
 * @param categoryId  商品类别ID
 * @param behavior  用户行为，包括(pv,buy,cart,fav)
 * @param timestamp 事件时间戳
 */
case class UserBehavior( userId: Long, itemId: Long, categoryId: Int, behavior: String, timestamp: Long)