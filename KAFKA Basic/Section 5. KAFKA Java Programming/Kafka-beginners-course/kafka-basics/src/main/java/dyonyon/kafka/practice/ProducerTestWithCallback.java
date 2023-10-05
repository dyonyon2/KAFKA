package dyonyon.kafka.practice;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerTestWithCallback {
    private static final Logger log = LoggerFactory.getLogger(ProducerTestWithCallback.class.getSimpleName());
    public static void main(String[] args) throws InterruptedException {
        log.info("Main Started!");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092"); // localHost
        properties.setProperty("key.serializer",StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());

        properties.setProperty("batch.size","400");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);


        for (int i=0;i<10;i++) {
            for(int j=0;j<30;j++) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>("Test_Topic", "Hello Dyonyon!!" + j);

                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        // executes every time a record successfully sent or an exception is thrown
                        if (e == null) {
                            // the record was successfully sent
                            log.info("Received new metadata \n" +
                                    "Topic: " + metadata.topic() + "\n" +
                                    "Partition: " + metadata.partition() + "\n" +
                                    "offset: " + metadata.offset() + "\n" +
                                    "timestamp: " + metadata.timestamp());
                        } else {
                            log.error("Error while Producing", e);
                        }
                    }
                });
            }
            Thread.sleep(500);
        }

        producer.flush();
        producer.close();

        log.info("Main Finished!");

    }
}
