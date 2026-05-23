package one.xis.boot.test;

import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.Inject;
import one.xis.context.Init;
import one.xis.context.IntegrationTestContext;
import one.xis.context.Value;
import one.xis.test.Captor;
import one.xis.test.InTestContext;
import one.xis.test.Mock;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@XisBootTest
class XisBootTestExtensionTest {

    private IntegrationTestContext context;

    private boolean initCalled;

    @Mock
    private Dependency dependencyMock;

    @Inject
    private Dependency dependency;

    @Inject
    private ProducedBean producedBean;

    @InTestContext
    private RealDependency realDependency;

    @Inject
    private RealDependency injectedRealDependency;

    @InTestContext
    private RealService realService;

    @Value("app.name")
    private String appName;

    @org.mockito.Mock
    private MockitoDependency mockitoDependencyMock;

    @Inject
    private MockitoDependency mockitoDependency;

    @Captor
    private ArgumentCaptor<String> captor;

    @Init
    void init() {
        initCalled = appName != null;
    }

    @Test
    void createsTestClassThroughXisContextAndRegistersTestFields(IntegrationTestContext context) {
        assertNotNull(context);
        assertSame(context, this.context);
        assertNotNull(captor);
        assertSame(dependencyMock, dependency);
        assertSame(dependencyMock, context.getSingleton(Dependency.class));
        assertSame(mockitoDependencyMock, mockitoDependency);
        assertSame(realDependency, injectedRealDependency);
        assertSame(realDependency, context.getSingleton(RealDependency.class));
        assertSame(realDependency, realService.realDependency);
        assertSame(realService, context.getSingleton(RealService.class));
        assertSame(producedBean, context.getSingleton(ProducedBean.class));
        assertSame(context.getSingleton(BeanDependency.class), producedBean.dependency);
        assertEquals("TestApp", appName);
        assertTrue(initCalled);
    }

    interface Dependency {
    }

    interface MockitoDependency {
    }

    static class RealDependency {
    }

    static class RealService {
        private final RealDependency realDependency;

        RealService(RealDependency realDependency) {
            this.realDependency = realDependency;
        }
    }

    @Component
    static class TestConfiguration {

        @Bean
        ProducedBean producedBean(BeanDependency dependency) {
            return new ProducedBean(dependency);
        }
    }

    @Component
    static class BeanDependency {
    }

    record ProducedBean(BeanDependency dependency) {
    }
}
