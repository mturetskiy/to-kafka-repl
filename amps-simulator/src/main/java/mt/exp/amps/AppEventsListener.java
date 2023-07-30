package mt.exp.amps;

import lombok.extern.slf4j.Slf4j;
import mt.exp.amps.connect.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppEventsListener {
    @Autowired
    private Connector connector;

    @EventListener
    public void onAppReady(ApplicationReadyEvent event) {
        log.info("Application has been ready.");
        connector.startAccepting();
    }

    @EventListener
    public void onAppStarted(ApplicationStartedEvent event) {
        log.info("Application has been started.");
    }

    @EventListener
    public void ctxClosed(ContextClosedEvent event) {
        log.info("App ctx closed.");
    }
}
