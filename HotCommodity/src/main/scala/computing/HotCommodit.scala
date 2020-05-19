package computing

import java.util.Properties

import aggsUtils.{CountAgg, TopNHotItems, WindowResult}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.kafka.clients.consumer.KafkaConsumer
//隐式转换
import org.apache.flink.streaming.api.scala._

/**
 * 计算用户热门商品
 */
object HotCommodit {

  /**
   * 配置kafka信息
   */
  private val props = new Properties()
  props.setProperty("bootstrap.servers", "120.55.43.230:9092")
  props.setProperty("group.id", "HotCommodity_Consumer")
  props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.setProperty("auto.offset.reset", "latest")

  private val topic = "Flink_UserBehavior"

  /**
   * 主函数
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {
    //获取流处理执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    //获取数据源
    //    val dataStream = env.addSource(new FlinkKafkaConsumer011[String](
    ////      "Flink_UserBehavior",
    ////      new SimpleStringSchema(),
    ////      props
    ////    ))
    val resource = getClass.getResource("UserBehavior.csv")
    val dataStream = env.readTextFile("E:\\learning\\UserBehaviorFlink\\HotCommodity\\src\\main\\resources\\UserBehavior.csv")
      .map(line => {
        //切分数据行
        val item = line.split(",")
        //提取数据中的字段
        val userId = item(0).trim.toLong
        val itemId = item(1).trim.toLong
        val categoryId = item(2).trim.toInt
        val behavior = item(3).trim
        val timestamp = item(4).trim.toLong
        //返回类型
        UserBehavior(userId, itemId, categoryId, behavior, timestamp)
      })
      .assignAscendingTimestamps(_.timestamp * 1000l)

    //处理数据
    val processedStream = dataStream
      .filter(_.behavior == "pv")
      .keyBy(_.itemId)
      //      设置滑动窗口，窗口大小一小时，滑动5分钟
      .timeWindow(Time.hours(1), Time.minutes(5))
      //      窗口预聚合函数，窗口输出函数
      .aggregate(new CountAgg(), new WindowResult())
      //按照窗口分组
      .keyBy(_.windowEnd)
      .process(new TopNHotItems(5))

    //    Test：控制台输出数据
    processedStream.print()

    //执行任务
    env.execute("Top N job")

  }
}

/**
 * 定义数据输入样例类
 *
 * @param userId     用户ID
 * @param itemId     商品ID
 * @param categoryId 商品类别ID
 * @param behavior   用户行为，包括(pv,buy,cart,fav)
 * @param timestamp  事件时间戳
 */
case class UserBehavior(userId: Long, itemId: Long, categoryId: Int, behavior: String, timestamp: Long)

/**
 * 窗口聚合样例类
 *
 * @param itemId    商品ID
 * @param windowEnd 窗口信息
 * @param count     商品数量
 */
case class ItemViewCount(itemId: Long, windowEnd: Long, count: Long)
