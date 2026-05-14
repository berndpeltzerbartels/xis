package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectMethodHandlerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:select-method-handler;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Ada'), (2, 'Grace')");
        }
    }

    @Test
    void selectsSingleObject() throws ReflectiveOperationException {
        Person person = invoke("findById", new Class[]{long.class}, 1L);

        assertEquals(1L, person.id);
        assertEquals("Ada", person.firstName);
    }

    @Test
    void selectsObjectList() throws ReflectiveOperationException {
        List<Person> people = invoke("findAll", new Class[0]);

        assertEquals(2, people.size());
        assertEquals("Ada", people.get(0).firstName);
        assertEquals("Grace", people.get(1).firstName);
    }

    @Test
    void selectsScalarValue() throws ReflectiveOperationException {
        String name = invoke("findNameById", new Class[]{long.class}, 2L);

        assertEquals("Grace", name);
    }

    @Test
    void selectsScalarList() throws ReflectiveOperationException {
        List<String> names = invoke("findNames", new Class[0]);

        assertEquals(List.of("Ada", "Grace"), names);
    }

    @Test
    void selectsOptionalObject() throws ReflectiveOperationException {
        Optional<Person> person = invoke("findOptionalById", new Class[]{long.class}, 99L);

        assertTrue(person.isEmpty());
    }

    @Test
    void selectsWithNamedParameters() throws ReflectiveOperationException {
        Person person = invoke("findByIdAndName", new Class[]{long.class, String.class}, 1L, "Ada");

        assertEquals(1L, person.id);
        assertEquals("Ada", person.firstName);
    }

    @Test
    void acceptsQuotedNamedParameters() throws ReflectiveOperationException {
        Person person = invoke("findByQuotedName", new Class[]{String.class}, "Grace");

        assertEquals(2L, person.id);
        assertEquals("Grace", person.firstName);
    }

    @Test
    void acceptsEntityPropertiesAsNamedParameters() throws ReflectiveOperationException {
        PersonFilter filter = new PersonFilter();
        filter.id = 1L;
        filter.firstName = "Ada";

        Person person = invoke("findByEntityFilter", new Class[]{PersonFilter.class}, filter);

        assertEquals(1L, person.id);
        assertEquals("Ada", person.firstName);
    }

    @Test
    void rejectsNamedSqlWithoutMatchingMethodParameter() throws ReflectiveOperationException {
        Method method = PersonQueries.class.getDeclaredMethod("brokenNamedParameter", long.class);
        var handler = new SelectMethodHandler(dataSource, new ROMapper());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("No @Param(\"missing\") on " + method, exception.getMessage());
    }

    @Test
    void rejectsUnusedMethodParameterInNamedSql() throws ReflectiveOperationException {
        Method method = PersonQueries.class.getDeclaredMethod("unusedNamedParameter", long.class, String.class);
        var handler = new SelectMethodHandler(dataSource, new ROMapper());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("Named @Select parameters do not match method parameters on " + method, exception.getMessage());
    }

    @Test
    void rejectsPositionalParameterCountMismatch() throws ReflectiveOperationException {
        Method method = PersonQueries.class.getDeclaredMethod("positionalMismatch", long.class, String.class);
        var handler = new SelectMethodHandler(dataSource, new ROMapper());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("@Select parameter count mismatch on " + method
                + ": SQL has 1 placeholders but method has 2 parameters", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = PersonQueries.class.getDeclaredMethod(methodName, parameterTypes);
        var handler = new SelectMethodHandler(dataSource, new ROMapper());
        handler.init(method);
        return (T) handler.invoke(args);
    }

    interface PersonQueries {

        @Select("select id, first_name from people where id = ?")
        Person findById(long id);

        @Select("select id, first_name from people order by id")
        List<Person> findAll();

        @Select("select first_name from people where id = ?")
        String findNameById(long id);

        @Select("select first_name from people order by id")
        List<String> findNames();

        @Select("select id, first_name from people where id = ?")
        Optional<Person> findOptionalById(long id);

        @Select("select id, first_name from people where id = {id} and first_name = {name}")
        Person findByIdAndName(@Param("id") long id, @Param("name") String name);

        @Select("select id, first_name from people where first_name = '{name}'")
        Person findByQuotedName(@Param("name") String name);

        @Select("select id, first_name from people where id = {id} and first_name = {firstName}")
        Person findByEntityFilter(PersonFilter filter);

        @Select("select id, first_name from people where id = {missing}")
        Person brokenNamedParameter(@Param("id") long id);

        @Select("select id, first_name from people where id = {id}")
        Person unusedNamedParameter(@Param("id") long id, @Param("name") String name);

        @Select("select id, first_name from people where id = ?")
        Person positionalMismatch(long id, String name);
    }

    static class Person {
        long id;
        String firstName;
    }

    @Entity("people")
    static class PersonFilter {
        long id;
        String firstName;
    }
}
