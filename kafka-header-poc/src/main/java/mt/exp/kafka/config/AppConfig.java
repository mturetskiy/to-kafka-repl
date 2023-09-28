package mt.exp.kafka.config;

import mt.exp.kafka.listener.KafkaListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.OffsetResetStrategy.LATEST;

@EnableAsync
@Configuration
public class AppConfig {
    @Value("${kafka.listener.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.listener.topic}")
    private String topicName;

    @Bean
    public Properties consumerProps() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaListenerPoc");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, LATEST.toString());
        return consumerProperties;
    }

    @Bean(initMethod = "startListening", destroyMethod = "stop")
    public KafkaListener listener(Properties consumerProps, ApplicationContext applicationContext) {
        return new KafkaListener(consumerProps, topicName, applicationContext);
    }

}
