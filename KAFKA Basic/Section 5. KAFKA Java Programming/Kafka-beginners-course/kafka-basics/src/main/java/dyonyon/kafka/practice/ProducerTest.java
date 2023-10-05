package dyonyon.kafka.practice;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerTest {
    private static final Logger log = LoggerFactory.getLogger(ProducerTest.class.getSimpleName());
    public static void main(String[] args) {
        log.info("Main Started!");

        // 1. Producer Properties 설정
        Properties properties = new Properties();

        // Connect to My localHost
        properties.setProperty("bootstrap.servers","localhost:9092"); // localHost

        // Connect to Conduktor Playground
//        properties.setProperty("security.protocol","SASL_SSL");
//        properties.setProperty("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username=\"NJaqsEhILC2COy5n52dmb\" password=\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2F1dGguY29uZHVrdG9yLmlvIiwic291cmNlQXBwbGljYXRpb24iOiJhZG1pbiIsInVzZXJNYWlsIjpudWxsLCJwYXlsb2FkIjp7InZhbGlkRm9yVXNlcm5hbWUiOiJOSmFxc0VoSUxDMkNPeTVuNTJkbWIiLCJvcmdhbml6YXRpb25JZCI6NzY5MDUsInVzZXJJZCI6ODk0NzksImZvckV4cGlyYXRpb25DaGVjayI6ImVjODAzYzBlLTEyNGMtNDA1ZC1hYjJjLTA1MDEzODQ0NDFmZSJ9fQ.Rd0oZ7-6fcmw0-Q39tTJaNW9C1pb2EIOakQGcPaE4uA\";");
//        properties.setProperty("sasl.mechanism","PLAIN");
//        properties.setProperty("bootstrap.server","cluster.playground.cdkt.io:9092");

        // Serializer 설정
        properties.setProperty("key.serializer",StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());

        // 2. Create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // 3. Create a Producer Record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("Test_Topic","Hello Dyonyon!!");

        // 4. Send Data
        producer.send(producerRecord);

        // 5. Flush and Close the Producer
        producer.flush(); // tell teh producer to send all data and block until done -- sy
        producer.close();

        log.info("Main Finished!");

    }
}
