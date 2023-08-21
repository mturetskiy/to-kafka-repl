package mt.exp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class KafkaPocApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KafkaPocApp.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
