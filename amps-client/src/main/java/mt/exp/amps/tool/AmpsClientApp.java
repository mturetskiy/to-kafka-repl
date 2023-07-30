package mt.exp.amps.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class})
public class AmpsClientApp {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(AmpsClientApp.class, AmpsClient.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
