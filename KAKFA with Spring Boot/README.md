- Section 1~6 : KAFKA 기본 내용 + CLI
- Section 7 : Build Spring boot KAFKA Producer


- Section 1~6 : KAFKA 기본 내용 + CLI
  - S/W 개발
    - (Past) : Monolith Architecture. 모든 서비스가 하나의 큰 Application안에 존재하고 DB를 공유하여 사용 => 과부하되면 실패
    - (Current) : Microservice Architecture. 각 서비스가 micro service로 분리되어 있고, DB가 따로 있다. micro service들이 communication Protocol을 통해 interact함 => 통신이 스파게티처럼 얽혀있음 => 그래서 발전된 오늘날의 MicroServices Architecture은 API와 Event Streaming Platform(Middleware)을 두어 interact하는 형태
      - Event Streaming Platform은 application이 stream of record를 produce하고 consume하게함(like a messaging system). Stream of Events도 
  - Traditional Messaging System VS KAFKA Streaming Platform
    - Traditional Messaging System
      - 메시지 지속성이 없음(일시적임 = 한번 읽으면 제거함)
      - 브로커에게 메시지 추적 책임이 있음
      - 특정한 컨슈머를 지정
      - 분산 시스템이 아님
    - KAFKA Streaming Paltform
      - retention 시간만큼 이벤트(데이터)가 저장되며, immutable하다(한번 KAFKA로 전송하면 수정 불가)
      - 컨슈머에게 메시지 추적 책임이 있음
      - 어느 컨슈머나 메시지를 access할 수 있음
      - 분산 시스템이다
  - KAFKA의 기본적인 내용은 스킵(자세한 정리는 KAFKA Basic 참고)
- Section 7 : Build Spring boot KAFKA Producer
  - 프로젝트 세팅 : Spring initializr -> Gradle-Groovy project -> web & Spring for Apache Kafka & Validation & Lombok(Logging을 위해) 의존성 추가
  - KAFKA Template : 스프링 라이브러리. Produce records in a KAFKA Topic
    - send 절차 : Send() -> Serializer() -> Partitioner -> Record Accumulator (버퍼에 모인 뒤 KAFKA Topic에 전송됨) -> 토픽 전송
      - Record Accumulator에 파티션 개수만큼의 Record Batch가 있고, 각 Batch가 batch.size만큼 채워진 후에 토픽으로 전달된다.
      - linger.ms를 통해서 버퍼를 다 채우지 않고, linger.ms만큼 데이터가 대기한 뒤 전송될 것임
    - configuring KAFKA Template:
      - bootstrap-servers, key-serializer, value-serializer
  - KAFKA Admin : 스프링 카프카 라이브러리. 토픽을 생성
    - How to create a topic from Code?
      - Create a Bean of type KafkaAdmin/NewTopic in Spring Configuration

  - KAFKA Topic 자동 생성: 
    - ex) @Configuration
          public class AutoCreateConfig {

              @Value("${spring.kafka.topic}")
              public String topic;

              @Bean
              public NewTopic libraryEvents(){
                  return TopicBuilder.name(topic).partitions(3).replicas(1).build();
              }
          }
  - KAFKA Producer:
    - ex) controller.java 예시
          @RestController
          @Slf4j
          public class LibraryEventsController {

              private final LibraryEventsProducer libraryEventsProducer;

              public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
                  this.libraryEventsProducer = libraryEventsProducer;
              }

              @PostMapping("/v1/libraryevent")
              public ResponseEntity<LibraryEvent> postLibraryEvent(
                      @RequestBody LibraryEvent libraryEvent
              ) throws JsonProcessingException {
          //        log.info("libraryEvenet : {}",libraryEvent);
                  //invoke the kafka producer
                  libraryEventsProducer.sendLibraryEvent(libraryEvent);

                  return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
              }
          }
    - ex) producer.java 예시
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