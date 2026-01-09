package one.xis.context.defaultcomponent.beanmethod;

import one.xis.context.Bean;
import one.xis.context.Component;

@Component
public class CustomConfigProvider implements ConfigProvider {
    
    @Bean
    @Override
    public Config getConfig() {
        return new Config("custom");
    }
}
