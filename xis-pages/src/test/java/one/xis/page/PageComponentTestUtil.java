package one.xis.page;

import one.xis.context.TestContext;
import one.xis.context.TestContextBuilder;
import one.xis.controller.ControllerInvocationService;
import one.xis.js.JavascriptControllerModelParser;
import one.xis.js.JavascriptTemplateParser;
import one.xis.resource.ClassPathResource;
import one.xis.template.ExpressionParser;
import one.xis.template.TemplateParser;

import static org.mockito.Mockito.mock;

class PageComponentTestUtil {

    static TestContext createCompileTestContext(Class<?> controllerClass, Object... objects) {
        return new TestContextBuilder()
                .withSingletonClass(PageComponentCompiler.class)
                .withSingletonClass(TemplateParser.class)
                .withSingletonClass(ExpressionParser.class)
                .withSingletonClass(JavascriptTemplateParser.class)
                .withSingletonClass(JavascriptControllerModelParser.class)
                .withSingletonClass(PageComponents.class)
                .withSingletonClass(PageService.class)
                .withSingletonClass(PageControllers.class)
                .withSingleton(mock(PageMetaDataFactory.class))
                .withSingleton(mock(ControllerInvocationService.class))
                .withSingletonClass(controllerClass)
                .withSingletons(objects)
                .build();
    }

    static PageComponent createPageComponent(String resourcePath, Class<?> controllerClass) {
        return new PageComponent(new ClassPathResource(resourcePath), "Test", "/test", controllerClass, false, null);
    }


}
