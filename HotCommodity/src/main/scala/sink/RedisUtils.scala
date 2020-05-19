package sink

import redis.clients.jedis.Jedis

object RedisUtils {
  val host: String = "127.0.0.1"
  val port: Int = 6379

  def getJedisObject = {
    val jedis: Jedis = new Jedis(host,port)
    jedis
  }
}
