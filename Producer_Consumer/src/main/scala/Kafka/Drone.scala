import java.util.{Calendar, Properties}
import Kafka.{Message, MessageStatus}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.Json
import scala.util.Random

object Drone {
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)
  val producerHealth = new KafkaProducer[String, String](props)

  def main(args: Array[String]): Unit = {
    val listTopics = List("topic_producer", "topic_health")
    writeToKafka(listTopics, 100)
  }

  def writeToKafka(topic: List[String], batteryLife: Double): Unit = {
    implicit val messageWrites = Json.writes[Message]
    implicit val messageStatusWrites = Json.writes[MessageStatus]
    val listPlaces = List("Central Park", "Times Square", "Empire State Building", "Statue of liberty", "Brooklyn bridge", "Rockefeller Center")
    val message = Message(lieu = s"${listPlaces(Random.nextInt(listPlaces.size))}", date = s"${Calendar.getInstance().getTime()}", idDrone = s"${Random.nextInt(200)}", plaque = s"${Random.alphanumeric.take(5).mkString("").toUpperCase}", infractionCode = s"${Random.nextInt(50)}")
    val messageStatus = MessageStatus(idDrone = s"${Random.nextInt(200)}", status = s"${Random.nextInt(2)}")
    val messageJson: String = Json.toJson(message).toString()
    val messageStatusJson: String = Json.toJson(messageStatus).toString()
    val record = new ProducerRecord[String, String](topic(0), messageJson)
    val record_status = new ProducerRecord[String, String](topic(1), messageStatusJson)
    while (batteryLife != 0) {
      producer.send(record)
      producerHealth.send(record_status)
      val tmp = 0.99 * batteryLife
      writeToKafka(topic, tmp)
    }
    producer.close()
    producerHealth.close();

  }
}
