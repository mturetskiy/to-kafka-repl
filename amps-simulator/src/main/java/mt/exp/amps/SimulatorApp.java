package mt.exp.amps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SimulatorApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SimulatorApp.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
