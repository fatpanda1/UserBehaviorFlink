package aggsUtils

import java.sql.Timestamp

import computing.ItemViewCount
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

//根据每个窗口分组统计topN
class TopNHotItems(topN: Int) extends KeyedProcessFunction[Long, ItemViewCount, String]{

  private var itemState: ListState[ItemViewCount] = _

  override def open(parameters: Configuration): Unit = {
    itemState = getRuntimeContext.getListState( new ListStateDescriptor[ItemViewCount]("item-state",classOf[ItemViewCount]) )
  }

  override def processElement(i: ItemViewCount, context: KeyedProcessFunction[Long, ItemViewCount, String]#Context, collector: Collector[String]): Unit = {
    //每来一条数据首先将其存入状态
    itemState.add(i)
    //注册一个定时器
    context.timerService().registerEventTimeTimer(i.windowEnd + 100)
  }

  //定时器触发时，对所有数据排序，并输出结果
  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, ItemViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
    //将所有state中的数据取出，放到一个list buffer中
    val allItems: ListBuffer[ItemViewCount] = new ListBuffer[ItemViewCount]
    //遍历state需要引入隐式转换
    import scala.collection.JavaConversions._
    for (item <- itemState.get()){
      //将所有state存入listBuffer中
      allItems += item
    }

    //按照count大小进行排序
    //函数函数柯里化将升序排序改为降序排序
    //take取出前N个
    val sortItems = allItems.sortBy(_.count)(Ordering.Long.reverse).take(topN)

    //将排名结果格式化输出
    val result: StringBuilder = new StringBuilder
    //注册定时器时间+100，这里需要回调
    result.append("time:").append(new Timestamp(timestamp - 100)).append("\n")
    //输出每一个商品的信息
    //indices直接获取下标
    for (i <- sortItems.indices){
      val currentItem = sortItems(i)
      result.append("No").append(i + 1).append(":")
        .append("商品id=").append(currentItem.itemId)
        .append("浏览量=").append(currentItem.count)
        .append("\n")
    }
    result.append("*****************")
    Thread.sleep(1000)

    //输出stringbuilder
    out.collect(result.toString())

    //清空状态释放内存空间
    itemState.clear()
  }
}