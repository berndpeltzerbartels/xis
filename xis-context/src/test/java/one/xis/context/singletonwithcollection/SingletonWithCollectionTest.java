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
        // Reproduces the Spring collection-injection problem:
        // 1. A pre-existing Spring bean implements an interface collected by XIS.
        // 2. The bean is added to the singletons collection.
        // 3. An XIS component depends on Collection<MySPI>.
        // 4. Building the XIS context should still expose the pre-existing bean.
        
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
