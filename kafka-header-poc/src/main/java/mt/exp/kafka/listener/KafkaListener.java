package mt.exp.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
public class KafkaListener {
    private final Properties consumerProperties;
    private final String topicName;
    private KafkaConsumer<String, String> kafkaConsumer;

    private volatile boolean isActive;
    public KafkaListener(Properties consumerProperties, String topicName) {
        this.consumerProperties = consumerProperties;
        this.topicName = topicName;
        initConsumer();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void initConsumer() {
        this.kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        log.info("Created kafka consumer.");
    }

    public void startListening() {
        new Thread(() -> {
            isActive = true;
            log.info("Start listening...");
//            kafkaConsumer.subscribe(List.of(topicName));
            TopicPartition tp = new TopicPartition(topicName, 1);
            kafkaConsumer.assign(List.of(tp));
            kafkaConsumer.seekToEnd(List.of(tp));
            long lastOffset = kafkaConsumer.position(tp);
            log.info("Discovered last offset: {}", lastOffset);
            kafkaConsumer.seek(tp, lastOffset - 1);

            while(isActive) {
                log.info("Waiting for messages ...");
                ConsumerRecords<String, String> polled = kafkaConsumer.poll(Duration.ofSeconds(10));
                polled.forEach(rec -> log.info("Received record: {} : {}=>{}", rec.offset(), rec.key(), rec.value()));
            }

            log.info("Done...");

        }).start();
    }

    public void stop() {
        log.info("Stopping ..");
        isActive = false;
    }
}
