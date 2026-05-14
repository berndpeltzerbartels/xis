package one.xis.sql;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ORMapperTest {

    private final ORMapper mapper = new ORMapper();

    @Test
    void writesClassFieldsToPreparedStatementInExplicitOrder() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-class;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table simple_rows (
                        id bigint,
                        name varchar(100),
                        code varchar(20),
                        state varchar(20)
                    )
                    """);

            try (var insert = connection.prepareStatement("insert into simple_rows (id, name, code, state) values (?, ?, ?, ?)")) {
                var row = new SimpleRow();
                row.id = 7L;
                row.name = "Desk";
                row.code = new char[]{'x', 'y'};
                row.state = State.OPEN;

                mapper.toStatement(row, insert, "id", "name", "code", "state");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select id, name, code, state from simple_rows")) {
                rs.next();
                assertEquals(7L, rs.getLong("id"));
                assertEquals("Desk", rs.getString("name"));
                assertEquals("xy", rs.getString("code"));
                assertEquals("OPEN", rs.getString("state"));
            }
        }
    }

    @Test
    void writesRecordComponentsToPreparedStatementInRecordOrder() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-record;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table event_rows (
                        id bigint,
                        name varchar(100),
                        created timestamp
                    )
                    """);

            try (var insert = connection.prepareStatement("insert into event_rows values (?, ?, ?)")) {
                mapper.toStatement(new EventRecord(9L, "created", Instant.parse("2026-05-11T10:15:30Z")), insert);
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select id, name, created from event_rows")) {
                rs.next();
                assertEquals(9L, rs.getLong("id"));
                assertEquals("created", rs.getString("name"));
                assertEquals(Instant.parse("2026-05-11T10:15:30Z"), rs.getTimestamp("created").toInstant());
            }
        }
    }

    @Test
    void writesExplicitColumnNamesInExplicitOrder() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-column-names;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table column_rows (id bigint, first_name varchar(100), legacy_name varchar(100))");

            try (var insert = connection.prepareStatement("insert into column_rows values (?, ?, ?)")) {
                var row = new ColumnRow();
                row.id = 3L;
                row.firstName = "Ada";
                row.lastName = "Lovelace";

                mapper.toStatement(row, insert, "id", "firstName", "legacy_name");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select id, first_name, legacy_name from column_rows")) {
                rs.next();
                assertEquals(3L, rs.getLong("id"));
                assertEquals("Ada", rs.getString("first_name"));
                assertEquals("Lovelace", rs.getString("legacy_name"));
            }
        }
    }

    @Test
    void writesInheritedFieldsToPreparedStatement() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-inherited-fields;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table inherited_rows (id bigint, name varchar(100))");

            try (var insert = connection.prepareStatement("insert into inherited_rows values (?, ?)")) {
                var row = new InheritedRow();
                row.id = 5L;
                row.name = "Inherited";

                mapper.toStatement(row, insert, "id", "name");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select id, name from inherited_rows")) {
                rs.next();
                assertEquals(5L, rs.getLong("id"));
                assertEquals("Inherited", rs.getString("name"));
            }
        }
    }

    @Test
    void writesJsonColumnToPreparedStatement() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-json-column;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("create table json_rows (id bigint, roles varchar(1000))");

            try (var insert = connection.prepareStatement("insert into json_rows values (?, ?)")) {
                var row = new JsonRow();
                row.id = 1L;
                row.roles = List.of("USER", "comma,role", "quote\"role");

                mapper.toStatement(row, insert, "id", "roles");
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select roles from json_rows")) {
                rs.next();
                assertEquals("[\"USER\",\"comma,role\",\"quote\\\"role\"]", rs.getString("roles"));
            }
        }
    }

    @Test
    void writesOldAndNewDateTypesToPreparedStatement() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:or-date-types;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("""
                    create table date_rows (
                        instant_value timestamp,
                        util_date timestamp,
                        local_date date,
                        local_time time,
                        offset_date_time timestamp with time zone,
                        zoned_date_time timestamp with time zone,
                        year_value varchar(4),
                        year_month varchar(7),
                        month_day varchar(7),
                        month_value varchar(20),
                        day_of_week varchar(20),
                        duration_value varchar(30),
                        period_value varchar(30)
                    )
                    """);

            Instant instant = Instant.parse("2026-05-11T10:15:30Z");
            DateRecord record = new DateRecord(
                    instant,
                    java.util.Date.from(instant),
                    LocalDate.of(2026, 5, 11),
                    LocalTime.of(10, 15, 30),
                    OffsetDateTime.parse("2026-05-11T12:15:30+02:00"),
                    ZonedDateTime.parse("2026-05-11T12:15:30+02:00[Europe/Berlin]"),
                    Year.of(2026),
                    YearMonth.of(2026, 5),
                    MonthDay.of(5, 11),
                    Month.MAY,
                    DayOfWeek.MONDAY,
                    Duration.ofMinutes(15),
                    Period.ofDays(2)
            );

            try (var insert = connection.prepareStatement("insert into date_rows values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                mapper.toStatement(record, insert);
                assertEquals(1, insert.executeUpdate());
            }

            try (var rs = statement.executeQuery("select * from date_rows")) {
                rs.next();
                assertEquals(instant, rs.getTimestamp("instant_value").toInstant());
                assertEquals(instant, rs.getTimestamp("util_date").toInstant());
                assertEquals(LocalDate.of(2026, 5, 11), rs.getObject("local_date", LocalDate.class));
                assertEquals(LocalTime.of(10, 15, 30), rs.getObject("local_time", LocalTime.class));
                assertEquals(OffsetDateTime.parse("2026-05-11T12:15:30+02:00"), rs.getObject("offset_date_time", OffsetDateTime.class));
                assertEquals(instant, rs.getObject("zoned_date_time", OffsetDateTime.class).toInstant());
                assertEquals("2026", rs.getString("year_value"));
                assertEquals("2026-05", rs.getString("year_month"));
                assertEquals("--05-11", rs.getString("month_day"));
                assertEquals("MAY", rs.getString("month_value"));
                assertEquals("MONDAY", rs.getString("day_of_week"));
                assertEquals("PT15M", rs.getString("duration_value"));
                assertEquals("P2D", rs.getString("period_value"));
            }
        }
    }

    enum State {
        OPEN
    }

    static class SimpleRow {
        long id;
        String name;
        char[] code;
        State state;
    }

    static class ColumnRow {
        long id;
        String firstName;
        @Column("legacy_name")
        String lastName;
    }

    static class BaseRow {
        long id;
    }

    static class InheritedRow extends BaseRow {
        String name;
    }

    static class JsonRow {
        long id;
        @JsonColumn
        List<String> roles;
    }

    record EventRecord(long id, String name, Instant created) {
    }

    record DateRecord(Instant instantValue, java.util.Date utilDate, LocalDate localDate, LocalTime localTime,
                      OffsetDateTime offsetDateTime, ZonedDateTime zonedDateTime, Year yearValue,
                      YearMonth yearMonth, MonthDay monthDay, Month monthValue, DayOfWeek dayOfWeek,
                      Duration durationValue, Period periodValue) {
    }
}
