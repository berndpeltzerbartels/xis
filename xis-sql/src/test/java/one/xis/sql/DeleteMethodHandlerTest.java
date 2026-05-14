package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeleteMethodHandlerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:delete-method-handler;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists order_lines");
            statement.execute("drop table if exists orders");
            statement.execute("drop table if exists customers");
            statement.execute("drop table if exists cascade_orders");
            statement.execute("drop table if exists cascade_customers");
            statement.execute("drop table if exists employee_roles");
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, name varchar(100))");
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
            statement.execute("""
                    create table order_lines (
                        id bigint primary key,
                        text varchar(100),
                        order_id bigint,
                        constraint fk_lines_order foreign key (order_id) references orders(id)
                    )
                    """);
            statement.execute("create table cascade_customers (id bigint primary key, name varchar(100))");
            statement.execute("""
                    create table cascade_orders (
                        id bigint primary key,
                        label varchar(100),
                        customer_id bigint,
                        constraint fk_cascade_orders_customer
                            foreign key (customer_id) references cascade_customers(id) on delete cascade
                    )
                    """);
            statement.execute("insert into customers values (1, 'Ada')");
            statement.execute("insert into orders values (10, 'Board', 1)");
            statement.execute("insert into orders values (11, 'Clock', 1)");
            statement.execute("insert into order_lines values (100, 'Line 1', 10)");
            statement.execute("insert into order_lines values (101, 'Line 2', 11)");
            statement.execute("insert into cascade_customers values (2, 'Grace')");
            statement.execute("insert into cascade_orders values (20, 'Board', 2)");
            statement.execute("insert into people values (30, 'Alan')");
            statement.execute("insert into employee_roles values (1, 10, 'User')");
        }
    }

    @Test
    void deletesChildrenBeforeParent() throws ReflectiveOperationException, SQLException {
        Customer customer = new Customer();
        customer.id = 1L;

        int deleted = invoke("deleteCustomer", new Class[]{Customer.class}, customer);

        assertEquals(5, deleted);
        assertTableCount("order_lines", 0);
        assertTableCount("orders", 0);
        assertTableCount("customers", 0);
    }

    @Test
    void skipsDirectChildDeleteWhenDatabaseCascades() throws ReflectiveOperationException, SQLException {
        CascadeCustomer customer = new CascadeCustomer();
        customer.id = 2L;

        int deleted = invoke("deleteCascadeCustomer", new Class[]{CascadeCustomer.class}, customer);

        assertEquals(1, deleted);
        assertTableCount("cascade_orders", 0);
        assertTableCount("cascade_customers", 0);
    }

    @Test
    void executesExplicitDeleteSql() throws ReflectiveOperationException, SQLException {
        boolean deleted = invoke("deletePersonSql", new Class[]{long.class}, 30L);

        assertTrue(deleted);
        assertTableCount("people", 0);
    }

    @Test
    void executesExplicitDeleteSqlWithEntityParameter() throws ReflectiveOperationException, SQLException {
        Person person = new Person();
        person.id = 30L;

        boolean deleted = invoke("deletePersonEntitySql", new Class[]{Person.class}, person);

        assertTrue(deleted);
        assertTableCount("people", 0);
    }

    @Test
    void deletesCompositePrimaryKeyEntity() throws ReflectiveOperationException, SQLException {
        EmployeeRole role = new EmployeeRole();
        role.employeeId = 1L;
        role.roleId = 10L;

        int deleted = invoke("deleteRole", new Class[]{EmployeeRole.class}, role);

        assertEquals(1, deleted);
        assertTableCount("employee_roles", 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = Deletes.class.getDeclaredMethod(methodName, parameterTypes);
        var handler = new DeleteMethodHandler(dataSource);
        handler.init(method);
        return (T) handler.invoke(args);
    }

    private void assertTableCount(String tableName, int count) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select count(*) from " + tableName)) {
            assertTrue(resultSet.next());
            assertEquals(count, resultSet.getInt(1));
        }
    }

    interface Deletes {
        @Delete
        int deleteCustomer(Customer customer);

        @Delete
        int deleteCascadeCustomer(CascadeCustomer customer);

        @Delete("delete from people where id = {id}")
        boolean deletePersonSql(@Param("id") long id);

        @Delete("delete from people where id = {id}")
        boolean deletePersonEntitySql(Person person);

        @Delete
        int deleteRole(EmployeeRole role);
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
        List<OrderLine> lines;
    }

    @Entity("order_lines")
    static class OrderLine {
        long id;
        String text;
    }

    @Entity("cascade_customers")
    static class CascadeCustomer {
        long id;
        String name;
        List<CascadeOrder> orders;
    }

    @Entity("cascade_orders")
    static class CascadeOrder {
        long id;
        String label;
    }

    @Entity("people")
    static class Person {
        long id;
        String name;
    }

    @Entity("employee_roles")
    static class EmployeeRole {
        long employeeId;
        long roleId;
        String label;
    }
}
