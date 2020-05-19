package computing

import java.text.SimpleDateFormat

import aggUtils.{CountAgg, WindowResult}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import process.TopNHotURLs

object HotFlowMain {
  def main(args: Array[String]): Unit = {
    //获取流处理运行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    //读取数据源(测试读入本地数据)
    /**
     * 数据格式
     * item0:46.105.14.53
     * item1:-
     * item2:-
     * item3:20/05/2015:21:05:15
     * item4:+0000
     * item5:GET
     * item6:/blog/tags/puppet?flav=rss20
     */
    val resource = getClass.getResource("/UserBehavior.csv")
    val inputStream = env.readTextFile(resource.getPath)

    inputStream.map(data => {
      val dataArray = data.split(" ")
      //切分数据行获取需要字段
      val ip = dataArray(0).trim
      val time = dataArray(3).trim
      val method = dataArray(5).trim
      val url = dataArray(6).trim
      //将时间转为时间戳
      val dateFormat = new SimpleDateFormat("dd/MM/yy:HH:mm:ss")
      val timeStamp = dateFormat.parse(time).getTime

      //将数据以样例类WebLog返回
      WebLog(ip, timeStamp, method, url)
    })
      //乱序数据，所以需要指定watermark,watermark延迟为1秒
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[WebLog](Time.seconds(1)) {
        override def extractTimestamp(t: WebLog): Long = t.time
      })
      .keyBy(_.url)
      .timeWindow(Time.minutes(10), Time.seconds(5))
      //允许60秒的迟到数据
      .allowedLateness(Time.seconds(60))
      .aggregate(new CountAgg, new WindowResult())
      .keyBy(_.windowEnd)
      .process(new TopNHotURLs(5))
      .print()

    env.execute("HotFlowAnalysis")
  }
}

case class WebLog(ip: String, time: Long, method: String, url: String)

//窗口聚合后输出的样例类
case class UrlViewCount(url: String, windowEnd: Long, count: Long)