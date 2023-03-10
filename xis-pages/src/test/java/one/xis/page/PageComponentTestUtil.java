package one.xis.page;

import one.xis.context.AppContext;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerInvocationService;
import one.xis.resource.ClassPathResource;
import one.xis.resource.Resources;
import one.xis.template.ExpressionParser;
import one.xis.test.js.JavascriptControllerModelParser;

import static org.mockito.Mockito.mock;

class PageComponentTestUtil {

    static AppContext createCompileTestContext(Class<?> controllerClass, Object... objects) {
        return AppContext.builder()
                .withSingletonClass(PageComponentCompiler.class)
                .withSingletonClass(PageTemplateDocumentParser.class)
                .withSingletonClass(ExpressionParser.class)
                .withSingletonClass(PageJavascriptTemplateParser.class)
                .withSingletonClass(JavascriptControllerModelParser.class)
                .withSingletonClass(PageComponents.class)
                .withSingletonClass(PageService.class)
                .withSingletonClass(PageControllers.class)
                .withSingelton(mock(PageMetaDataFactory.class))
                .withSingelton(mock(ControllerInvocationService.class))
                .withSingletonClass("one.xis.controller.ControllerParameterProvider")
                .withSingletonClasses(Resources.class)
                .withSingletonClass(controllerClass)
                .withSingeltons(objects)
                .withComponentAnnotation(XISComponent.class)

                .build();
    }

    static PageComponent createPageComponent(String resourcePath, Class<?> controllerClass) {
        return new PageComponent(new ClassPathResource(resourcePath), "Test", "/test", controllerClass, false, null);
    }


}
