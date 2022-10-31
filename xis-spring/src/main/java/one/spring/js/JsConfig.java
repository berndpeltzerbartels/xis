package one.spring.js;

import one.xis.context.AppContext;
import one.xis.root.RootPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class JsConfig {

    @Autowired
    private AppContext appContext;
    private RootPageService rootPageService;

    @PostConstruct
    void init() {
        rootPageService = appContext.getSingleton(RootPageService.class);
    }

    @Bean
    RootPageService apiJavascriptService() {
        return rootPageService;
    }

}

