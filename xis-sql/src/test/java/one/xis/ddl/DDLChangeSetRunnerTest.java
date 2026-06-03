package one.xis.ddl;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DDLChangeSetRunnerTest {

    @Test
    void runsChangeSetsInChainOrderAndChangesAlphabetically() throws SQLException {
        BaseSchema.invocations = 0;
        CustomerSchema.invocations = 0;
        var dataSource = dataSource("ddl-changeset-order");
        var runner = new DDLChangeSetRunner(dataSource);

        runner.run(List.of(new CustomerSchema(), new BaseSchema()));
        runner.run(List.of(new CustomerSchema(), new BaseSchema()));

        assertEquals(2, count(dataSource, "select count(*) from demo_customer"));
        assertEquals(3, count(dataSource, "select count(*) from __xis_schema_change"));
        assertEquals(2, BaseSchema.invocations);
        assertEquals(1, CustomerSchema.invocations);
    }

    @Test
    void rejectsExecutedChangesThatNoLongerExist() {
        var dataSource = dataSource("ddl-changeset-history");
        var runner = new DDLChangeSetRunner(dataSource);
        runner.run(List.of(new BaseSchema()));

        var exception = assertThrows(IllegalStateException.class, () -> runner.run(List.of(new RenamedBaseSchema())));

        assertTrue(exception.getMessage().contains("Executed DDL changes no longer exist"));
        assertTrue(exception.getMessage().contains("base/001-create"));
    }

    @Test
    void rejectsMultipleRootChangeSets() {
        var runner = new DDLChangeSetRunner(dataSource("ddl-changeset-roots"));

        var exception = assertThrows(IllegalStateException.class,
                () -> runner.run(List.of(new BaseSchema(), new OtherRootSchema())));

        assertTrue(exception.getMessage().contains("Exactly one root ChangeSet"));
    }

    @Test
    void rejectsForkingChangeSetChain() {
        var runner = new DDLChangeSetRunner(dataSource("ddl-changeset-fork"));

        var exception = assertThrows(IllegalStateException.class,
                () -> runner.run(List.of(new BaseSchema(), new FirstChildSchema(), new SecondChildSchema())));

        assertTrue(exception.getMessage().contains("must not fork"));
    }

    @Test
    void rejectsCyclicChangeSetChain() {
        var runner = new DDLChangeSetRunner(dataSource("ddl-changeset-cycle"));

        var exception = assertThrows(IllegalStateException.class,
                () -> runner.run(List.of(new CycleASchema(), new CycleBSchema())));

        assertTrue(exception.getMessage().contains("Cyclic ChangeSet dependency"));
    }

    @Test
    void rejectsDuplicateChangeIdsInSameChangeSet() {
        var runner = new DDLChangeSetRunner(dataSource("ddl-changeset-duplicate-change"));

        var exception = assertThrows(IllegalStateException.class, () -> runner.run(List.of(new DuplicateChangeIdSchema())));

        assertTrue(exception.getMessage().contains("Duplicate @Change id"));
    }

    @Test
    void rejectsInvalidChangeMethodSignature() {
        var runner = new DDLChangeSetRunner(dataSource("ddl-changeset-signature"));

        var exception = assertThrows(IllegalStateException.class, () -> runner.run(List.of(new InvalidSignatureSchema())));

        assertTrue(exception.getMessage().contains("exactly one DDL parameter"));
    }

    private JdbcDataSource dataSource(String name) {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    private int count(JdbcDataSource dataSource, String sql) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }

    @ChangeSet("base")
    static class BaseSchema {
        static int invocations;

        @Change("002-insert")
        void insert(DDL ddl) {
            invocations++;
            ddl.sql("insert into demo_customer (id, name) values (1, 'Ada')");
        }

        @Change("001-create")
        void create(DDL ddl) {
            invocations++;
            var table = ddl.createTableIfNotExists("demo_customer");
            table.addColumn("id").bigint().primaryKey();
            table.addColumn("name").varchar(100).notNull();
        }
    }

    @ChangeSet(value = "customer", previous = BaseSchema.class)
    static class CustomerSchema {
        static int invocations;

        @Change("001-insert-more")
        void insertMore(DDL ddl) {
            invocations++;
            ddl.sql("insert into demo_customer (id, name) values (2, 'Grace')");
        }
    }

    @ChangeSet("renamed-base")
    static class RenamedBaseSchema {
        @Change("001-create")
        void create(DDL ddl) {
            ddl.sql("select 1");
        }
    }

    @ChangeSet("other-root")
    static class OtherRootSchema {
        @Change("001-noop")
        void noop(DDL ddl) {
        }
    }

    @ChangeSet(value = "first-child", previous = BaseSchema.class)
    static class FirstChildSchema {
        @Change("001-noop")
        void noop(DDL ddl) {
        }
    }

    @ChangeSet(value = "second-child", previous = BaseSchema.class)
    static class SecondChildSchema {
        @Change("001-noop")
        void noop(DDL ddl) {
        }
    }

    @ChangeSet(value = "cycle-a", previous = CycleBSchema.class)
    static class CycleASchema {
        @Change("001-noop")
        void noop(DDL ddl) {
        }
    }

    @ChangeSet(value = "cycle-b", previous = CycleASchema.class)
    static class CycleBSchema {
        @Change("001-noop")
        void noop(DDL ddl) {
        }
    }

    @ChangeSet("duplicate-change")
    static class DuplicateChangeIdSchema {
        @Change("001")
        void first(DDL ddl) {
        }

        @Change("001")
        void second(DDL ddl) {
        }
    }

    @ChangeSet("invalid-signature")
    static class InvalidSignatureSchema {
        @Change("001")
        void invalid() {
        }
    }
}
