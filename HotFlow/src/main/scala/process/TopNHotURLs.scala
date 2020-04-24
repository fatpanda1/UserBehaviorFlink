package process

import java.sql.Timestamp

import computing.UrlViewCount
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

class TopNHotURLs(topSize: Int) extends KeyedProcessFunction[Long, UrlViewCount, String]{
  lazy val urlState: ListState[UrlViewCount] = getRuntimeContext.getListState( new ListStateDescriptor[UrlViewCount]("url-state",classOf[UrlViewCount]) )

  override def processElement(i: UrlViewCount, context: KeyedProcessFunction[Long, UrlViewCount, String]#Context, collector: Collector[String]): Unit = {
    //首先将数据保存在状态中
    urlState.add(i)
    context.timerService().registerEventTimeTimer(i.windowEnd + 1)
  }

  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, UrlViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
    //从状态中取出数据
    val allUrlViews: ListBuffer[UrlViewCount] = new ListBuffer[UrlViewCount]
    //构建迭代器
    val iter = urlState.get().iterator()
    //遍历取数据
    while (iter.hasNext){
      allUrlViews += iter.next()
    }

    //排序
    val sortedUrlViews = allUrlViews.sortWith(_.count > _.count).take(topSize)

    //格式化输出结果
    val result: StringBuilder = new StringBuilder
    result.append("时间:").append( new Timestamp(timestamp - 1) ).append("\n")
    for ( i <- sortedUrlViews.indices ){
      val currentUrlView = sortedUrlViews(i)
      result.append("NO").append(i + 1).append(":")
        .append(" URL=").append(currentUrlView.url)
        .append(" 访问量=").append(currentUrlView.count).append("\n")
    }
    result.append("***********************")
    Thread.sleep(1000)
    out.collect(result.toString())
  }
}
