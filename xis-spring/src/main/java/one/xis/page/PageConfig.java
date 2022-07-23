package one.xis.page;

import one.xis.Page;
import one.xis.context.AppContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class PageConfig implements BeanPostProcessor {

    @Autowired
    private AppContext appContext;
    private PageService pageService;

    @PostConstruct
    void init() {
        pageService = appContext.getSingleton(PageService.class);
    }

    @Bean
    PageService pageService() {
        return pageService;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Page.class)) {
            pageService.addPageController(bean);
        }
        return bean;
    }

}
