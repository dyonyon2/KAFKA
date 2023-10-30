package com.learnkafka.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsProducer {

    @Value("${spring.kafka.topic}")
    public String topic;

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key= libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        var completableFuture = kafkaTemplate.send(topic, key, value);
        completableFuture.whenComplete((sendResult, throwable) -> {
            if(throwable!=null){
                handleFauilure(key, value, throwable);
            } else{
                handleSuccess(key, value, sendResult);
            }
        });
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message sent successfully for the key : [} and the value : {}, partition is {}", key, value, sendResult.getRecordMetadata().partition());
    }


    private void handleFauilure(Integer key, String value, Throwable ex) {
        log.error("Error sending the message and the exception is {}", ex.getMessage(),ex);
    }
}
