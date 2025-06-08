package one.xis.deserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.*;
import one.xis.context.TestContextBuilder;
import one.xis.utils.lang.CollectionUtils;
import one.xis.validation.AllElementsMandatory;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeserializationTest {

    private MainDeserializer mainDeserializer;
    private UserContext userContext;

    @BeforeAll
    void init() {
        var context = new TestContextBuilder()
                .withPackage("one.xis.deserialize")
                .build();
        mainDeserializer = context.getSingleton(MainDeserializer.class);
        userContext = mock();
        when(userContext.getLocale()).thenReturn(Locale.GERMAN);
        when(userContext.getZoneId()).thenReturn(ZoneId.of("Europe/Berlin"));
        when(userContext.getUserId()).thenReturn("123");
    }

    @Test
    void deserializeIntField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intField\":123}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testBean.getIntField()).isEqualTo(123);
    }

    @Test
    void deserializeStringField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringField\":\"test\"}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testBean.getStringField()).isEqualTo("test");
    }

    @Test
    void deserializeLocalDateField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"localDateField\":\"2021-01-01\"}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testBean.getLocalDateField()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    void deserializeArrayField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringArrayField\":[\"a\",\"b\"]}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testBean.getStringArrayField()).containsExactly("a", "b");
    }

    @Test
    void deserializeCollectionField() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"stringCollectionField\":[\"a\",\"b\"]}";
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testBean.getStringCollectionField()).containsExactly("a", "b");
    }

    @Test
    void deserializeIntParameter() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodInt", int.class).getParameters()[0];
        var json = "123";
        var result = mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(result).isEqualTo(123);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deserializeCollectionParameter() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodCollection", Collection.class).getParameters()[0];
        var json = "[\"2021-01-01\",\"2021-01-02\"]";
        var result = (Collection<LocalDate>) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(result).containsExactly(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
    }

    @Test
    void deserializeFormattedField() throws NoSuchMethodException {
        var json = "{\"localDate\":\"01.01.2021\"}";
        var parameter = getClass().getDeclaredMethod("testMethodLocalDateBean", BeanWithLocalDate.class).getParameters()[0];
        var beanWithLocalDate = (BeanWithLocalDate) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(beanWithLocalDate.getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    void dateToLocalDateCollection() throws NoSuchMethodException {
        var date = getClass().getDeclaredMethod("testMethodLocalDateBean", BeanWithLocalDate.class).getParameters()[0];
        var json = "{\"localDate\": [\"01.01.2021\"]}";
        var result = (BeanWithLocalDate) mainDeserializer.deserialize(json, date, userContext, new PostProcessingResults());
        assertThat(result.getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    void dateCollectionToLocalDate() throws NoSuchMethodException {
        var date = getClass().getDeclaredMethod("testMethodLocalDateCollectionBean", BeanWithLocalDateCollection.class).getParameters()[0];
        var json = "{\"localDates\": [\"01.01.2021\", \"02.01.2021\"]}";
        var result = (BeanWithLocalDateCollection) mainDeserializer.deserialize(json, date, userContext, new PostProcessingResults());
        assertThat(result.getLocalDates()).containsExactly(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
    }

    @Test
    void integerParameterFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodInt", int.class).getParameters()[0];
        var ppObjects = new PostProcessingResults();
        mainDeserializer.deserialize("abc", parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingResults(InvalidValueError.class));
        assertThat(error.getDeserializationContext().getPath()).isEqualTo("/integer");
    }

    @Test
    void collectionParameterFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodCollection", Collection.class).getParameters()[0];
        var ppObjects = new PostProcessingResults();
        mainDeserializer.deserialize("[123]", parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(2);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).anyMatch(e -> e.getDeserializationContext().getPath().equals("/collection[0]"));
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).anyMatch(e -> e.getDeserializationContext().getPath().equals("/collection"));
    }


    @Test
    void nestedFieldFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter2", TestBean2.class).getParameters()[0];
        var json = "{\"intField\":123,\"testBeanField\":{\"intField\":\"abc\"}}";
        var ppObjects = new PostProcessingResults();
        mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingResults(InvalidValueError.class));
        assertThat(error.getDeserializationContext().getPath()).isEqualTo("/test/testBeanField/intField");
    }

    @Test
    void arrayElementFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intArrayField\":[0,1,\"a\",3]}";
        var ppObjects = new PostProcessingResults();
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(2);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class).stream().map(InvalidValueError::getDeserializationContext).map(DeserializationContext::getPath)).anyMatch("/testObject/intArrayField[2]"::equals);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class).stream().map(InvalidValueError::getDeserializationContext).map(DeserializationContext::getPath)).anyMatch("/testObject/intArrayField"::equals);
        assertThat(testBean.getIntArrayField()).containsExactly(0, 1, 0, 3);
    }

    @Test
    void collectionElementFailed() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestBean.class).getParameters()[0];
        var json = "{\"intCollectionField\":[\"a\",\"b\",3]}";
        var ppObjects = new PostProcessingResults();
        var testBean = (TestBean) mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(3);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class).stream().map(InvalidValueError::getDeserializationContext).map(DeserializationContext::getPath)).anyMatch("/testObject/intCollectionField[0]"::equals);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class).stream().map(InvalidValueError::getDeserializationContext).map(DeserializationContext::getPath)).anyMatch("/testObject/intCollectionField[1]"::equals);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class).stream().map(InvalidValueError::getDeserializationContext).map(DeserializationContext::getPath)).anyMatch("/testObject/intCollectionField"::equals);
        assertThat(testBean.getIntCollectionField()).containsExactly(null, null, 3);
    }

    @Test
    void formattedFieldFailed() throws NoSuchMethodException {
        var json = "{\"localDate\":\"01. Januar 2021\"}";
        var ppObjects = new PostProcessingResults();
        var parameter = getClass().getDeclaredMethod("testMethodLocalDateBean", BeanWithLocalDate.class).getParameters()[0];
        mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingResults(InvalidValueError.class));
        assertThat(error.getDeserializationContext().getPath()).isEqualTo("/localDateBean/localDate");
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
        var ppObjects = new PostProcessingResults();
        var parameter = getClass().getDeclaredMethod("testMethodMandatory", BeanWithMandatoryFields.class).getParameters()[0];
        var bean = (BeanWithMandatoryFields) mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(bean.getObjectField()).isNotNull();
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingResults(InvalidValueError.class));
        assertThat(error.getDeserializationContext().getPath()).isEqualTo("/model/objectField/localDateField");
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
        var ppObjects = new PostProcessingResults();
        var parameter = getClass().getDeclaredMethod("testMethodMandatory", BeanWithMandatoryFields.class).getParameters()[0];
        var bean = (BeanWithMandatoryFields) mainDeserializer.deserialize(json, parameter, userContext, ppObjects);
        assertThat(bean.getObjectField()).isNotNull();
        assertThat(ppObjects.postProcessingResults(InvalidValueError.class)).hasSize(1);
        var error = CollectionUtils.first(ppObjects.postProcessingResults(InvalidValueError.class));
        assertThat(error.getDeserializationContext().getPath()).isEqualTo("/model/objectField/localDateField");
    }


    @Test
    void deserializeRecord() throws NoSuchMethodException {
        var parameter = getClass().getDeclaredMethod("testMethodBeanParameter", TestRecord.class).getParameters()[0];
        var json = "{\"intField\":123,\"stringField\":\"test\",\"localDateField\":\"2021-01-01\"}";
        var testRecord = (TestRecord) mainDeserializer.deserialize(json, parameter, userContext, new PostProcessingResults());
        assertThat(testRecord.intField()).isEqualTo(123);
        assertThat(testRecord.stringField()).isEqualTo("test");
        assertThat(testRecord.localDateField()).isEqualTo(LocalDate.of(2021, 1, 1));
    }


    @Nested
    class PostProcessorTest {
        private MainDeserializer mainDeserializer;
        private ArgumentCaptor<DeserializationContext> deserializationContextArgumentCaptor;
        private ArgumentCaptor<Object> valueCaptor;
        private ArgumentCaptor<PostProcessingResults> postPricessingObjectCaptor;
        private TestPostProcessor postProcessorMock;
        private final PostProcessingResults postProcessingResults = new PostProcessingResults();
        private Parameter parameter;

        @BeforeEach
        void initArgumentCaptors() {
            deserializationContextArgumentCaptor = ArgumentCaptor.forClass(DeserializationContext.class);
            valueCaptor = ArgumentCaptor.forClass(Object.class);
            postPricessingObjectCaptor = ArgumentCaptor.forClass(PostProcessingResults.class);
        }


        @BeforeEach
        void initContext() {
            postProcessorMock = mock(TestPostProcessor.class);
            var context = new TestContextBuilder()
                    .withSingletonClass(ArrayDeserializer.class)
                    .withSingletonClass(CollectionDeserializer.class)
                    .withSingletonClass(EnumDeserializer.class)
                    .withSingletonClass(FormattedDeserializer.class)
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
        void deserialize() throws NoSuchFieldException {
            var json = """
                    {
                        "beanField": {
                            "localDate": "2021-01-01"
                        }
                    }""";

            var result = ((PostProcessorTestBean1) mainDeserializer.deserialize(json, parameter, userContext, postProcessingResults));

            assertThat(result).isNotNull();
            assertThat(postProcessingResults.postProcessingResults(InvalidValueError.class)).isEmpty();
            assertThat(result.getBeanField()).isNotNull();
            assertThat(result.getBeanField().getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));

            verify(postProcessorMock, times(3)).postProcess(deserializationContextArgumentCaptor.capture(), valueCaptor.capture(), postPricessingObjectCaptor.capture());

            var paths = deserializationContextArgumentCaptor.getAllValues().stream().map(DeserializationContext::getPath).toList();
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

            var targets = deserializationContextArgumentCaptor.getAllValues().stream().map(DeserializationContext::getTarget).toList();
            assertThat(targets).containsExactlyInAnyOrder(
                    PostProcessorTestBean2.class.getDeclaredField("localDate"),
                    PostProcessorTestBean1.class.getDeclaredField("beanField"),
                    parameter
            );

            var annotationClasses = deserializationContextArgumentCaptor.getAllValues().stream()
                    .map(DeserializationContext::getAnnotationClass)
                    .map(Class.class::cast)
                    .toList();
            assertThat(annotationClasses).containsExactlyInAnyOrder(
                    PostProcessorTestAnnotation.class,
                    PostProcessorTestAnnotation.class,
                    PostProcessorTestAnnotation.class);

            var userContexts = deserializationContextArgumentCaptor.getAllValues().stream().map(DeserializationContext::getUserContext).toList();
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

    void testMethodBeanParameter(@FormData("testObject") TestRecord testRecord) {

    }

    @SuppressWarnings("unused")
    void testMethodBeanParameter2(@FormData("test") TestBean2 testBean) {

    }


    @SuppressWarnings("unused")
    void testMethodInt(@URLParameter("integer") int i) {

    }

    @SuppressWarnings("unused")
    void testMethodCollection(@FormData("collection") @AllElementsMandatory Collection<LocalDate> collection) {

    }


    @SuppressWarnings("unused")
    void testMethodLocalDateBean(@FormData("localDateBean") BeanWithLocalDate bean) {

    }

    @SuppressWarnings("unused")
    void testMethodLocalDateCollectionBean(@FormData("localDateBean") BeanWithLocalDateCollection bean) {

    }

    @SuppressWarnings("unused")
    void testMethodMandatory(@FormData("model") BeanWithMandatoryFields bean) {

    }

    @SuppressWarnings("unused")
    void testMethodPostProcessing(@FormData("model") @PostProcessorTestAnnotation PostProcessorTestBean1 bean) {

    }


    @Data
    static class TestBean {
        private int intField;
        private String stringField;
        private LocalDate localDateField;

        @AllElementsMandatory
        private String[] stringArrayField;

        @AllElementsMandatory
        private int[] intArrayField;

        @AllElementsMandatory
        private Collection<String> stringCollectionField;

        @AllElementsMandatory
        private Collection<Integer> intCollectionField;
    }

    @Data
    static class TestBean2 {
        private int intField;
        private TestBean testBeanField;
    }

    record TestRecord(int intField, String stringField, LocalDate localDateField) {
        // This is just a record to test deserialization of records
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class BeanWithLocalDate {

        @UseFormatter(LocalDateFormatter.class)
        private LocalDate localDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class BeanWithLocalDateCollection {

        @AllElementsMandatory
        @UseFormatter(LocalDateFormatter.class)
        private Collection<LocalDate> localDates;
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
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDate(FormatStyle.MEDIUM) // oder SHORT, LONG
                    .withLocale(locale)
                    .withZone(zoneId);
            return formatter.format(localDate);
        }

        @Override
        public LocalDate parse(String s, Locale locale, ZoneId zoneId) {
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDate(FormatStyle.MEDIUM) // oder SHORT, LONG
                    .withLocale(locale)
                    .withZone(zoneId);
            try {
                return LocalDate.parse(s, formatter);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }

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