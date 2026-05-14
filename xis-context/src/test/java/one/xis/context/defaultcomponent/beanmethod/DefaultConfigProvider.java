package one.xis.context.defaultcomponent.beanmethod;

import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

@Component
@DefaultComponent
public class DefaultConfigProvider implements ConfigProvider {
    
    @Bean
    @Override
    public Config getConfig() {
        return new Config("default");
    }
}
