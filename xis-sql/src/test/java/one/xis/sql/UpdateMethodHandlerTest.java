package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateMethodHandlerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:update-method-handler;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists employee_roles");
            statement.execute("drop table if exists optional_people");
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("create table optional_people (id bigint primary key, first_name varchar(100))");
            statement.execute("""
                    create table employee_roles (
                        employee_id bigint,
                        role_id bigint,
                        label varchar(100),
                        primary key (employee_id, role_id)
                    )
                    """);
            statement.execute("insert into people values (1, 'Ada')");
            statement.execute("insert into employee_roles values (1, 10, 'User')");
        }
    }

    @Test
    void updatesAndReturnsBoolean() throws ReflectiveOperationException, SQLException {
        boolean updated = invoke("rename", new Class[]{long.class, String.class}, 1L, "Grace");

        assertTrue(updated);
        assertName("Grace");
    }

    @Test
    void acceptsQuotedNamedParameters() throws ReflectiveOperationException, SQLException {
        boolean updated = invoke("renameWithQuotedParameter", new Class[]{long.class, String.class}, 1L, "Grace");

        assertTrue(updated);
        assertName("Grace");
    }

    @Test
    void insertsAndReturnsUpdateCount() throws ReflectiveOperationException, SQLException {
        int inserted = invoke("insert", new Class[]{long.class, String.class}, 2L, "Grace");

        assertEquals(1, inserted);
        assertName(2L, "Grace");
    }

    @Test
    void returnsBigIntegerUpdateCount() throws ReflectiveOperationException {
        BigInteger updated = invoke("renameBigInteger", new Class[]{long.class, String.class}, 1L, "Grace");

        assertEquals(BigInteger.ONE, updated);
    }

    @Test
    void insertsEntityWithoutSql() throws ReflectiveOperationException, SQLException {
        EntityPerson person = entityPerson(2L, "Grace");

        int inserted = invoke("insertEntity", new Class[]{EntityPerson.class}, person);

        assertEquals(1, inserted);
        assertName(2L, "Grace");
    }

    @Test
    void insertsEntityWithoutNoColumnProperty() throws ReflectiveOperationException, SQLException {
        EntityPersonWithNoColumn person = new EntityPersonWithNoColumn();
        person.id = 2L;
        person.firstName = "Grace";
        person.derivedValue = "not persisted";

        int inserted = invoke("insertNoColumnEntity", new Class[]{EntityPersonWithNoColumn.class}, person);

        assertEquals(1, inserted);
        assertName(2L, "Grace");
    }

    @Test
    void updatesEntityWithoutSql() throws ReflectiveOperationException, SQLException {
        EntityPerson person = entityPerson(1L, "Augusta");

        boolean updated = invoke("updateEntity", new Class[]{EntityPerson.class}, person);

        assertTrue(updated);
        assertName("Augusta");
    }

    @Test
    void updatesCompositePrimaryKeyEntityWithoutSql() throws ReflectiveOperationException, SQLException {
        EmployeeRole role = employeeRole(1L, 10L, "Admin");

        boolean updated = invoke("updateRole", new Class[]{EmployeeRole.class}, role);

        assertTrue(updated);
        assertRoleLabel(1L, 10L, "Admin");
    }

    @Test
    void omitsMissingOptionalJsonColumnWhenInsertingEntityWithoutSql() throws ReflectiveOperationException, SQLException {
        OptionalPerson person = optionalPerson(1L, "Ada");

        int inserted = invoke("insertOptionalEntity", new Class[]{OptionalPerson.class}, person);

        assertEquals(1, inserted);
        assertOptionalName(1L, "Ada");
    }

    @Test
    void omitsMissingOptionalJsonColumnWhenUpdatingEntityWithoutSql() throws ReflectiveOperationException, SQLException {
        OptionalPerson person = optionalPerson(1L, "Ada");
        invoke("insertOptionalEntity", new Class[]{OptionalPerson.class}, person);
        person.firstName = "Augusta";

        boolean updated = invoke("updateOptionalEntity", new Class[]{OptionalPerson.class}, person);

        assertTrue(updated);
        assertOptionalName(1L, "Augusta");
    }

    @Test
    void returnsComplexParameter() throws ReflectiveOperationException, SQLException {
        Person person = new Person(2L, "Grace");

        Person returned = invoke("insertAndReturn", new Class[]{Person.class, long.class, String.class}, person, 2L, "Grace");

        assertSame(person, returned);
        assertName(2L, "Grace");
    }

    @Test
    void rejectsUnsupportedReturnType() throws ReflectiveOperationException {
        Method method = PersonUpdates.class.getDeclaredMethod("invalidReturn", long.class);
        var handler = new UpdateMethodHandler(dataSource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("@Update return type must be void, boolean, integer number, or a parameter type: " + method, exception.getMessage());
    }

    @Test
    void rejectsFractionalNumberReturnType() throws ReflectiveOperationException {
        Method method = PersonUpdates.class.getDeclaredMethod("invalidFractionalReturn", long.class);
        var handler = new UpdateMethodHandler(dataSource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("@Update return type must be void, boolean, integer number, or a parameter type: "
                + method, exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = PersonUpdates.class.getDeclaredMethod(methodName, parameterTypes);
        var handler = new UpdateMethodHandler(dataSource);
        handler.init(method);
        return (T) handler.invoke(args);
    }

    private void assertName(String name) throws SQLException {
        assertName(1L, name);
    }

    private void assertName(long id, String name) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select first_name from people where id = ?")) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(name, rs.getString(1));
            }
        }
    }

    private void assertRoleLabel(long employeeId, long roleId, String label) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("""
                     select label from employee_roles where employee_id = ? and role_id = ?
                     """)) {
            statement.setLong(1, employeeId);
            statement.setLong(2, roleId);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(label, rs.getString(1));
            }
        }
    }

    private EntityPerson entityPerson(long id, String firstName) {
        EntityPerson person = new EntityPerson();
        person.id = id;
        person.firstName = firstName;
        return person;
    }

    private OptionalPerson optionalPerson(long id, String firstName) {
        OptionalPerson person = new OptionalPerson();
        person.id = id;
        person.firstName = firstName;
        person.roles = List.of("USER");
        return person;
    }

    private void assertOptionalName(long id, String name) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select first_name from optional_people where id = ?")) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(name, rs.getString(1));
            }
        }
    }

    private EmployeeRole employeeRole(long employeeId, long roleId, String label) {
        EmployeeRole role = new EmployeeRole();
        role.employeeId = employeeId;
        role.roleId = roleId;
        role.label = label;
        return role;
    }

    interface PersonUpdates {

        @Update("update people set first_name = {name} where id = {id}")
        boolean rename(@Param("id") long id, @Param("name") String name);

        @Update("update people set first_name = {name} where id = {id}")
        BigInteger renameBigInteger(@Param("id") long id, @Param("name") String name);

        @Update("update people set first_name = '{name}' where id = {id}")
        boolean renameWithQuotedParameter(@Param("id") long id, @Param("name") String name);

        @Insert("insert into people (id, first_name) values (?, ?)")
        int insert(long id, String name);

        @Insert
        int insertEntity(EntityPerson person);

        @Insert
        int insertNoColumnEntity(EntityPersonWithNoColumn person);

        @Update
        boolean updateEntity(EntityPerson person);

        @Update
        boolean updateRole(EmployeeRole role);

        @Insert
        int insertOptionalEntity(OptionalPerson person);

        @Update
        boolean updateOptionalEntity(OptionalPerson person);

        @Insert("insert into people (id, first_name) values ({id}, {name})")
        Person insertAndReturn(Person person, @Param("id") long id, @Param("name") String name);

        @Update("update people set first_name = 'x' where id = ?")
        String invalidReturn(long id);

        @Update("update people set first_name = 'x' where id = ?")
        double invalidFractionalReturn(long id);
    }

    record Person(long id, String name) {
    }

    @Entity("people")
    static class EntityPerson {
        long id;
        String firstName;
    }

    @Entity("people")
    static class EntityPersonWithNoColumn {
        long id;
        String firstName;
        @NoColumn
        String derivedValue;
    }

    @Entity("optional_people")
    static class OptionalPerson {
        long id;
        String firstName;
        @OptionalColumn
        @JsonColumn
        List<String> roles;
    }

    @Entity("employee_roles")
    static class EmployeeRole {
        long employeeId;
        long roleId;
        String label;
    }
}
