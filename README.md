# scala-kafka-experiment

steps to reproduce:

1. run kafka with docker-compose:

```
cd .docker-compose-kafka
docker-compose up
```

2. create topic:
```
cd .docker-compose-kafka
docker-compose exec broker kafka-topics --create --topic test-exception-topic --bootstrap-server broker:9092
```

3. run project
```
sbt run
```
4. produce message
```
cd .docker-compose-kafka
docker-compose exec broker bash
kafka-console-producer --topic test-exception-topic --bootstrap-server broker:9092
```

example output:

```
[info] running com.test.app.Main 
14:28:46.761 [test-consumer-akka.actor.default-dispatcher-5] INFO  - Slf4jLogger started
14:28:46.853 [run-main-0] INFO  - Test Consumer Started
14:28:48.868 [test-consumer-akka.actor.default-dispatcher-6] INFO  - started consuming messages
14:28:50.945 [test-consumer-akka.actor.default-dispatcher-6] INFO  - --CONSUMED: offset: 97 message: 1
14:28:51.028 [test-consumer-akka.actor.default-dispatcher-6] INFO  - ----PRODUCED: offset: 98 message: 1643542130945
14:28:52.967 [test-consumer-akka.actor.default-dispatcher-6] INFO  - --CONSUMED: offset: 98 message: 1643542130945
14:28:52.974 [test-consumer-akka.actor.default-dispatcher-6] INFO  - ----PRODUCED: offset: 99 message: 1643542132967
.
.
.
14:29:07.106 [test-consumer-akka.actor.default-dispatcher-6] INFO  - --CONSUMED: offset: 105 message: 1643542145086
14:29:07.111 [test-consumer-akka.actor.default-dispatcher-6] INFO  - ----PRODUCED: offset: 106 message: 1643542147106
14:29:08.886 [test-consumer-akka.actor.default-dispatcher-6] INFO  - stopping consuming messages
14:29:08.891 [test-consumer-akka.actor.default-dispatcher-7] INFO  - --CONSUMED: offset: 106 message: 1643542147106
14:29:08.895 [test-consumer-akka.actor.default-dispatcher-7] INFO  - ----PRODUCED: offset: 107 message: 1643542148891 <------ this message was lost 
14:29:39.946 [test-consumer-akka.actor.default-dispatcher-7] ERROR - actor stopped
14:29:39.956 [test-consumer-akka.actor.default-dispatcher-5] INFO  - Message [akka.kafka.internal.KafkaConsumerActor$Internal$StopFromStage] from Actor[akka://test-consumer/system/Materializers/StreamSupervisor-2/$$a#1541548736] to Actor[akka://test-consumer/system/kafka-consumer-1#914599016] was not delivered. [1] dead letters encountered. If this is not an expected behavior then Actor[akka://test-consumer/system/kafka-consumer-1#914599016] may have terminated unexpectedly. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
14:29:48.866 [test-consumer-akka.actor.default-dispatcher-5] INFO  - started consuming messages
14:30:08.871 [test-consumer-akka.actor.default-dispatcher-7] INFO  - stopping consuming messages
14:30:38.896 [test-consumer-akka.actor.default-dispatcher-5] ERROR - actor stopped
```

ps: Kafka auto commit behaviour can be changed by extending BaseActorWithAutoCommit or BaseActorWithManuelCommit in [TestActor.scala](./src/main/scala/com/test/actor/TestActor.scala)