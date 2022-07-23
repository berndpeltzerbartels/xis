package one.xis.js;

import one.xis.context.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class JsConfig {

    @Autowired
    private AppContext appContext;
    private ApiJavascriptService apiJavascriptService;

    @PostConstruct
    void init() {
        apiJavascriptService = appContext.getSingleton(ApiJavascriptService.class);
    }

    @Bean
    ApiJavascriptService apiJavascriptService() {
        return apiJavascriptService;
    }

}

