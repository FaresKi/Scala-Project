package Spark

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}

object StreamingHealth {
  val sparkConf = new SparkConf()
    .setAppName("Structured-Streaming Health Processing")
    .setMaster("local[*]")
    .set("spark.cassandra.connection.host", "localhost")
    .set("spark.cassandra.auth.username", "cassandra")
    .set("spark.cassandra.auth.password", "cassandra")

  val sparkContext = new SparkContext(sparkConf)

  val sparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("Structured-Streaming Processing")
    .config(sparkConf)
    .getOrCreate()


  def main(args: Array[String]): Unit = {
    val df = sparkSession
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092,localhost:9092")
      .option("subscribe", "topic_health")
      .load()

    val messageJsonDF = df.selectExpr("CAST(value AS STRING)")
    messageJsonDF.printSchema()

    messageJsonDF
      .writeStream
      .foreach(new ForeachWriter[Row] {
        override def open(partitionId: Long, epochId: Long): Boolean = {
          true
        }

        override def process(value: Row): Unit = {
          CassandraConnector(sparkConf).withSessionDo { session =>
            session.execute("INSERT INTO scala_project.drone_messages JSON " + "'" + value.toString().substring(1, value.toString().length - 1) + "';")
          }
        }

        override def close(errorOrNull: Throwable): Unit = {
        }
      })
      .start()
      .awaitTermination()
  }


}
