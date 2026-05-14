package one.xis.sql;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ROMapperTest {

    private final ROMapper mapper = new ROMapper();

    @Test
    void mapsSimpleFields() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:simple-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table simple_rows (
                        id bigint,
                        name varchar(100),
                        active boolean,
                        price decimal(10, 2),
                        code varchar(20),
                        state varchar(20)
                    )
                    """);
            statement.execute("insert into simple_rows values (7, 'Desk', true, 12.50, 'xy', 'OPEN')");

            try (var rs = statement.executeQuery("select id, name, active, price, code, state from simple_rows")) {
                SimpleRow row = mapper.toObject(rs, SimpleRow.class);

                assertEquals(7L, row.id);
                assertEquals("Desk", row.name);
                assertEquals(true, row.active);
                assertEquals(new BigDecimal("12.50"), row.price);
                assertArrayEquals(new char[]{'x', 'y'}, row.code);
                assertEquals(State.OPEN, row.state);
            }
        }
    }

    @Test
    void mapsClobToStringContent() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:clob-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table clob_rows (id bigint, description clob)");
            statement.execute("insert into clob_rows values (1, 'Long text from clob')");

            try (var rs = statement.executeQuery("select id, description from clob_rows")) {
                ClobRow row = mapper.toObject(rs, ClobRow.class);

                assertEquals(1L, row.id);
                assertEquals("Long text from clob", row.description);
            }
        }
    }

    @Test
    void returnsNullWhenResultSetIsEmpty() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:empty-result;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table simple_rows (id bigint)");

            try (var rs = statement.executeQuery("select id from simple_rows")) {
                assertNull(mapper.toObject(rs, SimpleRow.class));
            }
        }
    }

    @Test
    void mapsSnakeCaseAndExplicitColumnNames() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:column-names;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table column_rows (id bigint, first_name varchar(100), legacy_name varchar(100))");
            statement.execute("insert into column_rows values (3, 'Ada', 'Lovelace')");

            try (var rs = statement.executeQuery("select id, first_name, legacy_name from column_rows")) {
                ColumnRow row = mapper.toObject(rs, ColumnRow.class);

                assertEquals(3L, row.id);
                assertEquals("Ada", row.firstName);
                assertEquals("Lovelace", row.lastName);
            }
        }
    }

    @Test
    void mapsInheritedFields() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:inherited-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table inherited_rows (id bigint, name varchar(100))");
            statement.execute("insert into inherited_rows values (5, 'Inherited')");

            try (var rs = statement.executeQuery("select id, name from inherited_rows")) {
                InheritedRow row = mapper.toObject(rs, InheritedRow.class);

                assertEquals(5L, row.id);
                assertEquals("Inherited", row.name);
            }
        }
    }

    @Test
    void rejectsMissingColumnForMappedField() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:strict-missing-column;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table strict_rows (id bigint)");
            statement.execute("insert into strict_rows values (1)");

            try (var rs = statement.executeQuery("select id from strict_rows")) {
                IllegalStateException exception = assertThrows(IllegalStateException.class,
                        () -> mapper.toObject(rs, StrictRow.class));

                assertEquals("No column for property one.xis.sql.ROMapperTest$StrictRow.name mapped to name",
                        exception.getMessage());
            }
        }
    }

    @Test
    void ignoresFieldsMarkedAsNoColumnIgnoreOrTransient() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:strict-ignored-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table strict_ignored_rows (
                        id bigint,
                        ignored varchar(100),
                        transient_value varchar(100),
                        derived_value varchar(100)
                    )
                    """);
            statement.execute("insert into strict_ignored_rows values (1, 'ignored by mapper', 'transient', 'derived')");

            try (var rs = statement.executeQuery("select id, ignored, transient_value, derived_value from strict_ignored_rows")) {
                StrictIgnoredRow row = mapper.toObject(rs, StrictIgnoredRow.class);

                assertEquals(1L, row.id);
                assertNull(row.ignored);
                assertNull(row.transientValue);
                assertNull(row.derivedValue);
            }
        }
    }

    @Test
    void allowsExplicitlyUnmappedInheritedFields() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:allowed-unmapped-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table crm_employees (id bigint, user_id varchar(100), name varchar(100), password varchar(100))");
            statement.execute("insert into crm_employees values (1, 'mara', 'Mara Stein', 'demo')");

            try (var rs = statement.executeQuery("select id, user_id, name, password from crm_employees")) {
                SqlEmployee row = mapper.toObject(rs, SqlEmployee.class);

                assertEquals(1L, row.id);
                assertEquals("mara", row.userId);
                assertEquals("Mara Stein", row.name);
                assertEquals("demo", row.password);
                assertNull(row.pictureUrl);
                assertNull(row.roles);
            }
        }
    }

    @Test
    void mapsJsonColumnValues() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:json-column-values;DB_CLOSE_DELAY=-1");
            var statement = connection.createStatement()) {
            statement.execute("create table json_rows (id bigint, roles varchar(1000), settings varchar(1000))");
            try (var insert = connection.prepareStatement("insert into json_rows values (?, ?, ?)")) {
                insert.setLong(1, 1L);
                insert.setString(2, "[\"USER\",\"comma,role\",\"quote\\\"role\"]");
                insert.setString(3, "{\"theme\":\"dark\",\"pageSize\":25}");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select id, roles, settings from json_rows")) {
                JsonRow row = mapper.toObject(rs, JsonRow.class);

                assertEquals(1L, row.id);
                assertEquals(List.of("USER", "comma,role", "quote\"role"), row.roles);
                assertEquals("dark", row.settings.theme);
                assertEquals(25, row.settings.pageSize);
            }
        }
    }

    @Test
    void allowsOptionalJsonColumnOnAccessorToBeMissing() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:optional-json-accessor;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table optional_rows (id bigint)");
            statement.execute("insert into optional_rows values (1)");

            try (var rs = statement.executeQuery("select id from optional_rows")) {
                OptionalJsonRow row = mapper.toObject(rs, OptionalJsonRow.class);

                assertEquals(1L, row.id);
                assertNull(row.roles);
            }
        }
    }

    @Test
    void groupsSimpleJoinRowsIntoCollectionField() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:simple-join;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table joined_rows (a_id bigint, b int)");
            statement.execute("insert into joined_rows values (1, 1), (1, 2), (1, 3)");

            try (var rs = statement.executeQuery("select a_id, b from joined_rows order by b")) {
                List<JoinedRow> rows = mapper.toObjects(rs, JoinedRow.class);

                assertEquals(1, rows.size());
                assertEquals(1L, rows.get(0).id);
                assertEquals(List.of(1, 2, 3), rows.get(0).list);
            }
        }
    }

    @Test
    void groupsJoinedRowsIntoObjectListField() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:object-list;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table order_customer_rows (order_id bigint, name varchar(100), age int)");
            statement.execute("insert into order_customer_rows values (1, 'Ada', 42), (1, 'Grace', 37)");

            try (var rs = statement.executeQuery("select order_id, name, age from order_customer_rows order by name")) {
                List<OrderRow> rows = mapper.toObjects(rs, OrderRow.class);

                assertEquals(1, rows.size());
                assertEquals(1L, rows.get(0).id);
                assertEquals(2, rows.get(0).customers.size());
                assertEquals("Ada", rows.get(0).customers.get(0).name);
                assertEquals(42, rows.get(0).customers.get(0).age);
                assertEquals("Grace", rows.get(0).customers.get(1).name);
                assertEquals(37, rows.get(0).customers.get(1).age);
            }
        }
    }

    @Test
    void mapsEntityCollectionWhenForeignKeyExists() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:fk-object-list;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table orders (id bigint primary key)");
            statement.execute("""
                    create table customers (
                        id bigint primary key,
                        order_id bigint not null,
                        name varchar(100),
                        age int,
                        constraint fk_customers_orders foreign key (order_id) references orders(id)
                    )
                    """);
            statement.execute("insert into orders values (1)");
            statement.execute("insert into customers values (10, 1, 'Ada', 42), (11, 1, 'Grace', 37)");

            try (var rs = statement.executeQuery("""
                    select orders.id, customers.id, customers.name, customers.age
                    from orders
                    join customers on customers.order_id = orders.id
                    order by customers.name
                    """)) {
                List<EntityOrder> rows = mapper.toObjects(rs, EntityOrder.class);

                assertEquals(1, rows.size());
                assertEquals(1L, rows.get(0).id);
                assertEquals(2, rows.get(0).customers.size());
                assertEquals(10L, rows.get(0).customers.get(0).id);
                assertEquals("Ada", rows.get(0).customers.get(0).name);
                assertEquals(11L, rows.get(0).customers.get(1).id);
                assertEquals("Grace", rows.get(0).customers.get(1).name);
            }
        }
    }

    @Test
    void rejectsEntityCollectionWhenForeignKeyIsMissing() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:missing-fk-object-list;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table orders (id bigint primary key)");
            statement.execute("create table customers (id bigint primary key, order_id bigint not null, name varchar(100), age int)");
            statement.execute("insert into orders values (1)");
            statement.execute("insert into customers values (10, 1, 'Ada', 42)");

            try (var rs = statement.executeQuery("""
                    select orders.id, customers.id, customers.name, customers.age
                    from orders
                    join customers on customers.order_id = orders.id
                    """)) {
                IllegalStateException exception = assertThrows(IllegalStateException.class,
                        () -> mapper.toObjects(rs, EntityOrder.class));

                assertEquals("No foreign key from customers to orders", exception.getMessage());
            }
        }
    }

    @Test
    void mapsEntityReferenceWhenForeignKeyExists() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:fk-reference;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table customers (id bigint primary key, name varchar(100), age int)");
            statement.execute("""
                    create table orders (
                        id bigint primary key,
                        customer_id bigint not null,
                        constraint fk_orders_customers foreign key (customer_id) references customers(id)
                    )
                    """);
            statement.execute("insert into customers values (10, 'Ada', 42)");
            statement.execute("insert into orders values (1, 10)");

            try (var rs = statement.executeQuery("""
                    select orders.id, customers.id, customers.name, customers.age
                    from orders
                    join customers on customers.id = orders.customer_id
                    """)) {
                OrderWithCustomer order = mapper.toObject(rs, OrderWithCustomer.class);

                assertEquals(1L, order.id);
                assertEquals(10L, order.customer.id);
                assertEquals("Ada", order.customer.name);
                assertEquals(42, order.customer.age);
            }
        }
    }

    @Test
    void mapsEntityReferenceWhenReferencedTableHasForeignKey() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:fk-one-to-one-reference;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table persons (id bigint primary key, name varchar(100))");
            statement.execute("""
                    create table profiles (
                        id bigint primary key,
                        person_id bigint not null unique,
                        bio varchar(100),
                        constraint fk_profiles_persons foreign key (person_id) references persons(id)
                    )
                    """);
            statement.execute("insert into persons values (1, 'Ada')");
            statement.execute("insert into profiles values (10, 1, 'First programmer')");

            try (var rs = statement.executeQuery("""
                    select persons.id, persons.name, profiles.id, profiles.bio
                    from persons
                    join profiles on profiles.person_id = persons.id
                    """)) {
                PersonWithProfile person = mapper.toObject(rs, PersonWithProfile.class);

                assertEquals(1L, person.id);
                assertEquals("Ada", person.name);
                assertEquals(10L, person.profile.id);
                assertEquals("First programmer", person.profile.bio);
            }
        }
    }

    @Test
    void rejectsBidirectionalEntityModel() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:bidirectional-entity-model;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table bidirectional_orders (id bigint primary key)");
            statement.execute("""
                    create table bidirectional_customers (
                        id bigint primary key,
                        order_id bigint not null,
                        name varchar(100),
                        constraint fk_bidirectional_customers_orders foreign key (order_id) references bidirectional_orders(id)
                    )
                    """);
            statement.execute("insert into bidirectional_orders values (1)");
            statement.execute("insert into bidirectional_customers values (10, 1, 'Ada')");

            try (var rs = statement.executeQuery("""
                    select bidirectional_orders.id, bidirectional_customers.id, bidirectional_customers.name
                    from bidirectional_orders
                    join bidirectional_customers on bidirectional_customers.order_id = bidirectional_orders.id
                    """)) {
                IllegalStateException exception = assertThrows(IllegalStateException.class,
                        () -> mapper.toObjects(rs, BidirectionalOrder.class));

                assertTrue(exception.getMessage().startsWith("Bidirectional relations are not supported: "));
            }
        }
    }

    @Test
    void mapsRecordRootAndRecordListElements() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:record-list;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table record_rows (order_id bigint, name varchar(100), age int)");
            statement.execute("insert into record_rows values (2, 'Ada', 42), (2, 'Grace', 37)");

            try (var rs = statement.executeQuery("select order_id, name, age from record_rows order by name")) {
                List<OrderRecord> rows = mapper.toObjects(rs, OrderRecord.class);

                assertEquals(1, rows.size());
                assertEquals(2L, rows.get(0).id());
                assertEquals(List.of(new CustomerRecord("Ada", 42), new CustomerRecord("Grace", 37)), rows.get(0).customers());
            }
        }
    }

    @Test
    void mapsOldAndNewDateTypes() throws SQLException {
        Instant instant = Instant.parse("2026-05-11T10:15:30Z");
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:date-types;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table date_rows (
                        util_date timestamp,
                        calendar_value timestamp,
                        sql_date date,
                        sql_time time,
                        sql_timestamp timestamp,
                        local_date date,
                        local_time time,
                        local_date_time timestamp,
                        instant timestamp,
                        offset_date_time varchar(40),
                        offset_time varchar(20),
                        zoned_date_time varchar(80),
                        year_value varchar(4),
                        year_month varchar(7),
                        month_day varchar(7),
                        month_value varchar(20),
                        day_of_week varchar(20),
                        duration_value varchar(30),
                        period_value varchar(30)
                    )
                    """);
            try (var insert = connection.prepareStatement("insert into date_rows values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                insert.setTimestamp(1, Timestamp.from(instant));
                insert.setTimestamp(2, Timestamp.from(instant));
                insert.setDate(3, java.sql.Date.valueOf("2026-05-11"));
                insert.setTime(4, java.sql.Time.valueOf("10:15:30"));
                insert.setTimestamp(5, Timestamp.from(instant));
                insert.setDate(6, java.sql.Date.valueOf("2026-05-11"));
                insert.setTime(7, java.sql.Time.valueOf("10:15:30"));
                insert.setTimestamp(8, Timestamp.from(instant));
                insert.setTimestamp(9, Timestamp.from(instant));
                insert.setString(10, "2026-05-11T12:15:30+02:00");
                insert.setString(11, "10:15:30+02:00");
                insert.setString(12, "2026-05-11T12:15:30+02:00[Europe/Berlin]");
                insert.setString(13, "2026");
                insert.setString(14, "2026-05");
                insert.setString(15, "--05-11");
                insert.setString(16, "MAY");
                insert.setString(17, "MONDAY");
                insert.setString(18, "PT15M");
                insert.setString(19, "P2D");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select * from date_rows")) {
                DateRow row = mapper.toObject(rs, DateRow.class);

                assertEquals(instant, row.utilDate.toInstant());
                assertEquals(instant, row.calendarValue.toInstant());
                assertEquals(LocalDate.of(2026, 5, 11), row.sqlDate.toLocalDate());
                assertEquals(LocalTime.of(10, 15, 30), row.sqlTime.toLocalTime());
                assertEquals(instant, row.sqlTimestamp.toInstant());
                assertEquals(LocalDate.of(2026, 5, 11), row.localDate);
                assertEquals(LocalTime.of(10, 15, 30), row.localTime);
                assertEquals(LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()), row.localDateTime);
                assertEquals(instant, row.instant);
                assertEquals(OffsetDateTime.parse("2026-05-11T12:15:30+02:00"), row.offsetDateTime);
                assertEquals(OffsetTime.parse("10:15:30+02:00"), row.offsetTime);
                assertEquals(ZonedDateTime.parse("2026-05-11T12:15:30+02:00[Europe/Berlin]"), row.zonedDateTime);
                assertEquals(Year.of(2026), row.yearValue);
                assertEquals(YearMonth.of(2026, 5), row.yearMonth);
                assertEquals(MonthDay.of(5, 11), row.monthDay);
                assertEquals(Month.MAY, row.monthValue);
                assertEquals(DayOfWeek.MONDAY, row.dayOfWeek);
                assertEquals(Duration.ofMinutes(15), row.durationValue);
                assertEquals(Period.ofDays(2), row.periodValue);
            }
        }
    }

    enum State {
        OPEN
    }

    static class SimpleRow {
        long id;
        String name;
        boolean active;
        BigDecimal price;
        char[] code;
        State state;
    }

    static class ClobRow {
        long id;
        String description;
    }

    static class JoinedRow {
        long id;
        List<Integer> list;
    }

    static class JsonRow {
        long id;
        @JsonColumn
        List<String> roles;
        @JsonColumn
        Settings settings;
    }

    static class Settings {
        String theme;
        int pageSize;
    }

    static class OptionalJsonRow {
        long id;
        List<String> roles;

        @OptionalColumn
        @JsonColumn
        public List<String> getRoles() {
            return roles;
        }
    }

    static class ColumnRow {
        long id;
        String firstName;
        @Column("legacy_name")
        String lastName;
    }

    static class StrictRow {
        long id;
        String name;
    }

    static class StrictIgnoredRow {
        long id;
        @Ignore
        String ignored;
        transient String transientValue;
        @NoColumn
        String derivedValue;
    }

    static class UserInfoLike {
        String userId;
        String name;
        String pictureUrl;
        List<String> roles;
    }

    @Entity(value = "crm_employees", allowUnmappedFields = true)
    static class SqlEmployee extends UserInfoLike {
        long id;
        String password;
    }

    static class BaseRow {
        long id;
    }

    static class InheritedRow extends BaseRow {
        String name;
    }

    static class OrderRow {
        long id;
        List<Customer> customers;
    }

    static class Customer {
        String name;
        int age;
    }

    @Entity("orders")
    static class EntityOrder {
        long id;
        List<EntityCustomer> customers;
    }

    @Entity("customers")
    static class EntityCustomer {
        long id;
        String name;
        int age;
    }

    @Entity("orders")
    static class OrderWithCustomer {
        long id;
        EntityCustomer customer;
    }

    @Entity("persons")
    static class PersonWithProfile {
        long id;
        String name;
        Profile profile;
    }

    @Entity("profiles")
    static class Profile {
        long id;
        String bio;
    }

    @Entity("bidirectional_orders")
    static class BidirectionalOrder {
        long id;
        List<BidirectionalCustomer> customers;
    }

    @Entity("bidirectional_customers")
    static class BidirectionalCustomer {
        long id;
        String name;
        BidirectionalOrder order;
    }

    static class DateRow {
        java.util.Date utilDate;
        Calendar calendarValue;
        java.sql.Date sqlDate;
        java.sql.Time sqlTime;
        Timestamp sqlTimestamp;
        LocalDate localDate;
        LocalTime localTime;
        LocalDateTime localDateTime;
        Instant instant;
        OffsetDateTime offsetDateTime;
        OffsetTime offsetTime;
        ZonedDateTime zonedDateTime;
        Year yearValue;
        YearMonth yearMonth;
        MonthDay monthDay;
        Month monthValue;
        DayOfWeek dayOfWeek;
        Duration durationValue;
        Period periodValue;
    }

    record OrderRecord(long id, List<CustomerRecord> customers) {
    }

    record CustomerRecord(String name, int age) {
    }
}
