package one.xis.parameter;

class ParameterDeserializerImplTest {

    /*

    @Nested
    class SimpleObjectTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class SimpleObject {
            private String name;
        }

        @Test
        void test() throws IOException {
            var json = "{\"name\":\"test\"}";
            var o = (SimpleObject) deserializer.deserialize(json, SimpleObject.class, new HashMap<>(), new UserContext());
            assertEquals("test", o.getName());
        }
    }

    @Nested
    class ObjectWithCollectionTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class ObjectWithCollection {
            private Collection<String> names;
        }

        @Test
        void test() throws IOException {
            var json = "{\"names\":[\"test1\",\"test2\"]}";
            var o = (ObjectWithCollection) deserializer.deserialize(json, ObjectWithCollection.class, new HashMap<>(), new UserContext());
            assertEquals(2, o.getNames().size());
            assertEquals("test1", o.getNames().iterator().next());
        }
    }

    @Nested
    class ObjectWithArrayTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class ObjectWithArray {
            private String[] names;
        }

        @Test
        void test() throws IOException {
            var json = "{\"names\":[\"test1\",\"test2\"]}";
            var o = (ObjectWithArray) deserializer.deserialize(json, ObjectWithArray.class, new HashMap<>(), new UserContext());
            assertEquals(2, o.getNames().length);
            assertEquals("test1", o.getNames()[0]);
        }
    }

    @Nested
    class ObjectWithCollectionOfObjectsTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class SimpleObject {
            private String name;
        }

        @Data
        static class ObjectWithCollectionOfObjects {
            private Collection<SimpleObject> objects;
        }

        @Test
        void test() throws IOException {
            var json = "{\"objects\":[{\"name\":\"test1\"},{\"name\":\"test2\"}]}";
            var o = (ObjectWithCollectionOfObjects) deserializer.deserialize(json, ObjectWithCollectionOfObjects.class, new HashMap<>(), new UserContext());
            assertEquals(2, o.getObjects().size());
            assertThat(o.getObjects().stream().map(SimpleObject::getName).toList()).containsExactly("test1", "test2");
        }
    }

    @Nested
    class ComplexObjectTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class SimpleObject {
            private String name;
        }

        @Data
        static class ObjectWithCollectionOfObjects {
            private Collection<SimpleObject> objects;
        }

        @Data
        static class ComplexObject {
            private ObjectWithCollectionOfObjects objectWithCollectionOfObjects;
        }

        @Test
        void test() throws IOException {
            var json = "{\"objectWithCollectionOfObjects\":{\"objects\":[{\"name\":\"test1\"},{\"name\":\"test2\"}]}}";
            var o = (ComplexObject) deserializer.deserialize(json, ComplexObject.class, new HashMap<>(), new UserContext());
            assertEquals(2, o.getObjectWithCollectionOfObjects().getObjects().size());
            assertThat(o.getObjectWithCollectionOfObjects().getObjects().stream().map(SimpleObject::getName).toList()).containsExactly("test1", "test2");
        }
    }

    @Nested
    class SimpleAssigmentErrorTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class ObjectWithIntField {
            private int number;
        }

        @Test
        void test() throws IOException {
            var json = "{\"number\":\"test\"}";
            var errors = new HashMap<String, ValidationError>();
            var object = deserializer.deserialize(json, ObjectWithIntField.class, errors, new UserContext());

            assertThat(errors).hasSize(1);
            assertThat(object).isNotNull();
            assertThat(errors).containsKey("/number[0]");
            assertThat(errors.get("/number[0]")).isInstanceOf(ValidationFieldInjectionError.class);
            assertThat(((ValidationFieldInjectionError) errors.get("/number[0]")).getThrowable()).isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    class CollectionValueAssignmentError {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class ObjectWithCollectionOfInts {
            private Collection<Integer> numbers;
        }

        @Test
        void test() throws IOException {
            var json = "{\"numbers\":[1,2, \"test\"]}";
            var errors = new HashMap<String, ValidationError>();
            var object = deserializer.deserialize(json, ObjectWithCollectionOfInts.class, errors, new UserContext());

            assertThat(errors).hasSize(1);
            assertThat(object).isNotNull();
            assertThat(errors).containsKey("/numbers[2]");
            assertThat(errors.get("/numbers[2]")).isInstanceOf(ValidationFieldInjectionError.class);
            assertThat(((ValidationFieldInjectionError) errors.get("/numbers[2]")).getThrowable()).isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    class DeepErrorTest {

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), emptySet());

        @Data
        static class A {
            private B b;
        }

        @Data
        static class B {
            private C[] c;
        }

        @Data
        static class C {
            private int i;
        }

        @Test
        void test() throws IOException {
            var json = """
                    {
                        "b":{
                            "c": [
                                {i:123},
                                {i:"test"}
                            ]
                        }
                    }
                    """;
            var errors = new HashMap<String, ValidationError>();
            var object = deserializer.deserialize(json, A.class, errors, new UserContext());

            assertThat(errors).hasSize(1);
            assertThat(object).isNotNull();
            assertThat(errors).containsKey("/b[0]/c[1]/i[0]");
            assertThat(errors.get("/b[0]/c[1]/i[0]")).isInstanceOf(ValidationFieldInjectionError.class);
            assertThat(((ValidationFieldInjectionError) errors.get("/b[0]/c[1]/i[0]")).getThrowable()).isInstanceOf(NumberFormatException.class);
        }

    }

    @Nested
    class CustomFieldFormatTest {


        static class TestFieldFormat implements FieldFormat<LocalDate> {

            @Override
            public String format(LocalDate localDate, Locale locale, ZoneId zoneId) {
                return localDate.toString();
            }

            @Override
            public LocalDate parse(String s, Locale locale, ZoneId zoneId) {
                return LocalDate.parse(s.replaceAll("Day: (\\d{2}), Month: (\\d{2}), Year: (\\d{4})", "$3-$2-$1"));
            }
        }

        private final ParameterDeserializerImpl deserializer = new ParameterDeserializerImpl(new GsonConfig().gson(), Set.of(new TestFieldFormat()));

        @Data
        static class CustomType {

            @Format(TestFieldFormat.class)
            private LocalDate value;
        }


        @Test
        void test() throws IOException {
            var json = "{\"value\":\"Day: 01, Month: 08, Year: 2017\"}";
            var errors = new HashMap<String, ValidationError>();
            var object = (CustomType) deserializer.deserialize(json, CustomType.class, errors, new UserContext());

            assertThat(errors).isEmpty();
            assertThat(object.getValue().compareTo(LocalDate.of(2017, 8, 1))).isEqualTo(0);
        }
    }

     */


}