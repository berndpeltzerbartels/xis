package one.xis.spring;

import one.xis.context.AppContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
class SpringContextAdapter {

    @PostConstruct
    void init() {
        AppContext.getInstance("one.xis");
    }
}
