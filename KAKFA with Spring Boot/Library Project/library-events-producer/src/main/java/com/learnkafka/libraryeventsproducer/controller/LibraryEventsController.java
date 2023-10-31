package com.learnkafka.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.libraryeventsproducer.domain.LibraryEvent;
import com.learnkafka.libraryeventsproducer.producer.LibraryEventsProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    private final LibraryEventsProducer libraryEventsProducer;

    public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
        this.libraryEventsProducer = libraryEventsProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(
            @RequestBody @Valid LibraryEvent libraryEvent
    ) throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("libraryEvenet : {}",libraryEvent);
        //invoke the kafka producer
//        libraryEventsProducer.sendLibraryEvent(libraryEvent);
//        libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
        libraryEventsProducer.sendLibraryEvent_approach3(libraryEvent);
        log.info("libraryEvenet after send call");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
