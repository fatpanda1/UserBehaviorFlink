package sink

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import redis.clients.jedis.Jedis

class PvToRedisSink extends RichSinkFunction[(String,Int)]{

  private var jedis: Jedis = null

  override def open(parameters: Configuration): Unit = {
    jedis = RedisUtils.getJedisObject
  }

  /**
   * invoke方法是sink数据处理逻辑的方法，source端传来的数据都在invoke方法中进行处理
   * 其中invoke方法中第一个参数类型与RichSinkFunction<String>中的泛型对应。第二个参数
   * 为一些上下文信息
   */
  override def invoke(value: (String,Int), context: SinkFunction.Context[_]): Unit = {
    jedis.hset("pvCount1",value._1.toString,value._2.toString)
  }

  override def close(): Unit = {
    jedis.close()
  }
}
