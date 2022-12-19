package one.xis.page;

import one.xis.context.AppContext;
import one.xis.context.TestContextBuilder;
import one.xis.controller.ControllerInvocationService;
import one.xis.js.JavascriptControllerModelParser;
import one.xis.resource.ClassPathResource;
import one.xis.template.ExpressionParser;

import static org.mockito.Mockito.mock;

class PageComponentTestUtil {

    static AppContext createCompileTestContext(Class<?> controllerClass, Object... objects) {
        return new TestContextBuilder()
                .withSingletonClass(PageComponentCompiler.class)
                .withSingletonClass(PageTemplateDocumentParser.class)
                .withSingletonClass(ExpressionParser.class)
                .withSingletonClass(PageJavascriptTemplateParser.class)
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
