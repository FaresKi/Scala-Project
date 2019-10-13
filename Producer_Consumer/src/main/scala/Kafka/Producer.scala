package Kafka

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.Json

object Producer {
  def main(args: Array[String]): Unit = {
    writeToKafka("topic_historique")
  }
  def writeToKafka(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    implicit val messageWrites = Json.writes[Message]
    val bufferedSource = scala.io.Source.fromFile("history.csv").getLines().foreach(line=> {
      val cols = line.split(",").map(_.trim)
      val message = Message(lieu = s"${cols(23)} "+s"${cols(24)} "+ s"${cols(25)} ", date = s"${cols(4)}" + s"${cols(19)}", idDrone = s"${cols(14)}", plaque = s"${cols(1)}", infractionCode = s"${cols(5)}")
      val messageJson: String = Json.toJson(message).toString()
      val record = new ProducerRecord[String, String](topic, messageJson)
      producer.send(record)
    }
    )

    //val messageJson: String = Json.toJson(message).toString()
    //val record = new ProducerRecord[String, String](topic, "key", messageJson)
    //producer.send(record)
    producer.close()
  }

}
