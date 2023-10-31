Abstract
- Section 1~6 : KAFKA 기본 내용 + CLI
- Section 7 : Build Spring boot KAFKA Producer
- Section 8~9 : Unit Testing using JUnit5


Content
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
  - KAFKA Producer: 3가지 방식 (동기, 비동기, Record)
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
    - ex) producer.java 비동기 예시
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
      - *** 위 예제에서 completableFuture.whenComplete가 비동기로 진행됨
        1. Blocking call - get metadata about the kafka cluster
        2. Send Message happens - return a CompletableFuture
    - ex) producer.java 동기 예시
      public SendResult<Integer, String> sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
          var key= libraryEvent.libraryEventId();
          var value = objectMapper.writeValueAsString(libraryEvent);
          var sendResult = kafkaTemplate.send(topic, key, value).get();
          log.info("in sendlibrary event after template send&get");
          handleSuccess(key,value,sendResult);
          return sendResult;
      }
      - kafkaTemplate.send(topic, key, value)에 get()을 붙히면 Result를 받을 때까지 기다림 (block, 동기)
    - ex) producer.java Record 예시
      public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent_approach3(LibraryEvent libraryEvent) throws JsonProcessingException {
          var key= libraryEvent.libraryEventId();
          var value = objectMapper.writeValueAsString(libraryEvent);

          var producerRecord = buildProducerRecord(key,value);

          var completableFuture = kafkaTemplate.send(producerRecord);
          log.info("in sendlibrary event after template send");

          return completableFuture.whenComplete((sendResult, throwable) -> {
              if(throwable!=null){
                  handleFauilure(key, value, throwable);
              } else{
                  handleSuccess(key, value, sendResult);
              }
          });
      }
      *** Record에 RecordHeader 추가할 수 있음
      private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
          List<Header> recordHeaders = List.of(new RecordHeader("event-source","scanner".getBytes()));
          return new ProducerRecord<>(topic, null, key, value, recordHeaders);
      }

- Section 8~9 : Integration/Unit Testing using JUnit5
  - Automated Test
    - Why Automated Tests?
      - Manual testing is time consuming
      - Manual testing slows down the development
      - Adding new changes are error prone
    - What are Automated Tests?
      - Automated tests are something that runs against your code base.
      - Automated test are run as part of your build.
      - Easy to capture bugs
      - 종류 (UnitTest, Integration Test, End to End Test)
      - Tools (JUnit, Spock)
  - Integration Test
    - Test combines all the independent layers of your code and make sure that they work as expected in collaboration.
    - ex) @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
          class LibraryEventsControllerIntegrationTest {

              @Autowired
              TestRestTemplate testRestTemplate;
              @Test
              void postLibraryEvent() {
                  HttpHeaders httpHeaders = new HttpHeaders();
                  httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
                  var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(),httpHeaders);

                  var responseEntity = testRestTemplate.exchange("/v1/libraryevent", HttpMethod.POST, httpEntity, LibraryEvent.class);

                  assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

              }
          }
      - KAFKA를 실제 KAFKA로 테스트를 하지 못하는 경우, EmbeddedKafka사용!
    - ex) EmbeddedKafka를 위한 annotation 추가
      @EmbeddedKafka(topics = "library-events")
      @TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
              "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
  - Unit Test
    - Test the just focuses on a single unit(method)
    - Mocks the external dependencies
    - ex) @WebMvcTest(LibraryEventsController.class)
          class LibraryEventsControllerUnitTest {

              @Autowired
              MockMvc mockMvc;

              @Autowired
              ObjectMapper objectMapper;

              @MockBean
              LibraryEventsProducer libraryEventsProducer;

              @Test
              void postLibraryEvent() throws Exception {
                  var json = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());
                  when(libraryEventsProducer.sendLibraryEvent_approach3(isA(LibraryEvent.class))).thenReturn(null);

                  mockMvc.perform(MockMvcRequestBuilders.post("/v1/libraryevent").content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
              }
          }
  