package computing

class BloomUv(size: Long) extends Serializable {
  //位图的总大小 位运算，1左移27位，相当于能够存储2的28次方种情况
  private val cap = if (size > 0) size else 1 << 27

  //定义hash函数
  def hash(value: String, seed: Int): Long = {

    var result: Long = 0l

    //将字符串每一位转为char值进行哈希计算
    for (i <- 0 until value.length) {
      result = result * seed + value.charAt(i)
    }

    //result和指定位图大小进行与运算得出结果
    result & (cap -1)
  }
}
