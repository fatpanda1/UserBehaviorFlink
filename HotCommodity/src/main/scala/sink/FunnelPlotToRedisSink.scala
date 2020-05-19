package sink

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import redis.clients.jedis.Jedis

class FunnelPlotToRedisSink extends RichSinkFunction[(String,Int)]{

  private var jedis: Jedis = null

  override def open(parameters: Configuration): Unit = {
    jedis = RedisUtils.getJedisObject
  }

  override def invoke(value: (String, Int), context: SinkFunction.Context[_]): Unit = {
    jedis.hset("FunnelPlotCount",value._1,value._2.toString)
  }

  override def close(): Unit = {
    jedis.close()
  }
}
