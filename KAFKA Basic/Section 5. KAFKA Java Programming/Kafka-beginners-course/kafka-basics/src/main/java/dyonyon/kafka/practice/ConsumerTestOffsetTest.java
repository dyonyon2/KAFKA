package dyonyon.kafka.practice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerTestOffsetTest {
    private static final Logger log = LoggerFactory.getLogger(ConsumerTestOffsetTest.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Main Consumer Started!");
        String groupId = "my-java-application";
        String topic = "Test_Topic";

        // 1. Create Consumer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer",StringDeserializer.class.getName());
        properties.setProperty("group.id",groupId);
        properties.setProperty("auto.offset.reset","earliest");// none / earliest / latest

        // 2. Create a Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // 3. Subscribe to a Topic
        consumer.subscribe(Arrays.asList(topic));

        // 4. Poll for Data
            log.info("Polling!");

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for(ConsumerRecord<String, String> record : records){
                log.info("key : "+record.key()+", value : "+record.value());
                log.info("Partition : "+record.partition()+", Offset : "+record.offset());
            }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ConsumerRecords<String, String> records2 = consumer.poll(Duration.ofMillis(1000));

//        log.info("Main Consumer Finished!");

    }
}
