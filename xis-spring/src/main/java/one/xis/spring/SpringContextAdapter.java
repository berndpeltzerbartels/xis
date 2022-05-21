package one.xis.spring;

import one.xis.context.AppContextInitializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
class SpringContextAdapter {

    @PostConstruct
    void init() {
        AppContextInitializer initializer = new AppContextInitializer("one.xis");
        new Thread(initializer).start();
    }
}
