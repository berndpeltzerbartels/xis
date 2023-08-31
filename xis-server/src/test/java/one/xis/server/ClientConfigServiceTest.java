package one.xis.server;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientConfigServiceTest {


    @Nested
    class SimplePageConfigTest {

        private AppContext appContext;

        @BeforeEach
        void init() {
            appContext = AppContext.builder()
                    .withSingletonClass(ConfigServiceTestPage1.class)
                    .withSingletonClass(ClientConfigService.class)
                    .withSingletonClass(PathResolver.class)
                    .withSingletonClass(PageAttributesFactory.class)
                    .withSingletonClass(WidgetAttributesFactory.class)
                    .build();
        }

        @Test
        void getConfig() {
            var config = appContext.getSingleton(ClientConfigService.class).getConfig();

            var expectedId = "/Test1.html";
            assertThat(config.getPageAttributes()).containsKey(expectedId);
            assertThat(config.getPageIds()).contains(expectedId);

            var attributes = config.getPageAttributes().get(expectedId);
            assertThat(attributes.getActionParameterNames().get("action")).contains("string", "model");
            assertThat(attributes.getModelParameterNames()).contains("string", "model");

        }

        @lombok.Data
        static class ConfigServieTestModel {
            private int someInt;
            private String someString;
        }

        @Page("/Test1.html")
        static class ConfigServiceTestPage1 {

            @Action("action")
            void action(@ModelData("string") String string, @ModelData("model") TestModel testModel) {

            }

            @ModelData("model")
            ConfigServieTestModel pageModel(@ModelData("string") String string, @ModelData("model") TestModel testModel) {
                return new ConfigServieTestModel();
            }
        }
    }


    @Nested
    class PagePathVariablesTest {

        private AppContext appContext;

        @BeforeEach
        void init() {
            appContext = AppContext.builder()
                    .withSingletonClass(ConfigServiceTestPage2.class)
                    .withSingletonClass(ClientConfigService.class)
                    .withSingletonClass(PathResolver.class)
                    .withSingletonClass(PageAttributesFactory.class)
                    .withSingletonClass(WidgetAttributesFactory.class)
                    .build();
        }

        @Test
        void getConfig() {
            var config = appContext.getSingleton(ClientConfigService.class).getConfig();

            var expectedId = "/*/test_*/*.html";
            assertThat(config.getPageAttributes()).containsKey(expectedId);
            assertThat(config.getPageIds()).contains(expectedId);
            assertThat(config.getPageAttributes().get(expectedId).getPath().normalized()).isEqualTo(expectedId);
        }

        @lombok.Data
        static class ConfigServieTestModel {
            private int someInt;
            private String someString;
        }

        @Page("/{group}/test_{id}/{xyz}.html")
        static class ConfigServiceTestPage2 {

            @Action("action")
            void action(@ModelData("string") String string, @ModelData("model") TestModel testModel) {

            }

            @ModelData("model")
            ConfigServieTestModel pageModel(@ModelData("string") String string, @ModelData("model") TestModel testModel) {
                return new ConfigServieTestModel();
            }
        }
    }


    @Nested
    class SimpleWidgetConfigTest {

        private AppContext appContext;

        @BeforeEach
        void init() {
            appContext = AppContext.builder()
                    .withSingletonClass(ConfigServiceTestWidget1.class)
                    .withSingletonClass(ClientConfigService.class)
                    .withSingletonClass(PathResolver.class)
                    .withSingletonClass(PageAttributesFactory.class)
                    .withSingletonClass(WidgetAttributesFactory.class)
                    .build();
        }

        @Test
        void getConfig() {
            var config = appContext.getSingleton(ClientConfigService.class).getConfig();

            var widgetId = "ConfigServiceTestWidget1";
            assertThat(config.getWidgetAttributes()).containsKey(widgetId);
            assertThat(config.getWidgetIds()).contains(widgetId);

            var attributes = config.getWidgetAttributes().get(widgetId);
            assertThat(attributes.getActionParameterNames().get("action")).contains("string", "model");
            assertThat(attributes.getModelParameterNames()).contains("string", "model");

        }

        @lombok.Data
        static class ConfigServieTestModel {
            private int someInt;
            private String someString;
        }

        @Widget
        static class ConfigServiceTestWidget1 {

            @Action("action")
            void action(@ModelData("string") String string, @ModelData("model") TestModel testModel) {

            }

            @ModelData("model")
            ConfigServieTestModel pageModel(@ModelData("string") String string, @ModelData("model") TestModel testModel) {
                return new ConfigServieTestModel();
            }
        }
    }


    static class TestModel {

    }

}