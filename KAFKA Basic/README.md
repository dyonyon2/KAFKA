- Section 1 : 간단한 강의&KAFKA 소개
- Section 2 : KAFKA 이론
- Section 3 : KAFKA 시작하기
- Section 4: KAFKA CLI


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

- Section 3: KAFKA 시작하기
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

- Section 4: KAFKA CLI
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