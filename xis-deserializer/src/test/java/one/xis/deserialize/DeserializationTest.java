package one.xis.deserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.*;
import one.xis.context.TestContextBuilder;
import one.xis.utils.lang.CollectionUtils;
import one.xis.validation.Mandatory;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeserializationTest {

    private MainDeserializer mainDeserializer;

    @BeforeAll
    void init() {
        var context = new TestContextBuilder()
                .withPackage("one.xis.deserialize")
                .build();
        mainDeserializer = context.getSingleton(MainDeserializer.class);
    }

    @Test
    void deserializeIntField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intField\":123}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(testBean.getIntField()).isEqualTo(123);
    }

    @Test
    void deserializeStringField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringField\":\"test\"}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(testBean.getStringField()).isEqualTo("test");
    }

    @Test
    void deserializeLocalDateField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"localDateField\":\"2021-01-01\"}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(testBean.getLocalDateField()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    void deserializeArrayField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringArrayField\":[\"a\",\"b\"]}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(testBean.getStringArrayField()).containsExactly("a", "b");
    }

    @Test
    void deserializeCollectionField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringCollectionField\":[\"a\",\"b\"]}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(testBean.getStringCollectionField()).containsExactly("a", "b");
    }

    @Test
    void deserializeIntParameter() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodInt", int.class).getParameters()[0];
        var json = "123";
        var result = mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(result).isEqualTo(123);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deserializeCollectionParameter() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodCollection", Collection.class).getParameters()[0];
        var json = "[\"2021-01-01\",\"2021-01-02\"]";
        var result = (Collection<LocalDate>) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(result).containsExactly(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
    }

    @Test
    void deserializeFormattedField() throws NoSuchMethodException {
        var json = "{\"localDate\":\"01.01.2021\"}";
        var parameter = getClass().getDeclaredMethod("testMethodLocalDateBean", BeanWithLocalDate.class).getParameters()[0];
        var beanWithLocalDate = (BeanWithLocalDate) mainDeserializer.deserialize(json, parameter, new UserContext(), new PostProcessingObjects());
        assertThat(beanWithLocalDate.getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    void integerParameterFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodInt", int.class).getParameters()[0];
        var ppObjects = new PostProcessingObjects();
        mainDeserializer.deserialize("abc", parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingObjects(InvalidValueError.class));
        assertThat(error.getReportedErrorContext().getPath()).isEqualTo("/integer");
    }

    @Test
    void collectionParameterFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodCollection", Collection.class).getParameters()[0];
        var ppObjects = new PostProcessingObjects();
        mainDeserializer.deserialize("[123]", parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(2);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).anyMatch(e -> e.getReportedErrorContext().getPath().equals("/collection[0]"));
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).anyMatch(e -> e.getReportedErrorContext().getPath().equals("/collection"));
    }

    @Test
    void nestedFieldFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter2", TestBean2.class).getParameters()[0];
        var json = "{\"intField\":123,\"testBeanField\":{\"intField\":\"abc\"}}";
        var ppObjects = new PostProcessingObjects();
        mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingObjects(InvalidValueError.class));
        assertThat(error.getReportedErrorContext().getPath()).isEqualTo("/test/testBeanField/intField");
    }

    @Test
    void arrayElementFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intArrayField\":[0,1,\"a\",3]}";
        var ppObjects = new PostProcessingObjects();
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(2);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class).stream().map(InvalidValueError::getReportedErrorContext).map(ReportedErrorContext::getPath)).anyMatch("/testObject/intArrayField[2]"::equals);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class).stream().map(InvalidValueError::getReportedErrorContext).map(ReportedErrorContext::getPath)).anyMatch("/testObject/intArrayField"::equals);
        assertThat(testBean.getIntArrayField()).containsExactly(0, 1, 0, 3);
    }

    @Test
    void collectionElementFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intCollectionField\":[\"a\",\"b\",3]}";
        var ppObjects = new PostProcessingObjects();
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(3);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class).stream().map(InvalidValueError::getReportedErrorContext).map(ReportedErrorContext::getPath)).anyMatch("/testObject/intCollectionField[0]"::equals);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class).stream().map(InvalidValueError::getReportedErrorContext).map(ReportedErrorContext::getPath)).anyMatch("/testObject/intCollectionField[1]"::equals);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class).stream().map(InvalidValueError::getReportedErrorContext).map(ReportedErrorContext::getPath)).anyMatch("/testObject/intCollectionField"::equals);
        assertThat(testBean.getIntCollectionField()).containsExactly(null, null, 3);
    }

    @Test
    void formattedFieldFailed() throws NoSuchMethodException {
        var json = "{\"localDate\":\"01. Januar 2021\"}";
        var ppObjects = new PostProcessingObjects();
        var parameter = getClass().getDeclaredMethod("testMethodLocalDateBean", BeanWithLocalDate.class).getParameters()[0];
        mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingObjects(InvalidValueError.class));
        assertThat(error.getReportedErrorContext().getPath()).isEqualTo("/localDateBean/localDate");
    }

    @Test
    @DisplayName("Missing mandatory field-value in nested object fieldl")
    void missingMandatoryField1() throws NoSuchMethodException {
        var json = """
                {
                    "stringField": "123",
                    "objectField": {
                        "localDateField": null
                        
                    }
                }""";
        var ppObjects = new PostProcessingObjects();
        var parameter = getClass().getDeclaredMethod("testMethodMandatory", BeanWithMandatoryFields.class).getParameters()[0];
        var bean = (BeanWithMandatoryFields) mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(bean.getObjectField()).isNotNull();
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingObjects(InvalidValueError.class));
        assertThat(error.getReportedErrorContext().getPath()).isEqualTo("/model/objectField/localDateField");
    }

    @Test
    @DisplayName("Missing mandatory object field value in nested object fieldl")
    void missingMandatoryField2() throws NoSuchMethodException {
        var json = """
                {
                    "stringField": "123",
                    "objectField": {
                        "localDateField": null
                        
                    }
                }""";
        var ppObjects = new PostProcessingObjects();
        var parameter = getClass().getDeclaredMethod("testMethodMandatory", BeanWithMandatoryFields.class).getParameters()[0];
        var bean = (BeanWithMandatoryFields) mainDeserializer.deserialize(json, parameter, new UserContext(), ppObjects);
        assertThat(bean.getObjectField()).isNotNull();
        assertThat(ppObjects.postProcessingObjects(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingObjects(InvalidValueError.class));
        assertThat(error.getReportedErrorContext().getPath()).isEqualTo("/model/objectField/localDateField");
    }

    @Nested
    class PostProcessorTest {
        private MainDeserializer mainDeserializer;
        private ArgumentCaptor<ReportedErrorContext> deserializationContextArgumentCaptor;
        private ArgumentCaptor<Object> valueCaptor;
        private ArgumentCaptor<PostProcessingObjects> postPricessingObjectCaptor;
        private TestPostProcessor postProcessorMock;
        private final UserContext userContext = new UserContext();
        private final PostProcessingObjects postProcessingObjects = new PostProcessingObjects();
        private Parameter parameter;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void initArgumentCaptors() {
            deserializationContextArgumentCaptor = ArgumentCaptor.forClass(ReportedErrorContext.class);
            valueCaptor = ArgumentCaptor.forClass(Object.class);
            postPricessingObjectCaptor = ArgumentCaptor.forClass(PostProcessingObjects.class);
        }


        @BeforeEach
        void initContext() {
            postProcessorMock = mock(TestPostProcessor.class);
            var context = new TestContextBuilder()
                    .withSingletonClass(ArrayDeserializer.class)
                    .withSingletonClass(CollectionDeserializer.class)
                    .withSingletonClass(EnumDeserializer.class)
                    .withSingletonClass(FormattedDeserializer.class)
                    .withSingletonClass(JsonDeserializer.class)
                    .withSingletonClass(LocalDateDeserializer.class)
                    .withSingletonClass(MainDeserializer.class)
                    .withSingletonClass(NumberDeserializer.class)
                    .withSingletonClass(ObjectDeserializer.class)
                    .withSingletonClass(StringDeserializer.class)
                    .withSingletonClass(PostProcessing.class)
                    .withSingleton(postProcessorMock).build();
            mainDeserializer = context.getSingleton(MainDeserializer.class);
        }

        @BeforeEach
        void initParameter() throws NoSuchMethodException {
            parameter = DeserializationTest.class.getDeclaredMethod("testMethodPostProcessing", PostProcessorTestBean1.class).getParameters()[0];
        }

        @Test
        void deserialize() {
            var json = """
                    {
                        "beanField": {
                            "localDate": "2021-01-01"
                        }
                    }""";

            var result = ((PostProcessorTestBean1) mainDeserializer.deserialize(json, parameter, userContext, postProcessingObjects));

            assertThat(result).isNotNull();
            assertThat(postProcessingObjects.postProcessingObjects(InvalidValueError.class)).isEmpty();
            assertThat(result.getBeanField()).isNotNull();
            assertThat(result.getBeanField().getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));
        }


        @AfterEach
        void doVerification() throws NoSuchFieldException {
            verify(postProcessorMock, times(3)).postProcess(deserializationContextArgumentCaptor.capture(), valueCaptor.capture(), postPricessingObjectCaptor.capture());

            var paths = deserializationContextArgumentCaptor.getAllValues().stream().map(ReportedErrorContext::getPath).toList();
            assertThat(paths).containsExactlyInAnyOrder(
                    "/model/beanField/localDate",
                    "/model/beanField",
                    "/model"
            );

            assertThat(valueCaptor.getAllValues()).containsExactlyInAnyOrder(
                    LocalDate.of(2021, 1, 1),
                    new PostProcessorTestBean2(LocalDate.of(2021, 1, 1)),
                    new PostProcessorTestBean1(new PostProcessorTestBean2(LocalDate.of(2021, 1, 1)))
            );

            var targets = deserializationContextArgumentCaptor.getAllValues().stream().map(ReportedErrorContext::getTarget).toList();
            assertThat(targets).containsExactlyInAnyOrder(
                    PostProcessorTestBean2.class.getDeclaredField("localDate"),
                    PostProcessorTestBean1.class.getDeclaredField("beanField"),
                    parameter
            );

            var annotationClasses = deserializationContextArgumentCaptor.getAllValues().stream()
                    .map(ReportedErrorContext::getAnnotationClass)
                    .map(Class.class::cast)
                    .toList();
            assertThat(annotationClasses).containsExactlyInAnyOrder(
                    PostProcessorTestAnnotation.class,
                    PostProcessorTestAnnotation.class,
                    PostProcessorTestAnnotation.class);

            var userContexts = deserializationContextArgumentCaptor.getAllValues().stream().map(ReportedErrorContext::getUserContext).toList();
            assertThat(userContexts).containsExactlyInAnyOrder(
                    userContext,
                    userContext,
                    userContext
            );

        }

    }


    @SuppressWarnings("unused")
    void testMethodBeanParameter(@FormData("testObject") TestBean testBean) {

    }

    @SuppressWarnings("unused")
    void testMethodBeanParameter2(@FormData("test") TestBean2 testBean) {

    }


    @SuppressWarnings("unused")
    void testMethodInt(@ModelData("integer") int i) {

    }

    @SuppressWarnings("unused")
    void testMethodCollection(@ModelData("collection") Collection<LocalDate> collection) {

    }

    @SuppressWarnings("unused")
    void testMethodLocalDateBean(@ModelData("localDateBean") BeanWithLocalDate bean) {

    }

    @SuppressWarnings("unused")
    void testMethodMandatory(@ModelData("model") BeanWithMandatoryFields bean) {

    }

    @SuppressWarnings("unused")
    void testMethodPostProcessing(@ModelData("model") @PostProcessorTestAnnotation PostProcessorTestBean1 bean) {

    }


    @Data
    static class TestBean {
        private int intField;
        private String stringField;
        private LocalDate localDateField;
        private String[] stringArrayField;
        private int[] intArrayField;
        private Collection<String> stringCollectionField;
        private Collection<Integer> intCollectionField;
    }

    @Data
    static class TestBean2 {
        private int intField;
        private TestBean testBeanField;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class BeanWithLocalDate {

        @Format(LocalDateFormatter.class)
        private LocalDate localDate;
    }

    @Data
    static class BeanWithMandatoryFields {

        @Mandatory
        private String stringField;

        @Mandatory
        private BeanWithMandatoryField objectField;
    }

    @Data
    static class BeanWithMandatoryField {

        @Mandatory
        private LocalDate localDateField;
    }

    static class LocalDateFormatter implements Formatter<LocalDate> {

        @Override
        public String format(LocalDate localDate, Locale locale, ZoneId zoneId) {
            return String.format("%02d.%02d.%d", localDate.getDayOfMonth(), localDate.getMonthValue(), localDate.getYear());
        }

        @Override
        public LocalDate parse(String s, Locale locale, ZoneId zoneId) {
            var split = s.split("\\.");
            if (split.length == 3) {
                return LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));
            }
            throw new IllegalArgumentException("Invalid date format");
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @PostProcessor(TestPostProcessor.class)
    @interface PostProcessorTestAnnotation {

    }

    interface TestPostProcessor extends DeserializationPostProcessor {

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PostProcessorTestBean1 {

        @PostProcessorTestAnnotation
        private PostProcessorTestBean2 beanField;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PostProcessorTestBean2 {

        @PostProcessorTestAnnotation
        LocalDate localDate;
    }
}