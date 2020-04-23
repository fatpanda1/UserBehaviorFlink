package aggsUtils

import computing.ItemViewCount
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


/**
 * 自定义窗口函数，需要将数据包装成自定义样例类进行输出
 * 输入类型为预聚合函数的输出类型
 */
class WindowResult extends WindowFunction[Long, ItemViewCount, Long, TimeWindow]{
  override def apply(key: Long, window: TimeWindow, input: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
    /**
     * 输出，获取key，windowend,count的值
     */
    out.collect(ItemViewCount(key, window.getEnd, input.iterator.next()))
  }
}
