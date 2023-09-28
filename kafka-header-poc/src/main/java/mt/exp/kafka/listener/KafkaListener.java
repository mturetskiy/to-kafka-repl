package mt.exp.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KafkaListener {
    private ApplicationContext appCtx;
    private final Properties consumerProperties;
    private final String topicName;
    private KafkaConsumer<String, String> kafkaConsumer;
    private volatile boolean isActive;
    private Thread consumerThread;

    public KafkaListener(Properties consumerProperties, String topicName, ApplicationContext appCtx) {
        this.appCtx = appCtx;
        this.consumerProperties = consumerProperties;
        this.topicName = topicName;
        initConsumer();
    }

    private void initConsumer() {
        this.kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        log.info("Created kafka consumer.");
    }

    public void startListening() {
        this.consumerThread = new Thread(() -> {
            isActive = true;
            log.info("Start listening...");

            try {
                //            kafkaConsumer.subscribe(List.of(topicName));
                TopicPartition tp1 = new TopicPartition(topicName, 0);
                TopicPartition tp2 = new TopicPartition(topicName, 1);
                TopicPartition tp3 = new TopicPartition(topicName, 2);
                kafkaConsumer.assign(List.of(tp1, tp2, tp3));
                kafkaConsumer.seekToEnd(List.of(tp1, tp2, tp3));
                long lastOffset1 = kafkaConsumer.position(tp1);
                log.info("Discovered last offset for partiotion 1: {}", lastOffset1);
                kafkaConsumer.seek(tp1, lastOffset1 - 1);

                long lastOffset2 = kafkaConsumer.position(tp2);
                log.info("Discovered last offset for partiotion 2: {}", lastOffset2);
                kafkaConsumer.seek(tp2, lastOffset2 - 1);

                long lastOffset3 = kafkaConsumer.position(tp3);
                log.info("Discovered last offset for partiotion 3: {}", lastOffset3);
                kafkaConsumer.seek(tp3, lastOffset3 - 1);

                while (isActive) {

                        log.info("Waiting for messages ...");
//                        ConsumerRecords<String, String> polled = kafkaConsumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                        ConsumerRecords<String, String> polled = kafkaConsumer.poll(Duration.ofSeconds(20));
                        log.info("Received: {} messages", polled.count());

                        AtomicReference<String> lastRec = new AtomicReference<>();
                        polled.forEach(rec -> {
                            log.info("Received record: {} : {}=>{}", rec.offset(), rec.key(), rec.value());
                            lastRec.set(rec.value());
                        });

//                        log.info("Last record of : {} chars", lastRec.get().length());


                }

            } catch (WakeupException e) {
                // ignore it
                log.error("WAKEUP", e);
            } catch (Exception e) {
                log.error("Exception during consumer thread main loop. Unable to continue. Exiting.", e);
                SpringApplication.exit(appCtx, () -> -1);
            } finally {
                kafkaConsumer.close();
                log.info("Kafka consumer has been disposed.");
            }



            log.info("Done with consumer thread.");

        }, "Consumer Thread");
        this.consumerThread.start();
        log.info("Consumer thread has been started.");
    }

    public void stop() {
        log.info("Stopping ..");
        isActive = false;
        kafkaConsumer.wakeup();

        try {
            this.consumerThread.join(3_000);
        } catch (InterruptedException e) {
            log.error("Unable to wait for consumer thread to finish.", e);
            Thread.currentThread().interrupt();
        }

        log.info("Stopped.");
    }
}
