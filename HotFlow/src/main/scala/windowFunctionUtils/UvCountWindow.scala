package windowFunctionUtils

import computing.{UserBehavior, UvCount}
import org.apache.flink.streaming.api.scala.function.AllWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class UvCountWindow extends AllWindowFunction[UserBehavior, UvCount, TimeWindow]{
  override def apply(window: TimeWindow, input: Iterable[UserBehavior], out: Collector[UvCount]): Unit = {
    //定义一个scala set,用于保存所有的数据userId并去重
    var idSet = Set[Long]()
    //当前窗口所有数据的ID收集到set中，最后输出set的大小
    for ( userBehavior <- input){
      idSet += userBehavior.userId
    }
    out.collect( UvCount( window.getEnd, idSet.size) )

  }
}
