package one.xis.ws.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SpringWebSocketConfig.class, SpringWSHandler.class, SpringWSHeartbeatScheduler.class})
public class XisWebSocketAutoConfiguration {
}
