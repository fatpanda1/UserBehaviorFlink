package windowFunctionUtils

import java.text.SimpleDateFormat

import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class PvCountWindow extends WindowFunction[(String,Int),(String,Int),String,TimeWindow]{
  override def apply(key: String, window: TimeWindow, input: Iterable[(String, Int)], out: Collector[(String, Int)]): Unit = {
    val count = input.size
    val format = new SimpleDateFormat("HH:mm:ss")
    val date = format.format(window.getEnd)
    out.collect(date,count)
  }
}
