package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveMethodHandlerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:save-method-handler;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists orders");
            statement.execute("drop table if exists customers");
            statement.execute("drop table if exists optional_people");
            statement.execute("drop table if exists people_without_pk");
            statement.execute("drop table if exists employee_roles");
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("create table optional_people (id bigint primary key, first_name varchar(100))");
            statement.execute("create table people_without_pk (id bigint, first_name varchar(100))");
            statement.execute("""
                    create table employee_roles (
                        employee_id bigint,
                        role_id bigint,
                        label varchar(100),
                        primary key (employee_id, role_id)
                    )
                    """);
            statement.execute("create table customers (id bigint primary key, name varchar(100))");
            statement.execute("""
                    create table orders (
                        id bigint primary key,
                        label varchar(100),
                        customer_id bigint,
                        constraint fk_orders_customer foreign key (customer_id) references customers(id)
                    )
                    """);
            statement.execute("insert into people values (1, 'Ada')");
            statement.execute("insert into employee_roles values (1, 10, 'User')");
            statement.execute("insert into customers values (1, 'Ada')");
            statement.execute("insert into orders values (10, 'Old board', 1)");
        }
    }

    @Test
    void insertsWhenPrimaryKeyDoesNotExist() throws ReflectiveOperationException, SQLException {
        Person person = new Person();
        person.id = 2L;
        person.firstName = "Grace";

        Person returned = invoke("save", new Class[]{Person.class}, person);

        assertSame(person, returned);
        assertName(2L, "Grace");
    }

    @Test
    void updatesWhenPrimaryKeyExists() throws ReflectiveOperationException, SQLException {
        Person person = new Person();
        person.id = 1L;
        person.firstName = "Augusta";

        boolean updated = invoke("saveBoolean", new Class[]{Person.class}, person);

        assertTrue(updated);
        assertName(1L, "Augusta");
    }

    @Test
    void executesExplicitSaveSql() throws ReflectiveOperationException, SQLException {
        Person person = new Person();
        person.id = 2L;
        person.firstName = "Grace";

        Person returned = invoke("saveSql", new Class[]{Person.class}, person);

        assertSame(person, returned);
        assertName(2L, "Grace");
    }

    @Test
    void insertsCompositePrimaryKeyEntity() throws ReflectiveOperationException, SQLException {
        EmployeeRole role = employeeRole(1L, 11L, "Admin");

        int saved = invoke("saveRole", new Class[]{EmployeeRole.class}, role);

        assertEquals(1, saved);
        assertRoleLabel(1L, 11L, "Admin");
    }

    @Test
    void updatesCompositePrimaryKeyEntity() throws ReflectiveOperationException, SQLException {
        EmployeeRole role = employeeRole(1L, 10L, "Admin");

        boolean saved = invoke("saveRoleBoolean", new Class[]{EmployeeRole.class}, role);

        assertTrue(saved);
        assertRoleLabel(1L, 10L, "Admin");
    }

    @Test
    void rejectsEntityWithoutPrimaryKey() throws ReflectiveOperationException {
        Method method = PersonSaves.class.getDeclaredMethod("saveWithoutPk", PersonWithoutPrimaryKey.class);
        var handler = new SaveMethodHandler(dataSource);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.init(method));

        assertEquals("No primary key for table people_without_pk", exception.getMessage());
    }

    @Test
    void insertsCollectionElementsWithParentForeignKey() throws ReflectiveOperationException, SQLException {
        Customer customer = new Customer();
        customer.id = 2L;
        customer.name = "Grace";
        customer.orders = List.of(order(20L, "Board"), order(21L, "Clock"));

        Customer returned = invoke("saveCustomer", new Class[]{Customer.class}, customer);

        assertSame(customer, returned);
        assertCustomer(2L, "Grace");
        assertOrder(20L, "Board", 2L);
        assertOrder(21L, "Clock", 2L);
    }

    @Test
    void updatesCollectionElementsWithParentForeignKey() throws ReflectiveOperationException, SQLException {
        Customer customer = new Customer();
        customer.id = 1L;
        customer.name = "Ada Lovelace";
        customer.orders = List.of(order(10L, "New board"), order(11L, "Clock"));

        boolean updated = invoke("saveCustomerBoolean", new Class[]{Customer.class}, customer);

        assertTrue(updated);
        assertCustomer(1L, "Ada Lovelace");
        assertOrder(10L, "New board", 1L);
        assertOrder(11L, "Clock", 1L);
    }

    @Test
    void omitsMissingOptionalJsonColumnWhenSavingEntity() throws ReflectiveOperationException, SQLException {
        OptionalPerson person = new OptionalPerson();
        person.id = 1L;
        person.firstName = "Ada";
        person.roles = List.of("USER");

        OptionalPerson returned = invoke("saveOptionalPerson", new Class[]{OptionalPerson.class}, person);

        assertSame(person, returned);
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select first_name from optional_people where id = ?")) {
            statement.setLong(1, 1L);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("Ada", rs.getString(1));
            }
        }
    }

    private Order order(long id, String label) {
        Order order = new Order();
        order.id = id;
        order.label = label;
        return order;
    }

    private EmployeeRole employeeRole(long employeeId, long roleId, String label) {
        EmployeeRole role = new EmployeeRole();
        role.employeeId = employeeId;
        role.roleId = roleId;
        role.label = label;
        return role;
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = PersonSaves.class.getDeclaredMethod(methodName, parameterTypes);
        var handler = new SaveMethodHandler(dataSource);
        handler.init(method);
        return (T) handler.invoke(args);
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

    private void assertCustomer(long id, String name) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select name from customers where id = ?")) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(name, rs.getString(1));
            }
        }
    }

    private void assertOrder(long id, String label, long customerId) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select label, customer_id from orders where id = ?")) {
            statement.setLong(1, id);
            try (var rs = statement.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(label, rs.getString(1));
                assertEquals(customerId, rs.getLong(2));
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

    interface PersonSaves {

        @Save
        Person save(Person person);

        @Save
        boolean saveBoolean(Person person);

        @Save("insert into people (id, first_name) values ({id}, {firstName})")
        Person saveSql(Person person);

        @Save
        int saveRole(EmployeeRole role);

        @Save
        boolean saveRoleBoolean(EmployeeRole role);

        @Save
        void saveWithoutPk(PersonWithoutPrimaryKey person);

        @Save
        Customer saveCustomer(Customer customer);

        @Save
        boolean saveCustomerBoolean(Customer customer);

        @Save
        OptionalPerson saveOptionalPerson(OptionalPerson person);
    }

    @Entity("people")
    static class Person {
        long id;
        String firstName;
    }

    @Entity("optional_people")
    static class OptionalPerson {
        long id;
        String firstName;
        @OptionalColumn
        @JsonColumn
        List<String> roles;
    }

    @Entity("people_without_pk")
    static class PersonWithoutPrimaryKey {
        long id;
        String firstName;
    }

    @Entity("customers")
    static class Customer {
        long id;
        String name;
        List<Order> orders;
    }

    @Entity("orders")
    static class Order {
        long id;
        String label;
    }

    @Entity("employee_roles")
    static class EmployeeRole {
        long employeeId;
        long roleId;
        String label;
    }
}
