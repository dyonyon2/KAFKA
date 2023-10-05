
package dyonyon.kafka.practice;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerTestWithKeys {
    private static final Logger log = LoggerFactory.getLogger(ProducerTestWithKeys.class.getSimpleName());
    public static void main(String[] args) throws InterruptedException {
        log.info("Main Started!");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092"); // localHost
        properties.setProperty("key.serializer",StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i=0;i<2;i++) {
            for (int j = 0; j < 10; j++) {
                String topic = "Test_Topic";
                String key = "id_" + j;
                String value = "hello Dyonyon " + j;

                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);

                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        // executes every time a record successfully sent or an exception is thrown
                        if (e == null) {
                            // the record was successfully sent
                            log.info("Key: " + key + " | " +
                                    "Partition: " + metadata.partition());
                        } else {
                            log.error("Error while Producing", e);
                        }
                    }
                });
            }
        }

        producer.flush();
        producer.close();

        log.info("Main Finished!");

    }
}
