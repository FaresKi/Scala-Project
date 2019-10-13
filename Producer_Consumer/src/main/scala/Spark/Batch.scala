package Spark

import java.util.Properties

import com.datastax.driver.core.Cluster
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Batch {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("Batch Processing").setMaster("local[*]")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.auth.username", "cassandra")
      .set("spark.cassandra.auth.password", "cassandra")
    val streamingContext = new StreamingContext(sparkConf, Seconds(5))
    streamingContext.sparkContext.setLogLevel("ERROR")
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092,localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "group_1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "zookeeper.consumer.connection" -> "localhost:2181",
      "kafka.consumer.id" -> "kafka-consumer"
    )

    val props = new Properties()
    kafkaParams foreach { case (key, value) => props.put(key, value) }
    val topics = Array("topic_producer", "topic_historique")
    val topicsStatus = Array("topic_health")
    val streamProducer = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    val streamDrones = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topicsStatus, kafkaParams)
    )

    val cassandra_host = sparkConf.get("spark.cassandra.connection.host")
    val cassandra_user = sparkConf.get("spark.cassandra.auth.username")
    val cassandra_pass = sparkConf.get("spark.cassandra.auth.password")
    val clusterDrones = Cluster.builder().addContactPoint(cassandra_host).withCredentials(cassandra_user, cassandra_pass).build()
    val clusterHealth = Cluster.builder().addContactPoint(cassandra_host).withCredentials(cassandra_user, cassandra_pass).build()
    val session = clusterDrones.connect()

    streamProducer.foreachRDD(rdd => {
      rdd.foreach(x =>
        CassandraConnector(sparkConf).withSessionDo { session =>
          session.execute("INSERT INTO scala_project.nypd_data JSON " + "'" + x.value().toString + "';")
        }

      )
    }
    )
    session.close()

    val sessionHealth = clusterHealth.connect()
    streamDrones.foreachRDD(rdd => {
      rdd.foreach(x =>
        CassandraConnector(sparkConf).withSessionDo { sessionHealth =>
          sessionHealth.execute("INSERT INTO scala_project.drone_messages JSON " + "'" + x.value().toString + "';")
        }

      )
    })
    sessionHealth.close()


    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
