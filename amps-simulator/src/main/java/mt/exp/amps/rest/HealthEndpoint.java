package mt.exp.amps.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/health")
public class HealthEndpoint {

    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("HEALTHY");
    }
}
