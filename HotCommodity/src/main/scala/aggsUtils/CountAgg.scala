package aggsUtils

import computing.UserBehavior
import org.apache.flink.api.common.functions.AggregateFunction

//输入类型，中间聚合状态类型，输出的类型
class CountAgg extends AggregateFunction[UserBehavior, Long, Long] {
  //初值
  override def createAccumulator(): Long = 0l

  //每来一条数据怎样累加
  override def add(in: UserBehavior, acc: Long): Long = acc + 1

  //输出结果，累加器的值
  override def getResult(acc: Long): Long = acc

  //如果遇到重分区，两个累加器怎样处理
  override def merge(acc: Long, acc1: Long): Long = acc + acc1
}

//自定义聚合函数计算平均数
class AveraggAgg() extends AggregateFunction[UserBehavior, (Long, Int), Double]{
  override def createAccumulator(): (Long, Int) = (0l,0)

  override def add(in: UserBehavior, acc: (Long, Int)): (Long, Int) = (acc._1 + in.timestamp, acc._2 + 1)

  override def getResult(acc: (Long, Int)): Double = acc._1/acc._2


  override def merge(acc: (Long, Int), acc1: (Long, Int)): (Long, Int) = (acc._1 + acc1._1, acc._2 + acc1._2)
}