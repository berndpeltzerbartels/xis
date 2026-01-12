package one.xis.ws.spring;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SpringWSHeartbeatScheduler {

    private final SpringWSHandler springWSHandler;

    public SpringWSHeartbeatScheduler(SpringWSHandler springWSHandler) {
        this.springWSHandler = springWSHandler;
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void sendHeartbeat() {
        springWSHandler.sendPingToAllSessions();
    }
}
