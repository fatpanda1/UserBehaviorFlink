package producer

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.io.Source

object DataProducer {

  private val props=new Properties()
  props.setProperty("bootstrap.servers", "120.55.43.230:9092")
  props.setProperty("group.id", "HotCommodity_Producer")
  props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.setProperty("auto.offset.reset", "latest")

  private val topic = "Flink_UserBehavior"

  def main(args: Array[String]): Unit = {
    val file = Source.fromFile("D:\\learning\\bigData\\flinkUserData\\UserBehavior.csv")

    val producer = new KafkaProducer[String,String](props)

    for (line <- file.getLines()){
      val rcd = new ProducerRecord[String,String](topic,line)
      producer.send(rcd)
    }

    producer.close()
  }
}
