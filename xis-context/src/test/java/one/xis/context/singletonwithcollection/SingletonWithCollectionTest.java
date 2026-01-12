package one.xis.context.singletonwithcollection;

import one.xis.context.AppContext;
import one.xis.context.AppContextBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class SingletonWithCollectionTest {

    @Test
    void shouldInjectPreExistingSingletonIntoCollectionDependency() {
        // Reproduziert das Spring-Problem:
        // 1. Spring Bean (SpringWSHandler) implementiert @ImportInstances Interface (SpringWSHandlerSPI)
        // 2. Diese Bean wird in singletons Collection gestopft
        // 3. XIS Component (SpringWebSocketInitializer) braucht Collection<SpringWSHandlerSPI>
        // 4. Der XIS Context wird gebaut - sollte die Spring Bean finden
        
        MyImplementation preExistingBean = new MyImplementation("spring-bean");
        
        AppContext context = AppContextBuilder.createInstance()
                .withSingletons(List.of(preExistingBean))
                .withSingletonClasses(ConsumerWithCollection.class)
                .build();

        var consumer = findElementOfType(context.getSingletons(), ConsumerWithCollection.class);
        
        assertThat(consumer).isNotNull();
        assertThat(consumer.getInstances())
                .as("Collection should contain the pre-existing singleton")
                .hasSize(1)
                .contains(preExistingBean);
    }
}
