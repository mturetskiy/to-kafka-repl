package mt.exp.amps.config;

import mt.exp.amps.connect.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${app.name}")
    private String appName;
    @Value("${accept.port:6789}")
    private Integer acceptPort;

    @Bean
    public Connector connector() {
        return new Connector(appName, acceptPort);
    }
}
