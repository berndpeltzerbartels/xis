package one.xis.context;

import one.xis.context.springlike.SpringLikeBean;
import one.xis.context.springlike.SpringLikeConfiguration;
import one.xis.context.springlike.SpringLikeProductCatalog;
import one.xis.context.springlike.SpringLikeProductConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTestContextBuilderTest {

    @Test
    void acceptsCustomComponentAndBeanMethodAnnotations() {
        var context = IntegrationTestContext.builder()
                .withBasePackageClass(SpringLikeProductConfig.class)
                .withComponentAnnotation(SpringLikeConfiguration.class)
                .withBeanMethodAnnotation(SpringLikeBean.class)
                .build();

        assertThat(context.getSingleton(SpringLikeProductCatalog.class).name()).isEqualTo("test catalog");
    }

    @Test
    void acceptsNestedCustomConfigurationClasses() {
        var context = IntegrationTestContext.builder()
                .withBasePackageClass(IntegrationTestContextBuilderTest.class)
                .withComponentAnnotation(SpringLikeConfiguration.class)
                .withBeanMethodAnnotation(SpringLikeBean.class)
                .build();

        assertThat(context.getSingleton(NestedCatalog.class).name()).isEqualTo("nested catalog");
    }

    @SpringLikeConfiguration
    static class NestedProductConfig {

        @SpringLikeBean
        NestedCatalog nestedCatalog() {
            return new NestedCatalog("nested catalog");
        }
    }

    record NestedCatalog(String name) {
    }
}
