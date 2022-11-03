package one.xis.spring.ajax;

import one.xis.context.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xis.ajax.AjaxService;

import javax.annotation.PostConstruct;

@Configuration
class AjaxConfig {

    @Autowired
    private AppContext appContext;
    private AjaxService ajaxService;

    @PostConstruct
    void init() {
        ajaxService = appContext.getSingleton(AjaxService.class);
    }

    @Bean
    AjaxService connectorService() {
        return ajaxService;
    }
}
