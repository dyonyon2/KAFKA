- Section 1 : 간단한 강의&KAFKA 소개
- Section 2 : KAFKA 이론
- Section 3 : KAFKA 시작하기
- Section 4 : KAFKA CLI
- Section 5 : KAFKA Java Programming
- Section 6 : KAFKA simple wikimedia Project and advanced Producer configuration
- Section 7 : OpenSearch Consumer and advanced Consumer configurations
- Section 8 : KAFKA 확장 API & 실제 사례
- Section 9 : KAFKA 기업 가이드
- Section 10 : 고급 KAFKA 토픽 구성



- Section 1 : 간단한 강의&KAFKA 소개
	- 기존 데이터 전송 방식 : 소스 시스템 -> 타겟 시스템으로 데이터를 전달하는 형태.
		- If 소스-타겟 쌍이 많아지면? : 정보 공유를 위해서는 모든 타겟 시스템에 데이터를 전송해야한다. =>  즉 데이터 통합을 위해 많은 어려움이 생긴다. (프로토콜, 데이터 형식 등이 상이하기 때문) => 소스 서비스의 부담이 증가한다.
	- Source System 예시 : Website Events, Pricing Data, Transactions, User Interaction 등
	- Target System 예시 : Database, Analytics, Email, Audit 등
	- KAFKA란?
		- LinkedIn에서 개발한 오픈소스이다.
		- 분산형이고, 회복탄력성이 있는 아키텍쳐이며, 실패에 내성이 있다.
		- 수평적 scalability에 좋으며, 빠른 처리량, 10ms 보다 작은 latency => 실시간 시스템
		- 사용 사례
			- Messaging System, Activity Tracking, Gather metrics&logs, Stream processing(Streams API), de-coupling of system dependencies, big data technology, Micro services pub/sub

- Section 2 : KAFKA 이론
	- KAFKA Topics & Partitions & Offset
		- Topics
			- 데이터 스트림(sequence of messages)이고, 토픽 이름으로 식별한다.
			- 모든 메시지 형식을 지원한다. (Json, binary, text 등등)
			- Topic을 query할 수는 없고, Producer&Consumer를 통해 데이터를 전송하고 읽는다.
			- Topic은 immutable이다. 즉 파티션에 데이터가 작성된 이후 delete, update불가!
		- Partitions and offsets
			- 토픽은 파티션들로 분할된다. 
			- 분할된 파티션에 메시지들이 쌓일 때, offset이라는 증가하는 고유 식별자를 가진다.
				- 각 파티션마다 다른 Offset을 가지게 된다.
		- ex. truck의 gps 
			- 각각의 트럭이 20초마다 자신의 ID와 위치 정보를 담은 메시지를 카프카에 전송하여 truck_gps 토픽에 전송한다.
			- 토픽이 10개의 파티션으로 구성된다.
			- 트럭의 GPS 정보를 가져가서 사용하는 Consumer들이 존재한다. (Location Dashboard, Notification Service 등등)
			=> 동일한 정보를 여러 서비스들이 consume 할 수 있다.
		- ** 중요한 사항 **
			- 데이터가 파티션에 작성되면 변경될 수 없다.
			- 데이터는 일정 시간만 유지된다. (기본값 1주)
			- Offset은 특정 파티션에만 의미가 있다. (파티션 0의 offset n이 파티션 1의 offset과는 연관이 없다.)
			- 순서보장은 동일 파티션안에서만 보장된다!!!
			- 특정 key가 없을 경우에, 메시지는 파티션들중 임의의 파티션에 배정되어 message가 저장될 것이다.
			- 파티션의 개수는 원하는 만큼 설정할 수 있다. But 알맞는 파티션의 개수가 서비스의 특징, 환경에 따라 달라진다.
 	- Producer & Message Key & serialization
 		- Producer
 			- 토픽에(여러 파티션으로 구성된) 데이터를 write 하는 주체이다.
 			- 프로듀서는 어떤 파티션에 데이터를 작성해야하는지 안다.(Kafka broker가 알려줌)
 			- KAFKA Broker가 고장난 경우에는 Producer가 자동적으로 회복한다.
 			- 매커니즘에 따라 Data를 파티션에 분산해서 전송하기 때문에 Load balancing 된다.
 			- 프로듀서는 Message Key를 정할 수 있다. (String, number, binary 등등..)
 				- If key = null, 데이터는 Round Robin으로 전송된다. (0,1,2,0,1,2...)
 				- If key != null, 키에 따라서 파티션이 정해지며, 동일한 키를 공유하는 모든 메시지는 Hashing 기법에 의해 동일한 파티션에 전송된다.
 				=> Key를 지정할 때, 특정 값(key)에 대한 메시지 순서를 정해야한다.
 					- ex. Truck ID가 Key에 적합하다. 동일한 ID에 대한 순서가 보장되어야 의미있는 데이터가 된다.(GPS)
	 	- KAKFA Message 구성
	 		- Key, Value, Compression Type, Header(option), Partition+Offset, Timestamp 로 구성된다. 
	 			- Compression Type은 none, gzip, snappy, lz4, zstd 등 압축 형식
	 			- Header은 옵션이며, key-value 쌍으로 구성된다.
	 	- KAFKA Message Serializer(직렬화)
	 		- KAFKA는 프로듀서에게 바이트를 입력 받고 컨슈머에게 바이트를 전송한다. But 우리는 바이트 형식으로 Message를 만드는 것이 아니기 때문에, 메시지 직렬화가(데이터나 객체를 바이트로 변) 필요하다.
	 		- 직렬화는 값과 키에만 사용된다.
	 			- ex. key(Int) = 123 => KeySerializer = IntegerSerializer => 01110011(Binary)
	 			- ex. value(String) = "hello world" => ValueSerializer = String Serializer => 00100110101001...(Binary)
	 		- Common Serializers
	 			- String, Int, Float, Avro, Protobuf, etc...
	 	- How Message Key is hashing?
	 		- KAFKA Partitioner이 메시지를 받아서 전송할 파티션을 결정한다.
	 			- ex. 	(1) 프로듀서가 record를 send()
	 					(2) 프로듀서 파티셔너 로직이 record를 확인하여 파티션 할당
			- Key Hashing은 키 - 파티션의 매핑에 사용되며, 기본 KAFKA 파티셔너는 murmur2 알고리즘을 사용하여 Key를 Hashing한다.
				-  Key의 Bytes를 확인하여 murmur2 알고리즘을 통해 파티션을 결정한다.
	- Consumers & Deserialization
		- Consumers
			- 컨슈머는 토픽으로부터 PULL 방식으로 데이터를 읽는다.(KAFKA BROKER에게 데이터를 요청하고 응답으로 데이터를 받는 방식이다.)
			=> 데이터를 컨슈머에게 푸싱하는 것은 KAFKA BROKER가 아닌 PULL Model이다.
			- 컨슈머는 자동적으로 어떤 브로커에게서 데이터를 읽을지 알게되며, 브로커 고장시에 어떻게 회복할지 안다.
			- 데이터는 각 파티션내의 작은 Offset부터 큰 Offset 순서로 데이터를 순서대로 읽는다.
		- Deserialization
			- 컨슈머는 카프카로부터 데이터를(바이트) 읽어서 객체나 데이터로 변환하는 역직렬화가 필요하다. 
				- ex. key = 011101(Binary) => KeyDeserializer = IntegerDeserializer => 123(Key Object)
	 			- ex. value = 1011011..(Binary) => ValueDeserializer = StringDeserializer => "hello world"(Value Object)
 			- Common Deserializers
	 			- String, Int, Float, Avro, Protobuf, etc...
	 	- *** 직렬화/역직렬화시에 데이터 타입이 변경되면 안된다!! => 타입 에러를 야기함 ***
 	- Consumer Groups & Consumer Offset
 		- Consumer Groups
	 		- Consumer들은 Consumer 그룹 형태로 데이터를 읽는다.
	 			- ex. Partition 5, Consumer Group 1, Cosumer 3 
	 			- ex. Cosumer1(P0,P1), Cosumer2(P2,P3), Cosumer3(P4)
	 			=> 컨슈머별로 각각 다른 파티션의 데이터를 읽는다.
			- If 컨슈머 그룹내에 컨슈머 개수가 파티션 개수보다 많다면? => 초과된 개수만큼의 컨슈머는 비활성화된다.
			- IF 하나의 토픽을 여러 컨슈머 그룹이 바라보고 있다면? => 문제 없다. 컨슈머 그룹별로 Offset이 다르니까
			- *** 같은 컨슈머 그룹 내의 하나의 컨슈머는 하나의 파티션을 독점한다. 파티션의 개수가 부족하면 나머지 컨슈머는 비활성화된다. *** 
			- 컨슈머 그룹은 Cosumer property인 group.id에 의해 식별된다
		- Consumer Offset
			- KAFKA는 컨슈머 그룹이 읽고 있는 Offset을 저장한다. => 이 offset이 저장되는 topic이 consumer_offsets 이다.
			- 컨슈머 그룹내의 컨슈머가 데이터를 읽고, 주기적으로 Offset을 커밋하는데, 해당 Offset을 통해 데이터를 어디까지 성공적으로 읽었는지 확인한다. => 컨슈머 장애 해결
			- Java Consumer는 자동적으로 at least once로 offset을 커밋한다.
			- 커밋 방식에는 3가지 방법이 존재함
				- At least once(usually preferred)
					- 메세지가 처리된 뒤에 커밋되는 방식. 메시지 처리중 문제발생시 다시 읽음
					- 이것은 메시지의 중복처리가 될 수 있다는 것이다.
					=> KAFKA를 도입하는 시스템이 메시지의 재처리시 문제가 되는 시스템인지 확인해야함
				- At most once
					- 메시지를 받자마자 Offset을 커밋한다. 메시지 처리중 문제발생시 일부 메시지를 잃을 수 있다. 이미 메시지를 읽었다고 커밋하였기 때문.
				- Exactly once
					- Transactional API를 사용하거나, idempotent(멱등, 여러번 적용하더라도 결과가 달라지지 않는 성질 = 재처리해도 문제 없음) 컨슈머를 사용
	- Broker & Topic
		- Broker
			- Broker = server이다. 메시지를 주고받기에 KAFKA에서는 Broker라 칭함.
			- Kafka Cluster는 여러 Broker로 구성되고 Broker는 ID가 있음.
			- 각 Broker에는 특정 토픽 파티션들이 분산되어 저장된다.
			- Client가 어느 단 하나의 Broker(BootStrap Broker라고 부름)에 연결한 뒤에는 클러스터 전체에 연결될 수 있음. (모든 Broker를 알 필요가 없음)
		- Broker & Topic
		    - 데이터는 분산되어 저장된다, 특정 브로커에는 토픽이 없을 수 있음
			- ex) Topic A는 3개 partition, Topic B는 2개의 Partition 존재
			      Broker1(Topic A_0) Broker2(Topic A_2) Broker3(Topic A_1)
			      Broker1(Topic B_1) Broker2(Topic B_0) 
        - Kafka Broker Discovery
            - 모든 Broker는 Bootstrap server로 부를 수 있음. Client가 하나의 브로커에 접속하면, Client은 전체 Cluster에 연결하는 방법을 알 수 있음 => 각 Broker는 모든 Broker, 모든 Topic, 모든 Partition에 대한 MetaData를 가지고 있다!!!
            - 1. Connection + Metadata Request (Client -> Broker)
              2. List of all Brokers (Broker -> Client)
              3. Can connect to the needed brokers (Client -> 특정 Broker)
	    - Topic Replication Factor
	    	- Topic들은 복제 Factor를 가지고, 보통 2~3으로 설정함.
	    	- Broker가 다운되었을 때, 복제를 가지고 있는 다른 Broker가 Data를 대신 제공함
				- ex) Topic A : 2 partition, 2 Replication
				      Broker1(Topic A_0) Broker2(Topic A_1) Broker3(Topic A_1)
				                         Broker2(Topic A_0) 
				      Broker2가 Down되어도 Topic A의 Partition 2개가 다 살아있음
		- Concept of Leader for a Partition
			- 동일 Partition 중 하나의 Broker는 Partition의 Leader로 되어야함
			- Producer는 데이터를 Partition Leader에게만 보낼 수 있음
				- ex) Topic A : 2 partition, 2 Replication, * Leader
				      Broker1(Topic A_0_Leader) Broker2(Topic A_1_Leader) Broker3(Topic A_1_ISR)
				                                Broker2(Topic A_0_ISR) 
	        - *** 각 Partition은 하나의 Leader와 여러 ISR(In Sync Replica)를 가짐
        - Default Producer&Consumer behavior with leaders
         	- Producer는 Partition의 Leader Broker에만 데이터를 작성하며, Consumer도 Leader Broker에게서 데이터를 읽을 수 있음.
     	- Kafka Consumers Replica Fetching (Kafka v2.4+)
     		- Consumer이 가장 가까운 replica(ISR)에서 데이터를 읽을 수 있도록 기능 추가됨
    - Producer Acknowledgements & Topic Durability
    	- Producer ACKs
    		- Producer로 Broker에 데이터를 보낸 뒤, Kafka Broker로부터 데이터가 쓰여졌다고 확인할 수 있음
    			- acks = 0 : Producer가 Broker의 ack를 기다리지 않음 (Data Loss 가능성 있음)
    			- acks = 1 : Producer가 Leader Broker의 ACK를 기다림 (제한된 Data Loss 가능성 있음)
    			- acks = all : Producer가 Leader + all replicas의 ACK를 기다림(Data Loss 없음)
		- Kafka Topic Durability
			- 규칙에 따르면 Replication Factor가 N일 때, N-1개의 Broker가 Down되어도 Data 복구가 가능함
	- Zookeeper
		- KAFKA Broker들을 관리하는 소프트웨어임. (Broker List를 유지함)
		- Partition의 리더 선출을 도움
		- 토픽 생성/삭제, Broker dies/comes up 등의 변화가 일어날 때 Kafka에게 알림
		- 홀수 개의 서버와 함께 동작하고, Zookeeper도 한 개의 Leader와 나머지 Follower들을 가진다.
		- KAFKA Version
			- Kafka v0.10 이후부터 Zookeeper는 Consumer 데이터를 가지지 않는다.(이후에는 KAFKA 내부 consumer_offset에 저장)
			- Kafka 2.x can't work without Zookeeper
			- Kafka 3.x can work without Zookeeper(KIP-500) - using Kafka Raft instead
			- Kafka 4.x will not have zookeeper 
	- Kafka KRaft (KIP-500)
		- Kafka Cluster에 Partition 개수가 100,000개가 스케일링 이슈가 생김
		- Zookeeper 제거로 인해 더 많은 수의 Partition 사용이 가능해지며, 유지보수와 설정도 간편해지고, 안정성과 모니터링, 지원 관리가 쉬워지며, 전체 시스템에 하나의 보안 모델을 사용할 수 있고(KAFKA 보안만 신경쓰면 되므로), KAFKA 시작이 단일 프로세스로 가능해지며 컨트롤러 종료 및 복구 시간도 짧아짐
		- Kafka 3.x 부터 KRaft를 사용할 수 있으며, Production Ready는 Kafka 3.3.1(KIP-833) 부터 가능함
		- Kafka 4.0부터는 KRaft만 사용됨 (Zookeeper X)

- Section 3 : KAFKA 시작하기
	- Window
		- WSL2설치 -> JDK 11 설치 -> KAFKA 설치 -> 주키퍼 실행 -> 카프카 서버(Broker)실행
		  - cmd 창에서 ubuntu 입력하면 WSL(Windows Subsystem for Linux)로 우분투 실행됨
			- ~/kafka_2.13-3.0.0/bin/zookeeper-server-start.sh ~/kafka_2.13-3.0.0/config/zookeeper.properties
		  - ~/kafka_2.13-3.0.0/bin/kafka-server-start.sh ~/kafka_2.13-3.0.0/config/server.properties
		  - 각 properties에서 설정 변경 가능
		- WSK2를 사용하지 않는 방법
			- 토픽 삭제가 불가능하며 KAFKA-8811 에러가 발생함.
			- 1주 이상 사용하면 세그먼트가 삭제되며 KAFKA-1194 에러가 발생할 것임
	- Linux
		- WSL과 동일
	- KRaft 모드 (주기퍼 없는 모드!) 
		- 1.Install Java JDK version 11
      2.Download Apache Kafka v2.8+ from https://kafka.apache.org/downloads under Binary
			3.Extract the contents on Linux
			4.Generate a cluster ID and format the storage using kafka-storage.sh
			5.Start Kafka using the binaries
			6.Setup the $PATH environment variables for easy access to the Kafka binaries
		- kafka-storage.sh random-uuid 로 클러스터 ID를 얻기 -> kafka-storage.sh format -t 클러스터ID -c ~/kafka_2.13-3.0.0/config/kraft/server.properties 로 폴더 포맷 -> kafka-server-start.sh ~/kafka_2.13-3.0.0/config/kraft/server.properties 로 서버 실행

- Section 4 : KAFKA CLI
  - kafka-topics.sh 
  	- KAFKA Topic 관리 (Create, List, Describe, Increase Partition, Delete)
	  	- Create (토픽 생성)
	  	  - kafka-topics.sh --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1
	  	  *** replication-factor는 Broker의 개수보다 같거나 작아야 함 ***
	  	- List (토픽 List)
	  	  - kafka-topics.sh --bootstrap-server localhost:9092 --list 
  	  - Describe (토픽 상세 정보)
  	    - kafka-topics.sh --bootstrap-server localhost:9092 --topic first_topic --describe
	    - Delete (토픽 삭제)
  	    - kafka-topics.sh --bootstrap-server localhost:9092 --topic first_topic --delete
  - kafka-console-producer.sh
  	- Produce without keys
  		- kafka-console-producer.sh --bootstrap-server localhost:9092 --topic first_topic 
  		- kafka-console-producer.sh --bootstrap-server localhost:9092 --topic first_topic --producer-property acks=all
  		- 없는 토픽에 Produce하면. Timeout or 토픽 자동 생성 후 Leader가 없다는 Error -> 토픽 자동 생성 이후 리더 선정되어 토픽 사용 가능 (But 토픽 자동 생성은 하지 않는 것을 권장)
  			- kafka-console-producer.sh --bootstrap-server localhost:9092 --topic new_topic
  		- > config/server.properties에 num.partitions에 자동 생성시 default partition 개수 설정 가능
  	- Produce with keys
  		- kafka-console-producer.sh --bootstrap-server localhost:9092 --topic first_topic --property parse.key=true --property key.separator=:
  		- property 옵션으로 key parsing을 활성화하고 key 구분자를 준 뒤, produce 할 때 키:값 의 형태로 입력하면 키와 값이 parsing되어 Kafka에 produce 됨.
	- kafka-console-consumer.sh
		- consume from tail of the topic
			- Consume 명령 : kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic second_topic
				- produce한 것을 바로 받아감 (Lag 상관없이 그냥 소켓 통신 느낌)
		- consume from the beginning of the topic
			- Consume 명령 :kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic second_topic --from-beginning
				- 순서가 좀 변경되어서 출력되는데, 이는 파티션 별로 나뉘어져서 순서가 섞인것 뿐이지, 파티션 내에서는 순서가 지켜지고 있음
		- show both key and values in the output
			- Consume 명령 : kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic second_topic --formatter kafka.tools.DefaultMessageFormatter --property print.timestamp=true --property print.key=true --property print.value=true --property print.partition=true --from-beginning
				- ex) CreateTime:1696393976895        Partition:1     null    test1
							CreateTime:1696393980106        Partition:1     null    test4
							CreateTime:1696393984140        Partition:1     null    test6
							CreateTime:1696393988673        Partition:1     null    test9
							CreateTime:1696393979060        Partition:0     null    test3
							CreateTime:1696393982891        Partition:0     null    test5
							CreateTime:1696393987324        Partition:0     null    test8
							CreateTime:1696393978088        Partition:2     null    test2
							CreateTime:1696393981182        Partition:2     null    test4
							CreateTime:1696393985378        Partition:2     null    test7
		- Consumer in Group
			- --group 옵션 : kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic fourth_topic --group my-first-application
			  - Partition이 3개인데, 같은 consumer group 내에 Consumer 개수가 1개이면, 해당 Consumer가 모든 Partition의 데이터를 받음.
			  - Partition이 3개, 동일 Consumer group 내에 Consumer 개수가 2개이면, 1개의 Consumer가 Partition 2개를 읽고 나머지 1개 Consumer는 1개의 Partition만 읽는다.
			  - Partition이 3개, 동일 Consumer group 내에 Consumer 개수가 3개이면, 각 Consumer가 Partition 1개를 독점하여 데이터를 읽는다.
			  *** Partition의 개수보다 동일 Consumer Group 내의 Consumer 개수가 크다면, 1대1로 할당된 뒤, 남은 Consumer는 비활성화 된다 ***
			  *** Consumer가 Partition을 할당받는데, Consumer의 개수가 변경되어 Partition의 재할당이 일어나는데, 이를 Rebalance라고 함 ***
			  *** Consumer Group에 속하는 Consumer가 동작하면 Consumer Group의 해당 Topic offset에 따라 미수신한 데이터들도 같이 수신됨 ***
			  *** --from-beginning 옵션은 Consumer Group으로부터 커밋된 Consumer Offset이 없는 경우에만 동작함!! ***
  - kafka-consumer-groups.sh
  	- List Consumer Groups
  		- List 명령어 : kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
  	- Describe One Consumer Group
  		- Describe 명령어 : kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-second-application
  		*** Console Consumer에서 Group ID를 입력하지 않으면 console-consumer라는 consumer group이 새로 만들어 져서 하나의 Consumer Group처럼 동작하고 중지하면 Group 삭제 ***
  	- Delete A Consumer Group
  	- Reset Offsets
  	  - Consumer Group이 inactive일 때만 적용가능(Consumer가 Stop되어야 가능)
  		- --reset-offsets 옵션 : kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --to-earliest --topic third_topic --dry-run
  			- --to-earliest : 토픽에 존재하는 가장 이른 데이터의 offset으로 offset 설정
  			- --dry-run : run하지 않고 결과 미리보기
  			- --execute : 실행

- Section 5 : KAFKA Java Programming
	- KAFKA Client Libraries SDK List : Java, Scala, C/C++, Golang, Python, Javascript/Node.js, .NET/C#, Rust, REST API, Kotlin, Haskell, Ruby
	- Producer
		- Java API - Basics
			- 1. Producer Properties 설정
	        Properties properties = new Properties();
	        properties.setProperty("bootstrap.servers","localhost:9092"); // localHost
	        // Serializer 설정
	        properties.setProperty("key.serializer",StringSerializer.class.getName());
	        properties.setProperty("value.serializer",StringSerializer.class.getName());
	    - 2. Create the Producer
	        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
	    - 3. Create a Producer Record
	        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("Test_Topic","Hello Dyonyon!!");
	    - 4. Send Data
	        producer.send(producerRecord);
	    - 5. Flush and Close the Producer
	    		// tell teh producer to send all data and block until done -- sy
	        producer.flush(); 
	        producer.close();
    - Java API - Callbacks
    	- producer.send(데이터, 콜백함수) 를 통해 metadata, error를 받아서 확인할 수 있음.
    	  - producer.send(producerRecord, new Callback() {
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
      - 기본 Partitional.class는 null이며, default는 스티키 파티셔너(Sticky Partitioner)이다.
        - Sticky Partitioner : 각 데이터마다 따로 보내는 것이 아니라, 빠른 시간내에 들어온 데이터들은 batching하여 하나로 묶어서 보내는 방식
    - Java API - keys
    	- ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
	- Consumer
		- Java API - Basics
			- 1. Create Consumer Properties
			```
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer",StringDeserializer.class.getName());
        properties.setProperty("group.id",groupId);
        properties.setProperty("auto.offset.reset","earliest"); // none / earliest / latest
		``
			- 2. Create a Consumer
			```
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
		```
      - 3. Subscribe to a Topic
	  ```
        consumer.subscribe(Arrays.asList(topic));
		```
			- 4. Poll for Data
			```
        while(true){
          log.info("Polling!");
          ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
          for(ConsumerRecord<String, String> record : records){
            log.info("key : "+record.key()+", value : "+record.value());
            log.info("Partition : "+record.partition()+", Offset : "+record.offset());
          }
        }
		```
			- 5. Shutdown Consumer ( hook 등록 -> while 동작 -> 종료 누르기 -> hooking에 걸림 -> consumer wakeup() -> catch 문에 WakeupException에 걸림 -> finally에서 consumer 종료 -> main 종료 -> hooking에 main.join으로 종료 기다림 )
			```
				 // Get a reference to the main thread
        final Thread mainThread = Thread.currentThread();

        // Adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                consumer.wakeup();
                // join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
				..
				try {
          consumer.subscribe(Arrays.asList(topic));

          while (true) {
            log.info("Polling!");

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
              log.info("key : " + record.key() + ", value : " + record.value());
              log.info("Partition : " + record.partition() + ", Offset : " + record.offset());
            }
          }
        } catch (WakeupException e) {
          log.info("Consumer is starting to shut down");
        } catch (Exception e){
          log.error("Unexpected exception in the consumer", e);
        } finally {
          consumer.close(); // close the consumer, this will also commit offsets
          log.info("The consumer is now gracefully shut down");
        }
		```
		- Java API - Consumer Groups and Partition Rebalance
			- Partition Rebalance 
				- 같은 토픽, 같은 Consumer Group내에 속한 Consumer가 추가/제거 된다면 Partition을 재분배하는 리밸런스(Rebalance)가 일어난다.
				- Consumer가 그룹에 참여하거나 탈퇴할 때 발생함
				- Rebalance 종류
					- Eager Rebalance(적극적 리밸런스)
						- Consumer가 추가되면, 모든 Consumer는 Stop -> 이전 파티션 할당 Revoke -> 전부 무작위로 재할당
						*** Stop 해야한다는 것과(Stop the World), 이전에 할당 받은 Partition이 다시 같은 consumer에게 할당된다는 보장이 없음 ***
						- RangeAssignor : assign partitions on a per-topic basis
						- RoundRobin : assign partitions across all topics in round-tobin fashion, optimal balance
						- StickyAssignor : balanced like RoundRobin, then minimises partition movements when consumer join/leave the group 
					- Cooperative(Incremental) Rebalance(협력적/점진적 리밸런스)
						- 특정 파티션들만 Reassign되므로, 다른 Consumer들은 중단 없이 작업함
						- partition.assignment.strategy
							- CooperativeStickyAssignor : rebalance strategy is identical to StickyAssignor but supports cooperative rebalances and therefore consumers can keep on consuming from the topic
				- The default assignor is [RangeAssignor, CooperativeStickyAssignor], which will use the RangeAssignor by default, but allows upgrading to the CooperativeStickyAssignor with just a single rolling bounce that removes the RangeAssignor from the list.
				  - properties 설정으로 assign 전략 변경 가능 properties.setProperty("partition.assignment.strategy", CooperativeStickyAssignor.class.getName());
				- KAFKA Connect : Cooperative Rebalance가 Default
				- KAFKA Streams : StreamsPartitionAssignor가 Default
			- Static Group Membership
				- 기본적으로는 Consumer가 group을 leave하면 해당 partition을 revoke하고, re-assign 함
				- 만약 나갔던 Consumer가 돌아오면 새로운 Member ID를 받고 새로운 Partition을 assign 받음
				- 여기서 group.instance.id라는 static한 member ID값을 설정을 해주면, consumer가 leave한 뒤, Partition을 assign하지 않고 session.timeout.ms 만큼 기다린다. 그 timeout안에 동일한 group.instance.id를 가진 consumer가 re-join하면 해당 Partition을 그대로 assign해주고, timeout이 지나도 re-join하지 않으면 re-assign하는 방식이다.
		- Auto Offset Commit Behavior
			- enable.auto.commit을 true로 설정하고, auto.commit,interval.ms를 5000으로 설정하면, 데이터를 poll하고 5초가 지난 뒤, 다시 poll을 호출할 때 같이 commit을 한다!

			***** 지금까지 offset이 커밋되는 것은, offset auto commit에 의해 다시 pool을 호출해주면서 offset이 commit되거나, Consumer.close()를 하면서 자동으로 offset이 commit 된 것임!!! *****
- Section 6 : Wikimedia Project and advanced Producer configurations
	- Wikimedia Project
	  - wikimedia의 recent change data를 받는 url로 Event를 등록해두어, 해당 URL로부터 데이터(메시지)가 들어오면 onMessage 이벤트 핸들러가 동작함. 이 핸들러에 Kafka Producer Send를 등록해두어 데이터가 들어오면 카프카에 전송하는 구조!
	  - Producer Properties 세팅
	    - Producer Acknowledgements (acks)
	      - Producer들은 Data Write가 완료되면 ACK를 받을 수 있음
	      - acks = 0 : Producer가 ACK를 기다리지 않음 (Data Loss 가능성 있음)
	        - Producer가 메시지를 전송한 순간 메시지 쓰기를 성공했다고 간주. Broker가 수신하기까지 기다리지 않음. Broker가 오프라인 상태 혹은 예외 발생하면 상황을 알 수 없고 데이터를 손실함
	        - 데이터 손실이 생길 수 있으나, 네트워크 오버헤드가 작아 처리량이 제일 좋음
	      - acks = 1 : Producer가 리더 Broker의 ACK를 기다림 (제한된 Data Loss)
	        - Producer가 메시지를 전송하고 리더 Broker로부터의 ACK가 와야 메시지 쓰기 성공으로 판단
	        - 복제(Replication)에 대한 보증은 되지 않음
	      - acks = all or -1 : Proder가 리더 Broker와 Replica Broker들의 ACK를 다 기다림(손실 X)
          - Producer는 모든 ISR로부터 메시지를 수신했다는 ACK를 받아야 메시지 쓰기 성공으로 판단
        - min.insync.replicas
          - 최소 복제되어야하는 Broker 개수. 
        - Kafka Topic Availability : 카프카 토픽 가용성
          - acks=all일때, Replication Factor가 N이고 min.insync.replicas가 M이면, N-M개의 Broker가 다운되어도 괜찮음
          - 주로 Acks=all이고 min.insync.replicas=2로 설정하여, 하나의 Broker가 다운되어도 견딜 수 있음
	    - Producer Retries
	      - 데이터 전송에 실패하면 개발자는 그에 따른 예외처리를 해야함
	      	- ex) NOT_ENOUGH_REPLICAS (due to min.insync.replicas setting)
	      - 이를 재처리하는 세팅이 있음 (retries)
	        - KAFKA 2.0 이전에는 0, KAFKA 2.1이후에는 2147483647이 기본값
	      - retry.backoff.ms은 다음 재시도까지의 대기시간이고, 100ms가 기본값
	      - 재처리는 timeout 까지 계속 재처리된다.
	      - 요청이 delivery.timeout.ms이 지날때까지 처리되지 않으면 요청은 실패했다고 봄
	      - Idempotent(멱등) Producer (KAFKA 3.0부터 default)
	        - 메시지 중복, 순서 역전을 방지하는 Producer임
	        - ex) 메시지 A 전송(P) -> 메시지 commit(B) -> ACK 전송(B) -> ACK 전송실패 -> 메시지 A 재전송(P) -> 메시지 commit(B) -> ACK 전송(B) -> ACK 수신(P)
	          - > 메시지 중복 
	        - Idempotent Producer를 사용하면 메시지를 재전송했을 때, Broker에서 중복임을 감지하여 commit하지 않고 ACK만 Broker에게 전송
          - They come with:
            - retries = Integer.MAX_VALUE(2147483647)
            - max.inflight.request=1 (Kafka=0.11) 혹은 5 (Kafka>=1.0)
            - acks = all
            - producerProps.put("enable.idempotence",true);
    	- KAFKA Producer Default
    	  - Since KAFKA 3.0
	    	  - acks = all
	    	  - enable.idempotence=true
    	  - With KAFKA 2.8 and lower
    	    - acks = 1
    	    - enable.idempotence=false
    	- Safe KAFKA Producer 정리
    	  - acks = all
    	    - Ensures data is properly replicated before an ack is received
    	  - min.insync.replicas = 2
    	    - Ensures two brokers in ISR at least have the data after an ack
    	  - enable.idempotence = true
    	    - Duplicates are not introduced due to network retries
    	  - retries = MAX_INT
    	    - Retry until delivery.timeout.ms is reached
    	  - delivery.timeout.ms = 120000
    	    - Fail after retrying for 2 minutes
    	  - max.in.flight.requests.per.connection = 5
   				- Ensure maximum performance while keeping message ordering
   	  *** KAFKA 3.0 밑 버전을 사용할 시 위의 설정을 통해 KAKFA Producer을 Safe하게 해줘야함
   	    - ex) properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,"true");
        			properties.setProperty(ProducerConfig.ACKS_CONFIG,"all");
        			properties.setProperty(ProducerConfig.RETRIES_CONFIG,Integer.toString(Integer.MAX_VALUE));
  - Message Compression at the Producer level
    - 메시지 압축은 Producer level에서 진행(Broker/Topic level도 있음), Broker나 Consumer에서 설정을 변경해 줄 것이 없음.
    - compression.type은 none(default), gzip, lz4, snappy, zstd 등이 있음 (snappy, lz4를 주로 사)
    - 메시지 압축의 장점
      - Much smaller producer request size
      - Faster to transfer data over the network
      - Better throughput
      - Better disk utilisation in KAKFA
    - 메시지 압축의 단점
      - Producers must commit some CPU cycles to compression
      - Consumers must commit some CPU cycles to decompression
    - Broker level 압축
      - Broker level 압축은 모든 Topic에 대하여 압축, Topic level은 각 topic만 압축
      - Broker에서의 압축은 별도의 CPU 자원이 더 들어가서 비효율적임
      - Broker compression 설정
	      - compression.type = producer(default) : Broker는 Producer가 압축한 배치를 재압축하지 않고 그대로 로그 파일로 저장
	      - compression.type = none : 모든 배치는 Broker에 의해 decompressed됨
	      - compression.type = lz4(특정 압축)
	        - 만약 Producer의 compression 세팅과 동일하면 데이터를 그대로 저장
	        - 만약 Producer의 compression 세팅과 다르면 배치들을 decompressed한 뒤에 다시 명시한 압축 알고리즘으로 재압축한다.
    *** 주로 Producer에서 압축하고, Broker에서는 compression.type을 기본값인 producer로 두어 압축한 배치를 그대로 사용하고, Producer에서 압축을 못하는 상황에서만 Broker에서 압축!
  - Two settings to influence the batching mechanism
    - linger.ms : (default 0) how long to wait until we send a batch. Adding a small number for example 5 ms helps add more messages in the batch at the expense of latency. 
      - Message들이 들어왔을때 바로 전송하는 것이 아니라 linger.ms만큼 기다려서 batch로 만들어서 전송!
    - batch.size : (default 16KB) if a batch is filled before linger.ms, increase the batch size
      - Maximum number of bytes that will be included in a batch
      - Increasing a batch size to something like 32KB or 64KB can help increasing the compression, throughput, and efficiency of requests
      - Any message that is bigger than batch size will not be batched
      - A batch is allocated per partition, so make sure that you don't set it to a number that's too high, otherwise you'll run waste memory
      - KAFKA Producer Metrics을 통해 average batchsize metric을 모니터링 할 수 있음
  - High Throughput Producer
    - Increase linger.ms and the producer will wait a few milliseconds for the batches to fill up before sending them
    - If you are sending full batches and have memory to spare, you can increase batch.size and send larger batches
    - Introduce some producer-level compression for more efficiency in sends
  - High Throughput Producer - batch Demo
    - Snappy 메시지 압축을 사용해볼 것임. text 기반 메시지 압축에 효과적
    - batch.size를 32KB, linger.ms를 20ms로 세팅해볼 것임
    - ex) properties.setProperty(ProducerConfig.LINGER_MS_CONFIG,"20");
      		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,Integer.toString(32x1024));
      		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");
  - Producer Default Partitioner when key!=null
    - Producer Partitioner 로직에 따라 key가 어떤 Partition에 저장이 될지 Partition을 할당하는 로직이 있음. 
    - Key Hashing is the process of determining the mapping of a key to a partition
    - In the default KAFKA partitioner, the keys are hashed using murmur2 algorithm
      - targetPartition = Math.abs(Utils.murmur2(KeyBytes)) % (numPartitions - 1)
    - This means that same key will go to the same partition, and adding partitions to a topic will completely alter the formula
    - 식을 보면 알 수 있듯이, partition개수가 달라지게되면 저장되는 타겟 partition이 달라질 수 있음! => 추가가 필요하다면 토픽을 새로 만드는게 맞음
    - 권장되지는 않지만, 아주 특수한 경우에 파티셔널 로직을 수정하여 사용하려면 KAFKA Producer의 Partitioner.class 파라미터를 통해 만들 수 있음.
  - Producer Default Partitioner when key=null
    - When key=null, the producer has a default partitioner that varies:
      - Round Robin(Version <= KAFKA 2.3)
        - This results in more batches (one batch per partition) and smaller batches
        - Smaller batches lead to more requests as well as higher latency
      - Sticky Partitioner(Version >= KAFKA 2.4)
        - It would be better to have all the records sent to a single partition and not multiple partitions to improve batching
        - The Producer sticky partitioner
          - We "stick" to a partition until the batch is full or linger.ms has elapsed
          - After sending the batch, the partition that is sticky changes
          - Larger batches and reduced latency (because larger requests, and batch.size more likely to be reached)
  - Producer max.block.ms 와 buffer.memory
    - Producer의 처리량이 매우 많아지고, Broker가 요청에 빠르게 대응하지 못하면 Producer의 메모리에 Record 쌓임.
      - buffer.memory = 33554432 (32MB) : the size of the send buffer
    - The buffer will fill up over time and empty back down when the throughput to the broker increases
    - If the buffer is full, then the .send() method will start to block(바로 return 안됨)
      - max.block.ms = 60000 : the time the .send() will block until throwing an exception. 60초까지 block되도 되며, 그 이후는 Error 던진다는 말
      - Exceptions are thrown when:
        - The producer has filled up its buffer
        - The broker is not accepting any new data
        - 60seconds has elapsed
      - If you hit an exception hit that usually means your brokers are down or overloaded as they can't respond to requests

- Section 7 : OpenSearch Consumer and advanced Consumer configurations
  - Delivery Semantics
    - At Most Once : 메시지 batch를 수신하자마자 offset을 커밋하는 방법. 데이터를 처리중에 Consumer가 crash되거나 처리가 잘못되면, 데이터 유실이 일어난다.
    - At Least Once(권장됨) : 메시지가 처리된 뒤에 offset을 커밋하는 방법. 처리가 잘못되면 메시지를 다시 읽음. 그렇기에 메시지 처리의 중복이 발생할 수 있음.
      - > Processing이 idempotent(멱등)이어야 함
    - Exactly Once : Can be achieved for KAFKA => KAFKA workflows using the Transactional API(easy with KAFKA Streams API). For KAFKA => Sink workflows, use an idempotent consumer
  - Consumer 멱등 처리
    - 고유한 ID를 두어 처리할 때 사용하면 됨. 
    - ex) 
	```
	String id = record.topic()+"_"+record.partition()+"_"+record.offset();
	```
  - Consumer Offset Commit Strategies
    - Strategues:
      - enable.auto.commit = true & synchronous processing of batches
        - ex) 
			```
			while(true){
        	  List<Records> batch = consumer.poll(Duration.ofMillis(100));
        	  doSomethingSynchronous(batch)
        	}
			```
      - enable.auto.commit = false & manual commit of offset
      	- ex) 
			```
			while(true){
        	  batch += consumer.poll(Duration.ofMillis(100));
        	  if isReady(batch){
	        	  doSomethingSynchronous(batch)
	        	  consumer.commitAsync();
	        	}
        	}
			```
      - enable.auto.commit = false & storing offsets externally
        - Need to assign partitions to consumers at launch manually using .seek() API
        - Need to model and store offsets in DB.
    - Auto offset Commit Behavior
      - In the Java Consumer API, offset은 정기적으로 커밋됨
      - Offset은 auto.commit.interval.ms가 지나고 poll을 호출할 때 커밋된다.
      - Make sure messages are all successfully processed before call poll() again
        - 그렇지 않는 경우 enable.auto.commit을 false로 바꾸고 commitSync()나 commitAsync()를 호출하여 offset을 수동으로 커밋해주어야함.
  - Consumer Offset Reset Behavior
    - offset.retention.minutes : Broker 설정, offset 보관 기간
    - If Kafka has a retention of 7 days, and consumer is down for more than 7 days, the offsets are "Invalid"!
    - auto.offset.reset 
      - latest (가장 최신 log 끝부터. 즉 지금부터 들어오는 것들 받겠다) : Will read from the end of the log
      - earliest (가장 처음 log부터. 처음부터 쭉 받겠다) : Will read from the start of the log
      - none : Will throw exception if no offset is found
  - Replaying data for Consumers
    - To reply data for a consumer group:
      - 1. Take all the consumers from a specific group down
      - 2. Use kafka-consumer-groups command to set offset to what you want
      - 3. Restart consumers
  - Controlling Consumer Liveliness
    - Consumer 그룹 내의 Consumer들은 Consumer Groups Coordinator에게 Heartbeat 메시지를 보내고 Broker에게 Poll 요청을 보낸다.
      - Heartbeat와 Poll을 통해 Consumer가 down되었는지 살아있는지 판단함
    - Consumer Heartbeat Thread
      - This Mechanism is used to detect a consumer application being down
      - heartbeat.interval.ms (default 3 seconds):
        - Heartbeat 보내는 주기 설정이며, 보통 session.timeout.ms의 1/3로 설정함
      - session.timeout.ms (default 45 seconds kafka 3.0+, before 10 seconds)
        - 세션 timeout 시간동안 heartbeat가 오지 않으면 consumer dead로 판단
    - Consumer Poll Thread
			- 데이터 처리 issue을 detect 하는데 사용(처리 오류, consumer is "stuck")    
      - max.poll.interval.ms (default 5 minutes)
        - 2번의 poll 사이에 최대 시간. 설정 시간보다 오래걸리면 문제가 있다고 판단함
        - Poll한 데이터를 처리하는데 걸리는 경우에 설정을 잘해야함(Spark 같은 Big Data 프레임워크)
      - max.poll.records (default 500)
        - 요청당 한번에 가져올 수 있는 레코드 개수.
        - Record 크기, Record 처리 시간에 따라 달라져야함
      - fetch.min.bytes (default 1)
        - 요청당 가져올 요청의 최소 단위.
      - fetch.max.wait.ms (default 500)
        - fetch.min.bytes를 만족하지 못하는 데이터를 반환하기 전에 block하는 시간
      - max.partition.getch.bytes (default 1MB)
        - 서버가 반환할 파티션당 데이터의 최대 사이즈
        - ex) 100개 파티션을 읽을때 100MB의 메모리가 필요함을 뜻함
      - fetch.max.bytes (default 55MB)
        - 각 fetch로 가져올 데이터의 최대 
  - Default Consumer Behavior with partition leaders
    - Kafka Consumer는 기본적으로 Leader Broker에서 데이터를 읽음
    - Kafka Consumers Replica Fetching (KAFKA 2.4+)
      - Leader가 아닌 가까운 Replica에서 데이터를 읽을 수 있음
      - Improve Latency! Decrease Network costs
    	- Broker setting:
    	  - Version >= KAFKA 2.4
    	  - rack.id 설정이 Data Center의 아이디여야함 (AWS의 경우 AZ ID)
    	  - replica.selector.class 가 org.apache.kafka.common.replica.RackAwareReplicaSelector로 설정되어야 함
  	  - Client setting:
  	    - client.rack 을 Consumer가 실행되는 data Center ID로 설정
*** admin@conduktor.io / admin

- Section 8 : KAFKA 확장 API & 실제 사례
	- 다양한 KAFKA Advanced API가 있음
	  - KAFKA Connect solves External Source => KAFKA, or KAFKA => External Sink
	  - KAFKA Streams solves 토픽 -> 토픽으로 데이터 전송
	  - Schema Registry helps using Schema in KAFKA
	- KAFKA Connect Introduction
	  - KAFKA Connect is all about code & connectors re-use!
	  - 개발자들은 항상 같은 곳(Source)에서 데이터를 가져오길(import) 원하고 항상 같은 곳(Target, sink)에 저장하길 원함
	  - Source Connectors : get data from Common Data Sources
	  - Sink Connectors : pubilsh that data in Common Data Stores
	  - KAFKA Connect는 source, sink connect 등이 있으며, 개발자들이 만들어 놓은 것을 재사용할 수 있음. 약간 모듈 재사용하는 느낌
	  - 필요한 설정 및 connector를 가지고 connect-standalone.bat 로 connector를 실행하는 느낌
	    - ex) connect-standalone.bat ../../config/connect-standalone.properties ../../config/elasticsearch.properties
	  - 알아서 source에서 put하고 알아서 kafka에서 get하여 sink에 저장하는 완성된 모듈 사용하는 느낌
  - KAFKA Streams Introduction
    - Easy data processing and transformation library within KAFKA
    - 특징 
      - Standard Java Application
 			- No need to create a separate cluster
 			- Highly scalable, elastic and fault tolerant
 			- Exactly-Once Capabilities
 			- One record at a time processing
 			- Works for any application size
 		- 아래와 같이 Stream 처리 Process를 만들고 Topology를 구성하고 KAFKA Streams를 통해 Stream 처리할 수 있는 라이브러리.
 	    - ex) 
		```
				StreamsBuilder builder = new StreamsBuilder();
		        KStream<String, String> changeJsonStream = builder.stream(INPUT_TOPIC);

		        BotCountStreamBuilder botCountStreamBuilder = new BotCountStreamBuilder(changeJsonStream);
		        botCountStreamBuilder.setup();

		        WebsiteCountStreamBuilder websiteCountStreamBuilder = new WebsiteCountStreamBuilder(changeJsonStream);
		        websiteCountStreamBuilder.setup();

		        EventCountTimeseriesBuilder eventCountTimeseriesBuilder = new EventCountTimeseriesBuilder(changeJsonStream);
		        eventCountTimeseriesBuilder.setup();

		        final Topology appTopology = builder.build();
		        LOGGER.info("Topology: {}", appTopology.describe());
		        KafkaStreams streams = new KafkaStreams(appTopology, properties);
		        streams.start();  
```
		  - ex) 
		  ```
		  		public void setup() {
			        this.inputStream
			                .mapValues(changeJson -> {
			                    try {
			                        final JsonNode jsonNode = OBJECT_MAPPER.readTree(changeJson);
			                        if (jsonNode.get("bot").asBoolean()) {
			                            return "bot";
			                        }
			                        return "non-bot";
			                    } catch (IOException e) {
			                        return "parse-error";
			                    }
			                })
			                .groupBy((key, botOrNot) -> botOrNot)
			                .count(Materialized.as(BOT_COUNT_STORE))
			                .toStream()
			                .mapValues((key, value) -> {
			                    final Map<String, Long> kvMap = Map.of(String.valueOf(key), value);
			                    try {
			                        return OBJECT_MAPPER.writeValueAsString(kvMap);
			                    } catch (JsonProcessingException e) {
			                        return null;
			                    }
			                })
			                .to(BOT_COUNT_TOPIC);
			    }
				```
  - KAFKA Schema Registry Introduction
    - KAFKA는 Producer로부터 Byte를 받아서 Consumer에게 publish함. 이 과정에서 데이터 Verification이 없음.
    - Producer가 잘못된 데이터를 보내거나, 필드 이름이 변경되었거나, 데이터 포맷이 변경되었거나... 등등의 상황에서 Consumer는 Error가 발생함.
      - We need data to be self describable
      - We need to be able to evolve data without breaking downstream consumers.
      - > 그렇기 때문에 Schema와 Schema Registry가 필요함.
    - Q. KAFKA Broker가 데이터를 받아서 Verifying하면?
      - KAFKA의 존재 가치가 사라짐
        - KAFKA는 데이터를 읽거나 파싱하지 않음(CPU 사용이 없음)
        - Bytes을 입력으로 받을 뿐 메모리에 올리지 않음(zero copy)
      	- KAFKA는 단순히 takes bytes, distributes bytes (no matter what kind of data.... String, Int, JSON 등등)
      - > So! Schema Registry는 별도로 구성되어야하며, 
        > Consumer와 Producer는 Schema Registry와 소통할 수 있어야하고,
        > Schema Registry는 KAFKA에게 데이터를 전송하기 전, Bad data를 reject할 수 있어야 함
        > Common data format은 Schema Registory와 합의 되어야함.
          - Data format은 스키마, 스키마 변경(evolution) 을 지원해야하며, 가벼워야함.
    - Schema Registry의 목적
      - Store and retrieve schemas for Producers / Consumers
      - Enforce Backward / Forward / Full compatibility on topics 
      - Decrease the size of the payload of data sent to KAFKA
    - Pipeline with/without Schema Registry는 아래와 같다.
      - Schema Registry 없을 때 : Source - Producer - KAFKA - Consumer - Target
      - Schema Registry 있을 때 : Source - Producer     -     KAFKA     -     Consumer - Target
                                              |                |                |
                                              |---------Schema Registry---------|
        - Producer가 데이터를 KAFKA로 보내기 전 Schema Registry로 보냄 -> Schema Registry가 KAFKA와 스키마 유효성을 체크 -> 문제가 없다면, Producer가 데이터를 KAFKA에 전송
        - Consumer가 데이터를 KAFKA에서 받음 -> Schema Registry로부터 Schema를 받음 -> Schema를 통해 Data를 역직렬화하여 사용
  - KAFKA API
    - DB에서 가져오고 DB에 넣는 로직이라면 Connect사용, 간단하게 데이터를 전달하여 메일을 보낸다거나의 처리는 producer / consumer 사용
    - 사용하는 API는 서비스의 특성에 따라 달라질 것으로 보임 (ex. KAFKA 내부에서 Stream 처리가 필요한지, 단순한 처리인지 등)
    - Source DB - KAFKA Connect Source  - KAFKA  - KAFKA Connect Sink - Target DB
      Client    - KAFKA Producer        -        - KAFKA Consumer     - 간단한 처리
                                          |   |
                               KAFKA Streams  KSQL
  - 실제 KAFKA 사례
    - Partition Conut & Replication Factor & Cluster 설정 가이드
      - Partition 개수를 나중에 변경하게 된다면, Key ordering 보장이 깨진다.
      - Replication Factor를 topic lifecycle 도중 증가시키게 된다면, Cluster에 부담을 주어 성능에 영향이 감
    	- Partition Count 가이드라인
    	  - Partition의 개수가 많을 때의 장점
    	    - 병렬 진행(Parallelism), Throughput 증가
    	    - Consumer Group내에서 Consumer의 개수를 늘릴수 있다(Consumer의 최대 개수는 Partition 총 개수보다 클 수 없으므로)
    	    - Cluster가 크다면, 더 많은 Broker를 활용할 수 있음
    	  - 단점
    	    - Zookeeper를 위해 더 많은 Election이 이루어짐 (Partition Leader 선정?)
    	    - KAFKA에서 더 많은 File이 열림
        - > 가이드라인
          - Small Cluster(< 6 Brokers) : 3 x number of brokers
          - Big Cluster(> 12 Brokers) : 2 x number of brokers
          - Adjust for number of consumers you need to run in parallel at peak throughput
          - Adjust for producer throughput (increase if super-high throughput or projected increase inthe next 2 years)
          *** 각 환경과 상황에 따라 성능이 다르므로, 테스트를 반복하여 성능을 체크하여 최적의 배치를 찾는 것이 중요! ***
      - Replication Factor 가이드라인
        - Should be at least 2, usually 3,maximum 4
        - Replication Factor가 높을 때의 장점
          - Better durability of system (N-1 Broker들의 down되어도 괜찮음)
          - Better availability of system (N-min.insync.replicas if producer acks=all)
        - 단점
          - Higher latency
          - More disk space on system
        - > 가이드라인:
          - Replication Factor를 3으로 두고 시작!
          - Replication 성능이 문제가 된다면, replication factor를 줄이는 것이 아니라 더 좋은 성능의 Broker를 사용하는 것이 옳음
          - 절대 1로 설정하지 않을 것!!
      - Cluster 가이드라인
      	- 클러스터 내의 총 Partition의 개수:
      	  - Zookeeper 사용하는 KAFKA : Max 200,000 Partitions(Nov 2018) - Zookeeper Scaling limit
      	  	- 각 브로커마다 4,000개의 Partition을 권장하고 있음(Soft limit)
      	  - KRaft 사용하는 KAFKA : Millions of Partitions
      	    - 만약 200,000개의 Partition보다 더 많이 필요하다면, Netflix model을 참고하고, 독립적인 KAFKA Cluster를 추가로 생성하기
    - 토픽 이름 짓기
      - 토픽 이름은 자유이지만, Cluster 관리를 위하여 가이드라인을 만들어라 (참고 https://cnr.sh/essays/how-paint-bike-shed-kafka-topic-naming-conventions)
        - 위 링크에서는 <message type>.<dataset name>.<data name>.<data format> 형식을 추천함
          - Message type : logging, queuing, tracking, etl/db, streaming, push, user
          - dataset name : 토픽을 하나로 묶기 위한 카테고리. DB 이름과 유사
          - data name : DB Table 이름과 유사
          - data format : avro, json, text, protobuf, csv, log
          - use snake_case
    - KAFKA 실제 사례 1 _ 비디오 분석 MovieFlix
      - MovieFlix는 TV 쇼나 영화를 사용자의 요구에 따라 보여주는 회사. 아래와 같은 요구사항이 있음
        - 사용자가 마지막으로 봤던 곳에서 다시 재생하는 기능
        - 사용자의 Profile을 실시간으로 만드는 기능
        - 사용자에게 다음에 볼 영상을 실시간으로 추천하는 기능
        - 모든 데이터를 Analytics Store에 저장하는 기능
      - 여기서 KAFKA를 어디서 어떻게 사용할까?
        - KAFKA Architecture
	        - Video Player -> Video Position Service(Producer) -> KAFKA(Show Position Topic) -> Resuming Service(Consumer) -> Video Player
	        - KAFKA(Show Position Topic) -> KAFKA Streams(Recommendation Engine in Real Time) -> KAFKA(Recommendations Topic) -> Recommendations Service(Consumer) -> Movies and TV Shows Portal
	        - KAFKA(Show Position Topic & Recommendations Topic) -> KAFKA Connect(Analytics consumer) -> Analytics Store(Hadoop)
	      - show_position Topic:
	        - 많은 사용자가 Producer가 되는 Topic, Data 양이 많다면 distributed되어야함.
	        - user_id를 Key로 선정하여, 한 유저의 데이터에 대해서는 순서를 유지함
	      - recommendations Topics:
 					- KAFKA Streams를 통해 recommendation engine으로 분석을 할 것이며, 아마 Topic Data 양이 적을 것임
 					- user_id를 Key로 선정하고, Partition 개수를 show_position보다는 적게 설정할 것임.
    - KAFKA 실제 사례 2 _ IoT GetTaxi
      - GetTaxi는 사용자와 택시 운전자를 즉시 매칭시켜주며 아래와 같은 요구사항이 있음
        - 사용자는 가까이 있는 택시와 매칭되어야함
        - 사용자수가 많거나 택시 운전자가 적을 때 가격은 오를 수 있음
        - 모든 위치 정보(타기전, 탑승중)들은 Analytics Store에 저장되어, 비용을 정확하게 측정
      - 여기서 KAFKA를 어디서 어떻게 사용할까?
        - KAFKA Architecture
	        - User Application -> User Position Service(Producer) -> KAFKA(User_position topic) -> Surge Pricing computation Model(KAFKA Streams) -> KAFKA(Surge pricing Topic) -> Taxi Cost Service(Consumer) -> User Application
	        - Taxi Driver Application -> Taxi Position Service(Producer) -> KAFKA(Taxi_position topic) -> Surge Pricing computation Model(KAFKA Streams) -> KAFKA(Surge pricing Topic) -> Taxi Cost Service(Consumer) -> User Application
	        - KAFKA(user_position, taxi_position, Surge Pricing) -> Analytics Consumer(KAFKA Connect) -> Analytics Store(Amazon S3)
	      - user&taxi postion topics:
	        - 여러 Producer가 있고 데이터 양이 많기에 distributed 되어야함.
	        - Key는 user_id, taxi_id로 설정, 데이터의 수명은 짧은 편이기에, retention 기간을 짧게 설정
      - surge_pricing topic:
        - KAFKA Streams Application으로부터 surge pricing 계산 결과를 받음
        - 날씨, 이벤트 등의 다른 정보들을 추가하여 계산을 더 정확하게 할 수 있음
    - KAFKA 실제 사례 3 _ CQRS(Command and Query Responsibility Segregation) MySocialMedia
      - MySocialMedia는 유저가 이미지를 올리고 다른 사용자가 likes, comments를 사용하여 반응하는 서비스 SNS. 요구사항은 아래와 같음
        - 유저들은 Post, like and comment가 가능해야함
        - 유저들은 실시간으로 각 포스트에대한 likes와 comments의 총 숫자를 볼 수 있어야함
        - 오픈 첫날에는 대량의 데이터가 예상됨
        - 유저들은 인기가 높은 포스트를 볼 수 있어야함
      - 여기서 KAFKA를 어디서 어떻게 사용할까?
        - User posts -> Posting Service(Producer) -> KAFKA(Posts topic) -> Total likes/comments computation(KAFKA Streams) -> KAFKA(posts_with_counts) -> Refresh Feed Service(Consumer) -> Website
        - User likes/comments -> Like/Comment Serivce(Producer) -> KAFKA(likes topic, comments topics) -> Total likes/comments computation(KAFKA Streams) -> KAFKA(posts_with_counts) -> Refresh Feed Service(Consumer) -> Website
        - KAFKA(posts, likes, comments topic 혹은 posts_with_counts topic) -> Trending posts in the past hour(KAFKA Streams) -> KAFKA(Trending_posts topic) -> Trending Feed Service(Consumer) -> Website
      - Posts topic:
        - 여러 Producer가 존재, 많은 양이 있으므로 distributed -> 여러 Partition
        - user_id를 key로 두고, retention 기간을 길게 설정
      - Likes, Comments topic:
        - 여러 Producer가 존재, 많은 양이 있으므로 distributed -> 여러 Partition
        - post_id를 key로 둠
      - Data는 이벤트 방식의 형식을 갖춰야함
        - ex) User_123이 Post_1032를 Post함. ...like함. ...delete함. ...comment를 남김 등등
    - KAFKA 실제 사례 4 _ Finance Application MyBank
      - MyBank는 온라인 은행으로, 실시간 은행 서비스와 거액의 이체가 발생시 유저에게 알려주는 기능이 있음. 요구사항은 아래와 같음
        - Transaction 데이터는 이미 DB에 존재함
        - 거액 기준은 각 사용자가 앱에서 설정함
        - 알람은 사용자에게 실시간으로 전송되어야함
      - 여기서 KAFKA를 어디서 어떻게 사용할까?
        - DB of Transactions -> KAFKA Connect Source(CDC Connector. Debezium) -> KAFKA(bank_transactions topic) -> Real time Big Transactions Detection(KAFKA Streams) -> KAFKA(User_alerts topic) -> Notification Service(Consumer) -> Users see notifications in their apps
        - Users set threshold in Apps -> APP Threshold Serivce(Producer) -> KAFKA(user_settings topic) -> Real time Big Transactions Detection(KAFKA Streams) -> KAFKA(User_alerts topic) -> Notification Service(Consumer) -> Users see notifications in their apps
      - bank_transactions topic:
        - KAFKA Connect Source는 존재하는 DB에서 데이터를 가져오는데 좋은 방법
        - CDC(Change Data Capture) Connector를 사용하면 좋음
      - KAFKA Streams Application:
      	- 사용자가 세팅을 변경한 뒤에, 과거 Transaction은 알람 트리거하지 않음
      - user_threshold topic:
      	- 이벤트 형식(User123 enabled threshold at $1000 at 12pm on July 12th 2023)으로 데이터를 보내는 것 보다, state 방식(User123:threshold $1000)으로 데이터를 보내는 것이 효과적
    - KAFKA 실제 사례 5 _ Big Data Ingestion(수집, 적재)
      - 기존의 Connector들은 데이터를 받아서 KAFKA에 올리고 그것을 HDFS, Amazon S3 and ElasticSearch같은 곳으로 전달하였음
      - KAFKA는 실시간 Application을 위해 Speed Layer를 제공하고, Batch 방식의 데이터 적재를 위해 Slow Layer를 제공함
      - Big Data Ingestion 2가지 패턴
        - 데이터 Producer -> KAFKA -> Spark/Storm/Flink 등등 -> Real time analytics/Dashboards/Alerts/Apps/Consumers (Real Time)
        - 데이터 Producer -> kAFKA -> KAFKA Connect -> Hadoop/Amazon S3/RDBMS -> Data Science/Reporting/Audit/Backup/Long term Storage (Batch)
    - KAFKA 실제 사례 6 _ Logging & Metrics Aggregation(집계)
      - Application -> Log Forwarders -> KAFKA(application_logs topic) -> KAFKA Connect Sink -> Splunk
      - Application -> Metric collectors -> KAFKA(application_metrics topic) -> KAFKA Connect Sink -> Splunk

- Section 9 : KAFKA 기업 가이드
  - KAFKA Cluster Setup - High Level Architecture
    - Cluster를 setup하는 것은 쉽지 않음.
    - 각 Zookeeper와 Broker를 각각의 서버에 독립시켜야하고(장애 발생시 하나만 터지게), 모니터링도 필요하며, KAFKA 명령어도 숙달되어야함
    - 그래서 KAFKA as a Service의 형태로 여러 기업에서 관리해주는 것이 있음(Amazon MSK, Confluent Cloud, CloudKarafka, Instaclustr, Upstash, etc...)
      - 운영 부담이 적음
    - Broker 개수는 throughput, data retention, replication factor등 여러 계산해야할 것이 많고, 적용하는 각각의 환경에서 테스트를 많이 해봐야함
    - Broker 뿐만 아니라 KAFKA Connect Clusters, KAFKA Schema Registry(High Availability를 위해 최소 2개), UI Tools, Admin Tools for automated workflows, Automate 등 할게 많음
  - KAFKA Monitoring and Operations
    - KAFKA Monitoring
      - KAFKA는 JMX를 통해서 모든 KAFKA Metric들을 볼 수 있음
      - Common places to host the KAFKA Metrics:
        - ELK (ElasticSearch+Kibana), Datadog, NewRelic, Confluent Control Centre, Promotheus
      - 몇몇 중요한 지표들:
        - Under Replicated Partitions : ISR(In-Sync Replicas)에 문제가 있는 Partitions의 개수 => 파티션들이 싱크가 안되고 있는 것. 시스템 부하가 
        - Request Handlers : 쓰레드 이용률. 전반적인 KAFKA Broker의 이용률로 보면됨.
        - Request timing : 요청에 응답하는데 소요되는 시간.
    - KAKFA Operations
      - KAFKA 운영팀이 수행해야하는 tasks:
        - Rolling Restart of Brokers (클러스터를 구성하는 노드를 하나씩 restart 하는 것)
        - Topic&Broker의 설정 업데이트
        - Rebalancing Partitions
        - Increasing/Decreasing replication factor
        - Adding a Broker
        - Replacing a Broker
        - Removing a Broker
        - KAFKA Cluster 업그레이드(Without downtime)
  - KAFKA 보안
    - 현재 KAFKA 보안 
	    - 어느 Client도 KAFKA Cluster에 접근할 수 있음(No authentication, 인증X)
	    - 어느 Client든 모든 Topic의 데이터를 public/consume할 수 있음(No authorisation, 권한 부여 X)
	    - 모든 데이터가 암호화되어 있지 않아서 네트워크에서 다 볼수 있음(No encryption)
	    - 데이터를 intercept할 수 있고, bad 데이터를 publish하거나, 훔칠수 있고, 토픽을 삭제할 수 있는 문제가 있다.
	  - In-flight encryption in KAFKA
	    - Encryption in KAFKA ensures that the data exchanged between clients and brokers is secret to routers on the way.
	    - HTTPS와 비슷함.
	  - Authentication(SSL&SASL) in KAFKA
	    - Authentication in KAFKA ensures that only clients that can prove their identity can connect to KAFKA Cluster
	    - login 개념과 비슷(username/password)
	    - SSL Authentication
	    	- clients authenticate to KAFKA using SSL certificates
	    - SASL/PLAINTEXT
	    	- clients authenticate using username/password
	      - Must enable SSL encryption broker-side as well
	      - Changes in passwords require brokers reboot(good for dev only)
	    - SASL/SCRAM 
	    	- username/password with using salt
	      - Must enable SSL encryption broker-side as well
	      - Authentication data in Zookeeper(until removed)
	    - SASL/GSSAPI (Kerberos)
	      - Kerberos : such as Microsoft Active Directory
	    - SASL/OAUTHBEARER
	      - OAUTH2 token for authentication.
	  - Authorisation in KAFKA
	    - ACL(Access Control List)는 관리자에의해 유지되어야함
	  - KAFKA Security - Putting it all together
	    - Encryption + Authentication + Authorisation => KAFKA Security
	- KAKFA Multi Cluster & Replication
	  - Replication Application은 결국 Consumer와 Producer 합친 것
    - Tools
      - Mirror Maker2 : Open-source KAFKA Connector이며, 클러스터의 데이터를 다른 클러스터로 복제함
      - Netflix uses Flink : 넷플릭스에서 만든 Application
      - Uber uses uReplicator : Mirror Maker1의 성능 문제를 해결한 Uber에서 만든 Application
      - Comcast는 open-source KAFKA Connect Source를 가지고 자사 Application을 만듬
      - Confluent도 직접 자사 KAFKA Connect Source를 만듬(유료)
    - Replicating은 offset을 보존하지는 않고 데이터만 복제함. 어떤 offset의 데이터가 다른 클러스터의 동일 offset의 데이터와 동일한 것을 보장할 수 없음
    - 구조
      - Active / Active (클러스터 간 서로 write가 가능)
        - 장점:
          - 사용자와 더 가까이있는 Data Center를 이용할 수 있어서 성능이 좋음
          - Redundancy(중복)와 Resilience(회복력)도 장점. 
        - 단점:
          - 양방향 쓰기가 가능하기 때문에, 데이터를 읽을 때 충돌을 피해야함
      - Active / Passive
        - 장점:
          - 구성이 쉬움.
          - 데이터 접근이나 충돌 처리 등 구조적인 복잡함이 없음
        - 단점:
          - Cluster의 낭비, 메인 Cluster Failover시 데이터 손실이나 중복이 발생
  - Client와 KAFKA사이의 Communications 이해
    - Advertised Listener : KAFKA 설정 내에 가장 중요한 설정
      - ex) KAFKA CLIENT <-> KAFKA BROKER 통신 설정
      	1. Client가 BROKER의 public IP를 사용하여 연결 요청
      	2. Broker가 거절! 연결하려면 Advertised Host Name을 써야함!
      	3. Advertised Host IP는 ~.~.~.~이거야! 이걸 써서 연결해!
      	4. Client가 알려준 Advertiesed Host IP로 접속하여 연결!
      	*** 만약 ADV_HOST(Advertised Listener)가 private IP라면, KAFKA Client가 Broker와 동일한 네트워크 내에 존재해야 연결! 다른 네트워크라면 연결할 수 없음! ***
      - ex) 그러면 ADV_HOST를 public IP로 설정하면?
        *** 정상 동작, but Public IP가 변경되면 연결 실패! *** 
    - Client가 private network 내에 존재하면 advertised.listeners을:
      - internal private IP 혹은 private DNS hostname으로 설정!
    - Client가 public network내에 존재하면 advertised.listeners을:
      - external public IP 혹은 external public hostname pointing to the public IP로 설정! 
      - 이 경우, Cluster가 public IP 즉 공개적으로 노출되는 것은 유의해야함

- Section 10 : 고급 KAFKA 토픽 구성
  - 토픽 구성 변경
	  - 토픽 생성
	  	- ./kafka-topics.sh  --create --bootstrap-server localhost:9092 --topic configured-topic --replication-factor 1 --partitions 3
	  - 토픽 describe
	    - ./kafka-topics.sh  --describe --bootstrap-server localhost:9092 --topic configured-topic
	  - KAFKA 설정 명령어 : ./kafka-configs.sh
	    - Describe : ./kafka-configs.sh --bootstrap-server localhost:9092 --entity-type topics --entity-name configured-topic --describe
	    - Add config : ./kafka-configs.sh --bootstrap-server localhost:9092 --entity-type topics --entity-name configured-topic --alter --add-config min.insync.replicas=2
	    - Delete config : ./kafka-configs.sh --bootstrap-server localhost:9092 --entity-type topics --entity-name configured-topic --alter --delete-config min.insync.replicas
	- Segments & Index
	  - Segments Setting
		  - Topic은 Partitions로 구성되고, Partition은 Segments(files)로 구성된다.
		  - 각 Segment는 특정 범위의 Offset을 가짐
		    - ex) Seg0 (offset 0-957), Seg1 (offset 958-1675)...
		  - 하나의 Partition 내에는 오직 1개의 segment만 ACTIVE 상태이다.
		  - 2가지 Segment 세팅:
		    - log.segment.bytes : 단일 segment의 최대 Byte size (default 1GB)
		      - log.segment.bytes를 작게 설정하면, Log compaction이 자주 일어나고, Partition별로 더 많은 Segment가 생기고, KAFKA는 더 많은 files을 열어놓아 결국 T oo Many open files 에러가 발생함
		    - log.segment.ms : KAFKA가 segment가 full이 안됐지만 commit을 기다리는 시간.(1 week)
		      - 1주일 기다렸는데 segment가 다 차지 않아도, 해당 segment를 close하고 새로운 segment를 생성함
		      - log.segment.ms를 작게 설정하면, Log Compaction의 최대 주기를 설정하는 것이고, Log Compaction이 더 자주 일어나고, cleanup도 자주 일어남
		  - Segment는 2개의 Index가 있음
		    - An offset to position index : KAFKA가 특정 오프셋에서 특정 메시지를 찾기 위해 읽어야할 곳을 알려줌
		    - A timestamp to offset index : 특정 Timestamp를 통해 KAFKA가 메시지를 찾도록 도와줌
  - Log Cleanup Policies
    - KAFKA Cluster는 log cleanup 정책에 따라서 data를 expire함
    - 왜 Log Cleanup을 해야하나? 
      - KAFKA내의 데이터를 지움으로써:
	      - Disk 내의 데이터 크기를 조절하고, 오래된 데이터를 삭제함
	      - KAFKA Cluster의 maintenance 작업을 제한하는데에도 도움이 됨
	  - 언제 Log Cleanup이 일어나는가?
	    - Partition의 segment가 생성될 때마다 일어남. 즉 Smaller/more segment는 Log Cleanup이 더 자주 일어나는 것을 의미
	    - But 너무 자주 일어나면 CPU, RAM 자원을 낭비하게 됨
	    - Cleaner가 cleanup해야할 항목이 있는지 log.cleaner.backoff.ms마다 체크함(15초 기본값)
    - Policy1 : log.cleanup.policy=delete (모든 사용자 토픽의 기본값)
      - 데이터가 만들어진 시기에 따라 삭제됨(1주 기본값)
      - log의 최대 크기에 따라 삭제됨(-1이 기본값. 사이즈는 무한하지만 수명은 1주일)
      - 해당 정책에 영향을 미치는 설정:
        - log.retention.hours:
          - 데이터가 유지되는 시간 (기본값 168hour - 1주일)
          - Higher number(Disk 공간 차지), Lower number(Disk 공간은 줄겠지만, Consumer가 down되거나 consume하지 않으면 Data를 유실할 수 있음)
          - 시간 단위가 hour말고 ms와 minutes도 있음(적을수록 우선순위 높음)
        - log.retention.bytes:
          - 각 Partition에 있는 최대 Bytes 크기 (-1 기본값, infinite를 의미)
          - Log 사이즈를 일정 기준값 이하로 유지하고 싶을 때 유용함
      - 시간 or Byte 크기에 의하여 Segment를 지울 수 있음
    - Policy2 : log.cleanup.policy=compact (_ _ consumer_offsets의 모든 사용자 토픽의 기본값)
      - 확인 : ./kafka-topics.sh  --describe --bootstrap-server localhost:9092 --topic _ _ consumer_offsets
      - 가장 최근 발생한 Message의 Key를 기준으로 삭제됨. 즉 전의 모든 키는 Active segment가 커밋된 다음 삭제된다는 의미
      - 무한한 time, space retention
      - Log compaction은 파티션 내에 특정 키의 마지막으로 알려진 값이 포함되어 있다는 의미. log에 key를 가장 최신으로 업데이트하는 개념임. data에 대한 전체 history가 아니라 SNAPSHOT이 필요할 때 유용함
        - ex) user(key)별 연봉 데이터 topic에서 동일 key의 데이터가 들어오면 log compaction이 발생하여, 새로운 segment에는 key의 최신 데이터만 유지하고 이전 것은 삭제하는 방식!
      - Log Compaction Guarantees : 
        - 모든 consumer는 log의 tail부터 데이터를 읽을때 토픽으로 전송된 모든 메시지를 순서대로 볼 수 있고, Log Compaction은 일부 메시지만 삭제할 뿐이고, 순서는 재정렬하지 않고, 메시지의 offset도 바뀌지 않는다.
        - 삭제된 records는 delete.retention.ms(24시간 기본값)동안 consumer가 볼 수 있음
      - Log Compaction Myth Busting
        - 중복 데이터를 KAFKA로 보내는 것/읽는 것을 예방하지 못함
        - Log Compaction은 실패하는 경우도 종종 있음
      - 해당 정책에 영향을 미치는 설정:
        - segment.ms (default 7days)
        - segment.bytes (default 1G)
        - min.compaction.lag.ms (default 0)
        - delete.retention.ms (default 24hours)
        - min.cleanable.dirty.ratio (default 0.5) : 높으면 정리 빈도는 낮아지고 효율성 증가. 낮아지는 것은 반대
  - unclean.leader.election.enable
    - ISR이 offline이 되어 Out of Sync가 발생하였을 때 취할 수 있는 방법 2가지
      - ISR이 online이 될때까지 기다린다
      - unclean.leader.election.enable=true로 설정하여 non ISR Partitions에 producing한다.
    		- 가용성을 높일 순 있지만, 데이터를 잃을 수 있다.
    		- 데이터 유지보다 가용성이 훨씬 중요할 때만 사용하는 위험한 설정임
  - KAFKA에서 대용량 전송
    - 토픽 메시지당 1MB가 default 이고, 이보다 더 큰 메시지는 비효율적이고 anti-pattern임
    - 큰 메시지를 전송하는 2가지 접근:
      - 1. external Storage를 사용하는 것 : 데이터를 HDFS, Amazon S3, Google Cloud Storage, etc...에 보내놓고, 메시지의 reference를 KAFKA에 보내는 방식
      - 2. KAFKA parameter 수정 : Broker, Producer, Consumer setting 변경
      	- ex) 10MB 세팅
      	  - Topic-wise, KAFKA-side, set max message size to 10MB:
	      	  - Broker Side : message.max.bytes 변경
	      	  - Topic Side : max.message.bytes 변경
      	  - Broker-wise, set max replication fetch size to 10MB
      	    - replica.fetch.max.bytes=10485880 (in server.properties)
      	  - Consumer-side, must increase fetch size of the consumer will crash:
      	    - max.partition.fetch.bytes=10485880
      	  - Producer-side, must increase the max request size
      	    - max.request.size=10485880