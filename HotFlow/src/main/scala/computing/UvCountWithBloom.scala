xm�Ko�0�{�WXW�	U	xH���@ϑ����F��_��⺚��f�	y����9��]
T!��GJ@�x*��~9��dO����N�)�*P�(�CHgZq�3�y3TG�Y͍��p�N����CH��@	����m[���ҥbQ�u��E&\�RH(�F#EiZ��Kťu��m��Z����=�s��Б4rv}X�P�K��o�hz��dO�H�2�F��
dHW�u��cJ�yG���������O��W������5�5�jgvt���[�	��o��%N~�E��                                                                                                                                                                                                                        text: Context, elements: Iterable[(String, Long)], out: Collector[UvCount]): Unit = {
    //位图的存储方式，Redis 数据结构： key是windowEnd，value是bitmap
    val storeKey = context.window.getEnd.toString
    var count = 0l
    //把每个窗口的uv count值也存入redis,所以要先从redis中读取
    //存放内容为key:UvCount field:(windowEnd -> uvCount)
    val redisCount = jedis.hget("UvCount", storeKey)
    if( redisCount != null ) {
      count = redisCount.toLong
    }
    //用布隆过滤器当前用户是否已经存在
    val userId = elements.last._2.toString
    val offset = bloom.hash(userId, 61)
    //定义一个标志位，判断redis位图中有没有这一位
    val isExit = jedis.getbit(storeKey, offset)
    if (!isExit){
      //如果不存在，位图对应位置置1(count)，count + 1
      jedis.setbit(storeKey,offset,true)
      jedis.hset("UvCount",storeKey,(count + 1).toString)
      //返回
      out.collect(UvCount(storeKey.toLong, count + 1))
    } else {
      out.collect(UvCount(storeKey.toLong, count))
    }

  }
}
