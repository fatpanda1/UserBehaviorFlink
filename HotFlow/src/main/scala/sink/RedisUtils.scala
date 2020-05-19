package sink

import redis.clients.jedis.Jedis

object RedisUtils {
  val host: String = "120.55.43.230"
  val port: Int = 6379

  def getJedisObject = {
    val jedis: Jedis = new Jedis(host,port)
    jedis
  }
}
